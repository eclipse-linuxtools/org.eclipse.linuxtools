/*******************************************************************************
 * Copyright (c) 2010, 2011 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *    Red Hat Inc. - rewrite to use RemoteConnection class
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchConfigurationDelegate;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.profiling.launch.remote.RemoteConnection;
import org.eclipse.linuxtools.profiling.launch.remote.RemoteConnectionException;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider;

public class ValgrindRemoteLaunchDelegate extends
ValgrindLaunchConfigurationDelegate {

	private SubMonitor monitor;
	private IPath localOutputDir;
	private IPath remoteBinFile;
	private RemoteConnection rc;

	public void launch(final ILaunchConfiguration config, String mode,
			final ILaunch launch, IProgressMonitor m) throws CoreException {
		if (m == null) {
			m = new NullProgressMonitor();
		}
		
		// Clear process as we wait on it to be instantiated
		process = null;

		monitor = SubMonitor
		.convert(
				m,
				Messages.ValgrindRemoteLaunchDelegate_task_name, 10);
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		this.config = config;
		this.launch = launch;
		try {			
			// remove any output from previous run
			ValgrindUIPlugin.getDefault().resetView();
			// reset stored launch data
			getPlugin().setCurrentLaunchConfiguration(null);
			getPlugin().setCurrentLaunch(null);

			rc = new RemoteConnection(config);
			monitor.worked(1);

			// Copy binary using FileSystem service
			final IPath exePath = CDebugUtils.verifyProgramPath(config);
			final IPath remoteDir = Path.fromOSString(config.getAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_DESTDIR, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_DESTDIR));

			remoteBinFile = remoteDir.append(exePath.lastSegment());
			
			rc.upload(exePath, remoteDir, new SubProgressMonitor(monitor, 1));
			
			IPath remoteLogDir = Path.fromOSString(config.getAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_OUTPUTDIR, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_OUTPUTDIR));
			outputPath = remoteLogDir.append("eclipse-valgrind-" + System.currentTimeMillis()); //$NON-NLS-1$

			rc.createFolder(outputPath, new SubProgressMonitor(monitor, 1));

			// Retrieve user-defined Valgrind binary location
			final IPath valgrindLocation = Path.fromOSString(config.getAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_VALGRINDLOC, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_VALGRINDLOC));
			String[] arguments = getProgramArgumentsArray(config);
			// create/empty local output directory
			IValgrindOutputDirectoryProvider provider = getPlugin().getOutputDirectoryProvider();
			try {
				localOutputDir = provider.getOutputPath();
				createDirectory(localOutputDir);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			// tool that was launched
			toolID = getTool(config);
			// ask tool extension for arguments
			dynamicDelegate = getDynamicDelegate(toolID);
			String[] opts = getValgrindArgumentsArray(config);
			@SuppressWarnings({ "unused", "unchecked" })
			Map<String, String> env = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String, String>) null);
			@SuppressWarnings("unused")
			boolean usePty = config.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
					ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);

			String command = valgrindLocation.toString();
			// Add valgrind options
			for (String opt : opts) {
				command += " " + opt; //$NON-NLS-1$
			}
			// Add executable to run
			command += " " + remoteBinFile.toString();
			// Add arguments to pass to executable
			for (String argument : arguments) {
				command += " " + argument; //$NON-NLS-1$
			}
			ArrayList<String> commandOutput = new ArrayList<String>();
			int returnValue = rc.runCommand(command, remoteDir, commandOutput, new SubProgressMonitor(monitor, 1));

			// delete remote binary
			rc.delete(remoteBinFile, new SubProgressMonitor(monitor, 1));

			if (returnValue == 0)
			// move remote log files to local directory
				rc.download(outputPath, localOutputDir, new SubProgressMonitor(monitor, 1));

			// remove remote log dir and all files under it
			rc.delete(outputPath, new SubProgressMonitor(monitor, 1));
			
			if (returnValue != 0) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < commandOutput.size(); ++i) {
					buf.append(commandOutput.get(i));
					buf.append("\n"); //$NON-NLS-1$
				}
				abort(buf.toString(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
	
			// store these for use by other classes
			getPlugin().setCurrentLaunchConfiguration(config);
			getPlugin().setCurrentLaunch(launch);

			// parse Valgrind logs
			IValgrindMessage[] messages = parseLogs(localOutputDir);

			// create launch summary string to distinguish this launch
			launchStr = createLaunchStr();

			// create view
			ValgrindUIPlugin.getDefault().createView(launchStr, toolID);
			// set log messages
			ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
			view.setMessages(messages);
			monitor.worked(1);

			// pass off control to extender
			dynamicDelegate.handleLaunch(config, launch, localOutputDir, monitor.newChild(2));
			
			// initialize tool-specific part of view
			dynamicDelegate.initializeView(view.getDynamicView(), launchStr, monitor.newChild(1));

			// refresh view
			ValgrindUIPlugin.getDefault().refreshView();

			// show view
			ValgrindUIPlugin.getDefault().showView();
			monitor.worked(1);

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteConnectionException e) {
			// TODO Auto-generated catch block
			abort(e.getLocalizedMessage(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} finally {
			monitor.done();
			m.done();
		}
	}


	protected String createLaunchStr() {
		return config.getName()
		+ " [" + getPlugin().getToolName(toolID) + " on " + rc.getId() + "] "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	protected String getPluginID() {
		return ValgrindLaunchPlugin.PLUGIN_ID;
	}

	public void onError(Throwable t) {
		// for now do nothing
	}
	
}