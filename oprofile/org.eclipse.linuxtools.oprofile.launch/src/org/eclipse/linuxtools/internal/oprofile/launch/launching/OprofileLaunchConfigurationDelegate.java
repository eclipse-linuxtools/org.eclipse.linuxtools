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

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.linuxtools.internal.oprofile.core.IOpcontrolProvider;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile.OprofileProject;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;
import org.eclipse.swt.widgets.Display;

public class OprofileLaunchConfigurationDelegate extends AbstractOprofileLaunchConfigurationDelegate {

	@Override
	protected boolean preExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch) {
		//set up and launch the oprofile daemon
		try {
			IProject project = getProject();
			// Set current project to allow using the oprofile path that
			// was chosen for the project
			Oprofile.OprofileProject.setProject(project);

			if (OprofileProject.getProfilingBinary().equals(OprofileProject.OPCONTROL_BINARY)) {
				//check if user has NOPASSWD sudo permission for opcontrol
				//if the Linux Tools Path property was changed
				if(!LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project).equals("")){
					IOpcontrolProvider provider = OprofileCorePlugin.getDefault().getOpcontrolProvider();
					if (!provider.hasPermissions(project)){
						throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolSudo", null));
					}
				}

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
			}
			//add a listener for termination of the launch prior to execution of launch
			ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
			lmgr.addLaunchListener(new LaunchTerminationWatcher(launch, options.getExecutionsNumber()));
		} catch (OpcontrolException oe) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	@Override
	protected void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, Process process) {
		// do nothing here since the termination listener already registered will handle everything needed
	}

	//A class used to listen for the termination of the current launch, and
	// run some functions when it is finished.
	class LaunchTerminationWatcher implements ILaunchesListener2 {
		private ILaunch launch;
		private int executions;
		public LaunchTerminationWatcher(ILaunch il, int executions) {
			launch = il;
			this.executions = executions;
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
					if (l.equals(launch) && l.getProcesses().length == executions) {
						if (OprofileProject.getProfilingBinary().equals(OprofileProject.OPCONTROL_BINARY)) {
							oprofileDumpSamples();
							oprofileShutdown();
						}

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

}
