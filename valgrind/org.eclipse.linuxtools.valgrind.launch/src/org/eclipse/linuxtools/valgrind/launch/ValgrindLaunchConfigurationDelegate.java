/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.core.utils.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class ValgrindLaunchConfigurationDelegate extends AbstractCLaunchDelegate implements ILaunchesListener2 {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected static final String NO = "no"; //$NON-NLS-1$
	protected static final String YES = "yes"; //$NON-NLS-1$
	protected static final String EQUALS = "="; //$NON-NLS-1$

	protected static final String LOG_PREFIX = "valgrind_"; //$NON-NLS-1$
	protected static final String LOG_FILE = LOG_PREFIX + "%p.xml"; //$NON-NLS-1$
	protected static final FileFilter LOG_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(LOG_PREFIX);
		}		
	};

	protected String toolID;
	protected ValgrindCommand command;
	protected File datadir;
	protected IValgrindLaunchDelegate dynamicDelegate;
	protected ILaunchConfiguration config;
	protected ILaunch launch;
	protected IProcess process;
	protected String launchStr;
	protected SubMonitor monitor;

	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor m) throws CoreException {
		if (m == null) {
			m = new NullProgressMonitor();
		}
		monitor = SubMonitor.convert(m, Messages.getString("ValgrindLaunchConfigurationDelegate.Profiling_Local_CCPP_Application"), 10); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		this.config = config;
		this.launch	= launch;
		try {
			datadir = ValgrindLaunchPlugin.getDefault().getStateLocation().toFile();
			command = new ValgrindCommand(datadir);

			// find Valgrind
			String valgrindCmd = null;
			try {
				valgrindCmd = ValgrindCommand.whichValgrind();
			} catch (IOException e) {
				abort(Messages.getString("ValgrindLaunchConfigurationDelegate.Please_ensure_Valgrind"), e, ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST); //$NON-NLS-1$
			}

			monitor.worked(1);
			IPath exePath = verifyProgramPath(config);

			String[] arguments = getProgramArgumentsArray(config);

			// tool that was launched
			toolID = getTool(config);
			// ask tool extension for arguments
			dynamicDelegate = ValgrindLaunchPlugin.getDefault().getToolDelegate(toolID);
			String[] opts = getValgrindArgumentsArray(config);

			// set the default source locator if required
			setDefaultSourceLocator(launch, config);

			File wd = getWorkingDirectory(config);
			if (wd == null) {
				wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			ArrayList<String> cmdLine = new ArrayList<String>(1 + arguments.length);
			cmdLine.add(valgrindCmd);
			cmdLine.addAll(Arrays.asList(opts));
			cmdLine.add(exePath.toOSString());
			cmdLine.addAll(Arrays.asList(arguments));
			String[] commandArray = (String[]) cmdLine.toArray(new String[cmdLine.size()]);
			boolean usePty = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			monitor.worked(1);

			// attach this delegate as a launch listener
			DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
			
			// call Valgrind
			command.execute(commandArray, getEnvironment(config), wd, usePty);
			monitor.worked(3);
			process = DebugPlugin.newProcess(launch, command.getProcess(), renderProcessLabel(commandArray[0]));

			throw new CoreException(null);
		} catch (IOException e) {
			abort(Messages.getString("ValgrindLaunchConfigurationDelegate.Error_starting_process"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
			monitor.done();	
		}
	}

	protected String createLaunchStr() {
		return config.getName() + " [" + ValgrindLaunchPlugin.getDefault().getToolName(toolID) + "] " + process.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String[] getValgrindArgumentsArray(ILaunchConfiguration config) throws CoreException, IOException {
		ArrayList<String> opts = new ArrayList<String>();
		opts.add(ValgrindCommand.OPT_TOOL + EQUALS + ValgrindLaunchPlugin.getDefault().getToolName(toolID));
		opts.add(ValgrindCommand.OPT_QUIET);
		opts.add(ValgrindCommand.OPT_LOGFILE + EQUALS + datadir.getCanonicalPath() + File.separator + LOG_FILE);

		opts.add(ValgrindCommand.OPT_TRACECHILD + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, false) ? YES : NO));
		opts.add(ValgrindCommand.OPT_CHILDSILENT + EQUALS + YES);//(config.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_CHILDSILENT, false) ? YES : NO));
		//		opts.add(ValgrindCommand.OPT_TRACKFDS + EQUALS + (config.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TRACKFDS, false) ? YES : NO));
		//		opts.add(ValgrindCommand.OPT_TIMESTAMP + EQUALS + (config.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TIMESTAMP, false) ? YES : NO));
		opts.add(ValgrindCommand.OPT_FREERES + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, true) ? YES : NO));

		opts.add(ValgrindCommand.OPT_DEMANGLE + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, true) ? YES : NO));
		opts.add(ValgrindCommand.OPT_NUMCALLERS + EQUALS + config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, 12));
		opts.add(ValgrindCommand.OPT_ERRLIMIT + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, true) ? YES : NO));
		opts.add(ValgrindCommand.OPT_BELOWMAIN + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, false) ? YES : NO));
		opts.add(ValgrindCommand.OPT_MAXFRAME + EQUALS + config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, 2000000));

		String strpath = config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, EMPTY_STRING);
		if (!strpath.equals(EMPTY_STRING)) {
			File suppfile = ValgrindLaunchPlugin.getDefault().parseWSPath(strpath);
			if (suppfile != null) {
				String escapedPath = ValgrindLaunchPlugin.getDefault().escapeAndQuote(suppfile.getCanonicalPath());
				opts.add(ValgrindCommand.OPT_SUPPFILE + EQUALS + escapedPath);
			}
		}

		opts.addAll(Arrays.asList(dynamicDelegate.getCommandArray(command, config)));		

		String[] ret = new String[opts.size()];
		return opts.toArray(ret);
	}

	//	protected void saveState(IProgressMonitor monitor) throws CoreException, IOException {
	//		monitor.beginTask(Messages.getString("ValgrindLaunchConfigurationDelegate.Saving_Valgrind_output"), 2); //$NON-NLS-1$
	//		try {
	//			XMLMemento memento = XMLMemento.createWriteRoot(MementoConstants.ELEMENT_ROOT);
	//			//		memento.putString(MementoConstants.ELEMENT_LABEL, process.getLabel());
	//			//		memento.putString(MementoConstants.ELEMENT_DATADIR, command.getDatadir().getCanonicalPath());
	//	
	//			// write launch history to persistent storage
	//			HistoryFile.getInstance().save(launchStr, datadir, memento, config);
	//		} finally {
	//			monitor.done();
	//		}
	//	}

	protected String getTool(ILaunchConfiguration config) throws CoreException {
		return config.getAttribute(LaunchConfigurationConstants.ATTR_TOOL, ValgrindLaunchPlugin.TOOL_EXT_DEFAULT);
	}


	@Override
	protected String getPluginID() {
		return ValgrindLaunchPlugin.PLUGIN_ID;
	}

	public void launchesTerminated(ILaunch[] launches) {
		if (launch != null && Arrays.asList(launches).contains(launch) && process != null) {
			try {
				if (process.getExitValue() == 0) {
					// create launch summary string to distinguish this launch
					launchStr = createLaunchStr();

					// create view
					ValgrindUIPlugin.getDefault().createView(launchStr, toolID);

					// pass off control to extender
					dynamicDelegate.launch(command, config, launch, monitor.newChild(3));

					// refresh view
					ValgrindUIPlugin.getDefault().refreshView();

					// show view
					ValgrindUIPlugin.getDefault().showView();
					
					// save results of launch to persistent storage
					//			saveState(monitor.newChild(2));
				}
				else {
					final String errorMsg = readLogs();
					
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							IStatus subStatus = new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, errorMsg);
							String msg = Messages.getString("ValgrindLaunchConfigurationDelegate.Valgrind_error_msg"); //$NON-NLS-1$
							IStatus status = new MultiStatus(ValgrindLaunchPlugin.PLUGIN_ID, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR, new IStatus[] { subStatus }, msg, null);
							
							ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.getString("ValgrindLaunchConfigurationDelegate.Valgrind_error_title"), null, status); //$NON-NLS-1$
						}						
					});				
				}
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
				monitor.done();
			}
		}
	}

	protected String readLogs() throws IOException {
		StringBuffer buf = new StringBuffer();
		File[] logs = command.getDatadir().listFiles(LOG_FILTER);
		for (int i = 0; i < logs.length; i++) {
			BufferedReader br = new BufferedReader(new FileReader(logs[i]));
			String line;
			while ((line = br.readLine()) != null) {
				buf.append(line);
				buf.append('\n');
			}
		}
		return buf.toString();
	}

	public void launchesAdded(ILaunch[] launches) {
	}

	public void launchesChanged(ILaunch[] launches) {
	}

	public void launchesRemoved(ILaunch[] launches) {
	}

}
