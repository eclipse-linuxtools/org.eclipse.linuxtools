/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc. and others
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
package org.eclipse.linuxtools.internal.oprofile.launch.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.internal.oprofile.ui.view.OprofileView;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractOprofileLaunchConfigurationDelegate extends ProfileLaunchConfigurationDelegate {
	
	protected ILaunchConfiguration config;
	
	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		this.config = config;
		LaunchOptions options = new LaunchOptions();		//default options created in the constructor
		options.loadConfiguration(config);
		IPath exePath = CDebugUtils.verifyProgramPath( config );
		options.setBinaryImage(exePath.toOSString());

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
		
		if (!preExec(options, daemonEvents)) return;

		/* 
		 * this code written by QNX Software Systems and others and was 
		 * originally in the CDT under LocalCDILaunchDelegate::RunLocalApplication
		 */
		//set up and launch the local c/c++ program
		
			File wd = getWorkingDirectory( config );
			if ( wd == null ) {
				wd = new File( System.getProperty( "user.home", "." ) ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String arguments[] = getProgramArgumentsArray( config );
			ArrayList<String> command = new ArrayList<String>( 1 + arguments.length );
			command.add( exePath.toOSString() );
			command.addAll( Arrays.asList( arguments ) );
			String[] commandArray = command.toArray( new String[command.size()] );
			boolean usePty = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			Process process;
			process = execute( commandArray, getEnvironment( config ), wd, usePty );
			DebugPlugin.newProcess( launch, process, renderProcessLabel( commandArray[0] ) );

			postExec(options, daemonEvents, launch, process);
	}
	
	protected abstract boolean preExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents);

	protected abstract void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch, Process process);

	@Override
	protected String getPluginID() {
		return OprofileLaunchPlugin.getUniqueIdentifier();
	}
	
	//Helper function to refresh the oprofile view. Opens and focuses the view 
	// if it isn't already. 
	protected void refreshOprofileView() {
		OprofileView view = OprofileUiPlugin.getDefault().getOprofileView();
		if (view != null) {
			view.refreshView();
		} else {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OprofileUiPlugin.ID_OPROFILE_VIEW);
			} catch (PartInitException e2) {
				e2.printStackTrace();
			}
			OprofileUiPlugin.getDefault().getOprofileView().refreshView();
		}
	}
	
	/* all these functions exist to be overridden by the test class in order to allow launch testing */
	
	protected void oprofileShutdown() throws OpcontrolException {
		OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();	
	}
	
	protected void oprofileReset() throws OpcontrolException {
		OprofileCorePlugin.getDefault().getOpcontrolProvider().reset();		
	}
	
	protected void oprofileSetupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) throws OpcontrolException {
		OprofileCorePlugin.getDefault().getOpcontrolProvider().setupDaemon(options, events);		
	}

	protected void oprofileStartCollection() throws OpcontrolException {
		OprofileCorePlugin.getDefault().getOpcontrolProvider().startCollection();
	}
	
	protected void oprofileDumpSamples() throws OpcontrolException {
		OprofileCorePlugin.getDefault().getOpcontrolProvider().dumpSamples();
	}
	
	/**
	 * Runs opcontrol --help. Returns true if there was any output, false 
	 * otherwise. Return value can be used to tell if the user successfully
	 * entered a password.
	 * @return true if opcontrol --help was run correctly. False otherwise
	 * @throws OpcontrolException
	 */
	protected boolean oprofileStatus() throws OpcontrolException {
		return OprofileCorePlugin.getDefault().getOpcontrolProvider().status();
	}
	
	protected IProject getProject(){
		try{
			IProject project = CDebugUtils.verifyCProject(config).getProject();
			return project;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
