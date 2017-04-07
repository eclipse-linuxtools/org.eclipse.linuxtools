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
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile.OprofileProject;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.swt.widgets.Display;

public class OprofileLaunchConfigurationDelegate extends AbstractOprofileLaunchConfigurationDelegate {

    @Override
	protected boolean preExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch) {
		// set up and launch the oprofile daemon
		IProject project = getProject();
		// Set current project to allow using the oprofile path that
		// was chosen for the project
		Oprofile.OprofileProject.setProject(project);

		if (!oprofileStatus()) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", null); //$NON-NLS-1$
			return false;
		}

		// add a listener for termination of the launch prior to execution of launch
		ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
		lmgr.addLaunchListener(new LaunchTerminationWatcher(launch, options.getExecutionsNumber()));
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
        @Override
		public void launchesTerminated(ILaunch[] launches) {
			for (ILaunch l : launches) {
				/**
				 * Dump samples from the daemon, shut down the daemon, activate the OProfile
				 * view (open it if it isn't already), refresh the view (which parses the
				 * data/ui model and displays it).
				 */
				if (l.equals(launch) && l.getProcesses().length == executions) {
					// need to run this in the ui thread otherwise get SWT Exceptions
					// based on concurrency issues
					if (!OprofileProject.getProfilingBinary().equals(OprofileProject.OCOUNT_BINARY)) {
						Display.getDefault().syncExec(() -> refreshOprofileView());
					}
				}
			}
		}
        @Override
        public void launchesAdded(ILaunch[] launches) { /* dont care */}
        @Override
        public void launchesChanged(ILaunch[] launches) { /* dont care */ }
        @Override
        public void launchesRemoved(ILaunch[] launches) { /* dont care */ }
    }

}
