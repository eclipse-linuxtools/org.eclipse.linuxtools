/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.system.SystemProfileView;
import org.eclipse.swt.widgets.Display;

/**
 * This is the delegation class for the profile launcher. It handles the request to
 * run the launch configuration.
 */
public class ProfileLaunchConfigurationDelegate extends LaunchConfigurationDelegate
{
	/**
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch,
									IProgressMonitor monitor) throws CoreException
	{
		OprofileSession session = new OprofileSession(config);
		ILaunch prog = session.run();

		//add a listener for termination of the launch
		ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
		lmgr.addLaunchListener(new LaunchTerminationWatcher(prog));
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
				for (ILaunch l : launches)
				{
					//when the launch terminates, dump the samples and shut down oprofile
					if (l.equals(launch)) {
						OprofileCorePlugin.getDefault().getOpcontrolProvider().dumpSamples();
						OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();

						//need to run this in the ui thread otherwise get SWT Exceptions
						// based on concurrency issues
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								//stolen from AbstractOprofileUiAction::_updateViews() 
								SystemProfileView view = OprofileUiPlugin.getDefault().getSystemProfileView();
								if (view != null) {
									view.refreshView();
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
