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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.StringOutputStream;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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

	private String[] tapsets;
	protected boolean cancelRequested;

	protected TapsetParser(String[] tapsets, String jobTitle) {
		super(jobTitle);
		this.tapsets = Arrays.copyOf(tapsets, tapsets.length);
		listeners = new ArrayList<IUpdateListener>();
		cancelRequested = false;
	}

	@Override
	protected void canceling() {
		super.canceling();
		this.cancelRequested = true;
	}

	/**
	 * This method checks to see if the parser completed executing on its own.
	 * @return Boolean indicating whether or not the thread finished on its own.
	 */
	public boolean isFinishSuccessful() {
		IStatus result = getResult();
		return result != null && result.isOK();
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
	 * @param probe String containing the script to run stap on
	 * @since 1.2
	 */
	protected String runStap(String[] options, String probe) {
		String[] args = null;

		int size = 2;	//start at 2 for stap, script, options will be added in later
		if (null != tapsets && tapsets.length > 0 && tapsets[0].trim().length() > 0) {
			size += tapsets.length<<1;
		}
		if (null != options && options.length > 0 && options[0].trim().length() > 0) {
			size += options.length;
		}

		args = new String[size];
		args[0] = ""; //$NON-NLS-1$
		args[size-1] = probe;
		args[size-2] = ""; //$NON-NLS-1$

		//Add extra tapset directories
		if(null != tapsets && tapsets.length > 0 && tapsets[0].trim().length() > 0) {
			for(int i=0; i<tapsets.length; i++) {
				args[2+(i<<1)] = "-I"; //$NON-NLS-1$
				args[3+(i<<1)] = tapsets[i];
			}
		}
		if(null != options && options.length > 0 && options[0].trim().length() > 0) {
			for(int i=0; i<options.length; i++)
				args[args.length-options.length-1+i] = options[i];
		}

		StringOutputStream str = new StringOutputStream();
		StringOutputStream strErr = new StringOutputStream();
		try {
			URI uri;
			if (IDEPlugin.getDefault().getPreferenceStore().getBoolean(IDEPreferenceConstants.P_REMOTE_PROBES)) {
				uri = IDEPlugin.getDefault().createRemoteUri(null);
			} else {
				uri = new URI(Path.ROOT.toOSString());
			}
			IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(uri);
			Process process = launcher.execute(new Path("stap"), args, null, null, null); //$NON-NLS-1$
			if(process == null){
				displayError(Messages.TapsetParser_CannotRunStapTitle, Messages.TapsetParser_CannotRunStapMessage);
			}
			launcher.waitAndRead(str, strErr, new NullProgressMonitor());
		} catch (URISyntaxException e) {
			ExceptionErrorDialog.openError(Messages.TapsetParser_ErrorRunningSystemtap, e);
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.TapsetParser_ErrorRunningSystemtap, e);
		}

		return str.toString();
	}

	private void displayError(final String title, final String error){
    	Display.getDefault().asyncExec(new Runnable() {
    		@Override
			public void run() {
    			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    			MessageDialog.openWarning(window.getShell(), title, error);
    		}
    	});
	}

}
