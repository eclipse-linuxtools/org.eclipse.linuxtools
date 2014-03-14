/*******************************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.StringOutputStream;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.linuxtools.systemtap.structures.process.SystemtapProcessFactory;
import org.eclipse.linuxtools.systemtap.structures.runnable.StringStreamGobbler;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

/**
 * Runs stap -vp1 & stap -up2 in order to get all of the probes/functions
 * that are defined in the tapsets.  Builds probeAlias and function trees
 * with the values obtained from the tapsets.
 *
 * Ugly code is a result of two issues with getting stap output.  First,
 * many tapsets do not work under stap -up2.  Second since the output
 * is not a regular language, we can't create a nice lexor/parser combination
 * to do everything nicely.
 * @author Ryan Morse
 */
public abstract class TapsetParser extends Job {

	private ArrayList<IUpdateListener> listeners;

	protected boolean cancelRequested;

	protected TapsetParser(String jobTitle) {
		super(jobTitle);
		listeners = new ArrayList<>();
		cancelRequested = false;
	}

	@Override
	protected void canceling() {
		super.canceling();
		this.cancelRequested = true;
	}

	/**
	 * This method will register a new listener with the parser
	 * @param listener The listener that will receive updateEvents
	 */
	public void addListener(IUpdateListener listener) {
		if (null != listener) {
			listeners.add(listener);
		}
	}

	/**
	 * This method will unregister the listener with the parser
	 * @param listener The listener that no longer wants to recieve update events
	 */
	public void removeListener(IUpdateListener listener) {
		if (null != listener) {
			listeners.remove(listener);
		}
	}

	/**
	 * This method will fire an updateEvent to all listeners.
	 */
	protected void fireUpdateEvent() {
		for (IUpdateListener listener : listeners) {
			listener.handleUpdateEvent();
		}
	}

	/**
	 * Runs the stap with the given options and returns the output generated
	 * @param options String[] of any optional parameters to pass to stap
	 * @param probe String containing the script to run stap on,
	 * or <code>null</code> for scriptless commands
	 * @param getErrors Set this to <code>true</code> if the script's error
	 * stream contents should be returned instead of its standard output
	 */
	protected String runStap(String[] options, String probe, boolean getErrors) {
		String[] args = null;
		String[] tapsets = IDEPlugin.getDefault().getPreferenceStore()
				.getString(IDEPreferenceConstants.P_TAPSETS).split(File.pathSeparator);
		boolean noTapsets = tapsets[0].trim().length() == 0;
		boolean noOptions = options[0].trim().length() == 0;

		int size = probe != null ? 2 : 1;
		if (tapsets.length > 0 && !noTapsets) {
			size += tapsets.length<<1;
		}
		if (options.length > 0 && !noOptions) {
			size += options.length;
		}

		args = new String[size];
		args[0] = "stap"; //$NON-NLS-1$
		if (probe != null) {
			args[size-1] = probe;
		}

		//Add extra tapset directories
		if (tapsets.length > 0 && !noTapsets) {
			for (int i = 0; i < tapsets.length; i++) {
				args[1 + 2*i] = "-I"; //$NON-NLS-1$
				args[2 + 2*i] = tapsets[i];
			}
		}
		if (options.length > 0 && !noOptions) {
			for (int i = 0, s = noTapsets ? 1 : 1 + tapsets.length*2; i<options.length; i++) {
				args[s + i] = options[i];
			}
		}

		try {
			if (IDEPlugin.getDefault().getPreferenceStore().getBoolean(IDEPreferenceConstants.P_REMOTE_PROBES)) {
				StringOutputStream str = new StringOutputStream();
				StringOutputStream strErr = new StringOutputStream();

				IPreferenceStore p = ConsoleLogPlugin.getDefault().getPreferenceStore();
				String user = p.getString(ConsoleLogPreferenceConstants.SCP_USER);
				String host = p.getString(ConsoleLogPreferenceConstants.HOST_NAME);
				String password = p.getString(ConsoleLogPreferenceConstants.SCP_PASSWORD);

				Channel channel = SystemtapProcessFactory.execRemoteAndWait(args,str, strErr, user, host, password);
				if (channel == null) {
					displayError(Messages.TapsetParser_CannotRunStapTitle, Messages.TapsetParser_CannotRunStapMessage);
				}

				return (!getErrors ? str : strErr).toString();
			} else {
				Process process = RuntimeProcessFactory.getFactory().exec(args, null, null);
				if(process == null){
					displayError(Messages.TapsetParser_CannotRunStapTitle, Messages.TapsetParser_CannotRunStapMessage);
					return null;
				}

				StringStreamGobbler gobbler = new StringStreamGobbler(process.getInputStream());
				StringStreamGobbler egobbler = null;
				gobbler.start();
				if (getErrors) {
					egobbler = new StringStreamGobbler(process.getErrorStream());
					egobbler.start();
				}
				process.waitFor();
				gobbler.stop();
				if (egobbler == null) {
					return gobbler.getOutput().toString();
				} else {
					egobbler.stop();
					return egobbler.getOutput().toString();
				}
			}

		} catch (JSchException|IOException e) {
			ExceptionErrorDialog.openError(Messages.TapsetParser_ErrorRunningSystemtap, e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private void displayError(final String title, final String error) {
    	Display.getDefault().asyncExec(new Runnable() {
    		@Override
			public void run() {
    			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    			MessageDialog.openWarning(window.getShell(), title, error);
    		}
    	});
	}

}
