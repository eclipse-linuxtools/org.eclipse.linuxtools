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

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.OprofileDaemonEvent;
import org.eclipse.linuxtools.oprofile.core.OprofileProperties;

/**
 * This class represents an Oprofile launch. It is responsible for starting and configuring
 * the daemon given a launch configuration. [Note that this also means that it will
 * launch a CLaunchConfiguration if one was specified in the config.]
 */
public class OprofileSession {
	OprofileCounter[] _counters;
	LaunchOptions _options;
	String _launchConfig;
	
	public OprofileSession(ILaunchConfiguration config) {
		_counters = OprofileCounter.getCounters(config);

		try {
			_launchConfig = config.getAttribute(LaunchPlugin.ATTR_C_LAUNCH_CONFIG, (String) null);	
		} catch (CoreException ce) {
			_launchConfig = null;	//this wont happen without something ridiculous gone wrong
		}
		
		String imagePath = "all";	//default, but should never go through
		
		try {
			//FIXME: this assumes that project names are always the directory names in the workspace.
			//this assumption may be shaky, but a shallow lookup makes it seem ok

			ILaunchConfiguration claunch = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(_launchConfig);
			String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			imagePath = workspacePath + "/" +
					 	  	   claunch.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,"") + "/" +
					 	  	   claunch.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,"");
		} catch (CoreException e) { }
		
		_options = new LaunchOptions();
		_options.loadConfiguration(config);
		_options.setImage(imagePath);
	}
	
	public ILaunch run () {
		ILaunch prog = null;
		
		try {
			// Shutdown any currently running oprofile session
			OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();
			
			//reset any data from the current session (otherwise causes problems with multiple sessions)
			OprofileCorePlugin.getDefault().getOpcontrolProvider().reset();
			
			ArrayList<OprofileDaemonEvent> events = new ArrayList<OprofileDaemonEvent>();
			for (int i = 0; i < _counters.length; ++i) {
				if (_counters[i].getEnabled())
					events.add(_counters[i].getDaemonEvent());
			}
			OprofileDaemonEvent[] devents = new OprofileDaemonEvent[events.size()];
			events.toArray(devents);
			OprofileCorePlugin.getDefault().getOpcontrolProvider().setupDaemon(_options.getOprofileDaemonOptions(), devents);
			
			// Start the daemon & collection
			OprofileCorePlugin.getDefault().getOpcontrolProvider().startCollection();
		} catch (OpcontrolException oe) {
			String title = OprofileProperties.getString("opcontrolProvider.error.dialog.title"); //$NON-NLS-1$
			String msg = OprofileProperties.getString("opcontrolProvider.error.dialog.message"); //$NON-NLS-1$
			ErrorDialog.openError(null /* parent shell */, title, msg, oe.getStatus());
		}
		
		// Run c/c++ launch config, if defined
		if (_launchConfig != null) {
			ILaunchConfiguration claunch = null;
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			try {
				claunch = manager.getLaunchConfiguration(_launchConfig);
			} catch (CoreException ce) {
				claunch = null;
			}
			
			if (claunch != null) {
				try {
					// TODO: Progress monitor
					prog = claunch.launch(ILaunchManager.RUN_MODE, null);
				} catch (CoreException ce) {
				}
			}
		}
		
		return prog;
	}

//	private Process _runCommand(String[] cmdArray) {
//		Process p = null;
//		try {
//			p = Runtime.getRuntime().exec(cmdArray);
//		} catch (IOException ioe) {
//			if (p != null)
//				p.destroy();
//			p = null;
//		}
//		
//		return p;
//	}
}
