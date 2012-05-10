/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.launch.launching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;
import org.eclipse.swt.widgets.Display;

public class OprofileLaunchConfigurationDelegate extends AbstractOprofileLaunchConfigurationDelegate {

	private static final int SUDO_TIMEOUT = 5000;
	private static final String OPCONTROL_EXECUTABLE = "opcontrol";

	@Override
	protected boolean preExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents) {
		//set up and launch the oprofile daemon
		try {
			IProject project = getProject();

			//check if user has NOPASSWD sudo permission for opcontrol
			//if the Linux Tools Path property was changed
			if(project != null && !LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project).equals("")){
				if(!hasPermissions(project)){
					throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolSudo", null));
				}
			}
			// Set current project to allow using the oprofile path that
			// was chosen for the project 
			Oprofile.OprofileProject.setProject(project);
			
			if (!oprofileStatus())
				return false;
			
			//kill the daemon (it shouldn't be running already, but to be safe)
			oprofileShutdown();
			
			//reset data from the (possibly) existing default session, 
			// otherwise multiple runs will combine samples and results
			// won't make much sense
			oprofileReset();
			
			//setup the events and other parameters
			oprofileSetupDaemon(options.getOprofileDaemonOptions(), daemonEvents);
			
			//start the daemon & collection of samples 
			//note: since the daemon is only profiling for the specific image we told 
			// it to, no matter to start the daemon before the binary itself is run
			oprofileStartCollection();
		} catch (OpcontrolException oe) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
			return false;
		}
		return true;
	}


	/**
	 * Checks if the user has permissions to execute opcontrol as root without providing password
	 * and if opcontrol exists in the indicated path
	 * @param project
	 * @return
	 */
	public static boolean hasPermissions(IProject project) {
		String linuxtoolsPath = LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project);

		try {
			String opcontrolPath = null;
			if(linuxtoolsPath.equals("")){
				opcontrolPath = RuntimeProcessFactory.getFactory().whichCommand(OPCONTROL_EXECUTABLE, project);
			} else if(linuxtoolsPath.endsWith("/")){
				opcontrolPath = linuxtoolsPath + "opcontrol";
			} else {
				opcontrolPath = linuxtoolsPath + "/opcontrol";
			}

			if(opcontrolPath.equals("")){
				return false;
			}

			// Check if user has sudo permissions without password by running sudo -l.
			final Process p = RuntimeProcessFactory.getFactory().exec("sudo -l", project);
			final StringBuffer buffer = new StringBuffer();

			if(p == null){
				return false;
			}

			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String s = null;
						while ((s = input.readLine()) != null) {
							buffer.append(s);
							buffer.append('\n');
						}
						p.waitFor();
						p.destroy();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};

		     t.start();
			 t.join(SUDO_TIMEOUT);

			 String[] sudoLines = buffer.toString().split("\n");
			 for (String s : sudoLines) {
				 if(s.contains(opcontrolPath) && s.contains("NOPASSWD")){
						return true;
				 }
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch, Process process) {
		//add a listener for termination of the launch
		ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
		lmgr.addLaunchListener(new LaunchTerminationWatcher(launch));
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
						oprofileDumpSamples();
						oprofileShutdown();

						//need to run this in the ui thread otherwise get SWT Exceptions
						// based on concurrency issues
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								refreshOprofileView();
							}
						});
					}
				}
			} catch (OpcontrolException oe) {
				OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
			}
		}
		public void launchesAdded(ILaunch[] launches) { /* dont care */}
		public void launchesChanged(ILaunch[] launches) { /* dont care */ }
		public void launchesRemoved(ILaunch[] launches) { /* dont care */ }
	}

	@Override
	public String generateCommand(ILaunchConfiguration config) { return null; /* dont care */}
}
