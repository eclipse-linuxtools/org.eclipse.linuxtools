/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Keith Seitz <keiths@redhat.com> - setup code in launch the method, initially 
 *        written in the now-defunct OprofileSession class
 *    QNX Software Systems and others - the section of code marked in the launch 
 *        method, and the exec method
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.launch.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.OprofileDaemonEvent;
import org.eclipse.linuxtools.oprofile.core.OprofileProperties;
import org.eclipse.linuxtools.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.view.OprofileView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OprofileLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//FIXME: this assumes that project names are always the directory names in the workspace.
		//this assumption may be wrong, but a shallow lookup seems ok
		String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		String imagePath = workspacePath + 
							Path.SEPARATOR + 
							config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,"") + 
							Path.SEPARATOR + 
							config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,"");
		
		LaunchOptions options = new LaunchOptions();		//default options created in the constructor
		options.loadConfiguration(config);
		options.setImage(imagePath);

		//if daemonEvents null or zero size, the default event will be used
		OprofileDaemonEvent[] daemonEvents = null;
		if (!config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, false)) {
			//get the events to profile from the counters
			OprofileCounter[] counters = OprofileCounter.getCounters(config);
			ArrayList<OprofileDaemonEvent> events = new ArrayList<OprofileDaemonEvent>();
			
			for (int i = 0; i < counters.length; ++i) {
				if (counters[i].getEnabled())
					events.add(counters[i].getDaemonEvent());
			}
			
			daemonEvents = new OprofileDaemonEvent[events.size()];
			events.toArray(daemonEvents);
		}

		//set up and launch the oprofile daemon
		try {
			//kill the daemon (it shouldn't be running already, but to be safe)
			OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();
			
			//reset data from the (possibly) existing default session, 
			// otherwise multiple runs will combine samples and results
			// won't make much sense
			OprofileCorePlugin.getDefault().getOpcontrolProvider().reset();
			
			//setup the events and other parameters
			OprofileCorePlugin.getDefault().getOpcontrolProvider().setupDaemon(options.getOprofileDaemonOptions(), daemonEvents);
			
			//start the daemon & collection of samples 
			//note: since the daemon is only profiling for the specific image we told 
			// it to, no matter to start the daemon before the binary itself is run
			OprofileCorePlugin.getDefault().getOpcontrolProvider().startCollection();
		} catch (OpcontrolException oe) {
			String title = OprofileProperties.getString("opcontrolProvider.error.dialog.title"); //$NON-NLS-1$
			String msg = OprofileProperties.getString("opcontrolProvider.error.dialog.message"); //$NON-NLS-1$
			ErrorDialog.openError(null, title, msg, oe.getStatus());
		}

		/* 
		 * this code written by QNX Software Systems and others and was 
		 * originally in the CDT under LocalCDILaunchDelegate::RunLocalApplication
		 */
		//set up and launch the local c/c++ program
		try {
			IPath exePath = verifyProgramPath( config );
			File wd = getWorkingDirectory( config );
			if ( wd == null ) {
				wd = new File( System.getProperty( "user.home", "." ) );
			}
			String arguments[] = getProgramArgumentsArray( config );
			ArrayList<String> command = new ArrayList<String>( 1 + arguments.length );
			command.add( exePath.toOSString() );
			command.addAll( Arrays.asList( arguments ) );
			String[] commandArray = (String[])command.toArray( new String[command.size()] );
			boolean usePty = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT );
			Process process;
			process = exec( commandArray, getEnvironment( config ), wd, usePty );
			DebugPlugin.newProcess( launch, process, renderProcessLabel( commandArray[0] ) );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//add a listener for termination of the launch
		ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
		lmgr.addLaunchListener(new LaunchTerminationWatcher(launch));

	}
	
	/**
	 * This code was adapted from code written by QNX Software Systems and others 
	 * and was originally in the CDT under LocalCDILaunchDelegate::exec
	 * 
	 * @param cmdLine
	 *            the command line
	 * @param workingDirectory
	 *            the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *         cancelled
	 * @see Runtime
	 */
	protected Process exec( String[] cmdLine, String[] environ, File workingDirectory, boolean usePty ) throws CoreException, IOException {
		Process p = null;
		try {
			if ( workingDirectory == null ) {
				p = ProcessFactory.getFactory().exec( cmdLine, environ );
			}
			else {
				if ( usePty && PTY.isSupported() ) {
					p = ProcessFactory.getFactory().exec( cmdLine, environ, workingDirectory, new PTY() );
				}
				else {
					p = ProcessFactory.getFactory().exec( cmdLine, environ, workingDirectory );
				}
			}
		}
		catch( IOException e ) {
			if ( p != null ) {
				p.destroy();
			}
			throw e;
		}
		return p;
	}

	@Override
	protected String getPluginID() {
		return OprofileLaunchPlugin.getUniqueIdentifier();
	}

	
	//A class used to listen for the termination of the current launch, and 
	// run some functions when it is finished. 
	class LaunchTerminationWatcher implements ILaunchesListener2 {
		private ILaunch launch;
		
		public LaunchTerminationWatcher(ILaunch il) {
			launch = il;
		}
		
		public void launchesTerminated(ILaunch[] launches) {
			try {
				for (ILaunch l : launches) {
					/**
					 * Dump samples from the daemon,
					 * shut down the daemon,
					 * activate the OProfile view (open it if it isn't already),
					 * refresh the view (which parses the data/ui model and displays it).
					 */
					if (l.equals(launch)) {
						OprofileCorePlugin.getDefault().getOpcontrolProvider().dumpSamples();
						OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();

						//need to run this in the ui thread otherwise get SWT Exceptions
						// based on concurrency issues
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								OprofileView view = OprofileUiPlugin.getDefault().getOprofileView();
								if (view != null) {
									view.refreshView();
								} else {
									try {
										PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OprofileUiPlugin.ID_OPROFILE_VIEW);
									} catch (PartInitException e) {
										e.printStackTrace();
									}
									OprofileUiPlugin.getDefault().getOprofileView().refreshView();
								}
							}
						});
					}
				}
			} catch (OpcontrolException ignore) {}
		}

		public void launchesAdded(ILaunch[] launches) { /* dont care */}
		public void launchesChanged(ILaunch[] launches) { /* dont care */ }
		public void launchesRemoved(ILaunch[] launches) { /* dont care */ }
	}	
	
}
