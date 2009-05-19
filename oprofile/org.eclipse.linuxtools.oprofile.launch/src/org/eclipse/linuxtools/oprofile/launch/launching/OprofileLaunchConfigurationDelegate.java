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
package org.eclipse.linuxtools.oprofile.launch.launching;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.view.OprofileView;
import org.eclipse.linuxtools.oprofile.ui.view.OprofileViewSaveDefaultSessionAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OprofileLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//FIXME: this assumes that project names are always the directory names in the workspace.
		//this assumption may be wrong, but a shallow lookup seems ok
		String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		String imagePath = workspacePath
				+ Path.SEPARATOR
				+ config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "") //$NON-NLS-1$
				+ Path.SEPARATOR
				+ config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""); //$NON-NLS-1$
		
		LaunchOptions options = new LaunchOptions();		//default options created in the constructor
		options.loadConfiguration(config);
		options.setBinaryImage(imagePath);

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
		
		//determine if this is a manual launch or automated launch
		boolean manualProfile = config.getAttribute(OprofileLaunchPlugin.ATTR_MANUAL_PROFILE, false);

		if (!manualProfile) {
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
				OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
				return;
			}
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
				wd = new File( System.getProperty( "user.home", "." ) ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String arguments[] = getProgramArgumentsArray( config );
			ArrayList<String> command = new ArrayList<String>( 1 + arguments.length );
			command.add( exePath.toOSString() );
			command.addAll( Arrays.asList( arguments ) );
			String[] commandArray = (String[])command.toArray( new String[command.size()] );
			boolean usePty = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			Process process;
			process = exec( commandArray, getEnvironment( config ), wd, usePty );
			DebugPlugin.newProcess( launch, process, renderProcessLabel( commandArray[0] ) );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!manualProfile) {
			//add a listener for termination of the launch
			ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
			lmgr.addLaunchListener(new LaunchTerminationWatcher(launch));
		} else {
			final LaunchOptions fOptions = options;
			final OprofileDaemonEvent[] fDaemonEvents = daemonEvents;
			Display.getDefault().asyncExec(new Runnable() { 
				public void run() {
					//TODO: have a initialization dialog to do reset and setupDaemon?
					// using a progress dialog, can't abort the launch if there's an exception..
					try {
						OprofileCorePlugin.getDefault().getOpcontrolProvider().reset();
						OprofileCorePlugin.getDefault().getOpcontrolProvider().setupDaemon(fOptions.getOprofileDaemonOptions(), fDaemonEvents);
					} catch (OpcontrolException oe) {
						OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
						return;
					}
					
					//manual oprofile control dialog
					OprofiledControlDialog dlg = new OprofiledControlDialog();
					dlg.open();

					//progress dialog for ensuring the daemon is shut down
					IRunnableWithProgress refreshRunner = new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask(OprofileLaunchMessages.getString("oprofiledcontroldialog.post.stopdaemon"), 1); //$NON-NLS-1$
							try {
								OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();
							} catch (OpcontrolException e) {
//								e.printStackTrace();
							}
							monitor.worked(1);
							monitor.done();
						}
					};
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
					try {
						dialog.run(true, false, refreshRunner);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} 
			});
		}
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
	
	/**
	 * A custom dialog box to display the oprofiled log file.
	 */
	class OprofiledControlDialog extends MessageDialog {
		Button _startDaemonButton;
		Button _stopDaemonButton;
		Button _refreshViewButton;
		Button _resetSessionButton;
		Button _saveSessionButton;
		List _feedbackList;
		
		public OprofiledControlDialog () {
			super(new Shell(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()), OprofileLaunchMessages.getString("oprofiledcontroldialog.title"), null, null, MessageDialog.NONE, new String[] { IDialogConstants.OK_LABEL }, 0); //$NON-NLS-1$
		
			//override styles; makes the dialog non-modal
			setShellStyle(SWT.CLOSE | SWT.TITLE );
		}
		
		@Override
	    protected Control createCustomArea(Composite parent) {
			Composite area = new Composite(parent, 0);
			Layout layout = new GridLayout(5, true);
			GridData gd = new GridData();
			
			area.setLayout(layout);
			area.setLayoutData(gd);
			
			/**
			 * TODO:
			 * have the 4 buttons in a single row with a status text box above/below for feedback?
			 */

			Button startDaemonButton = new Button(area, SWT.PUSH);
			startDaemonButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			startDaemonButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.startdaemon")); //$NON-NLS-1$
			startDaemonButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					try {
						OprofileCorePlugin.getDefault().getOpcontrolProvider().startCollection();
						_startDaemonButton.setEnabled(false);
						_stopDaemonButton.setEnabled(true);
						_refreshViewButton.setEnabled(true);
						_resetSessionButton.setEnabled(true);
						_saveSessionButton.setEnabled(true);
					} catch (OpcontrolException oe) {
						//disable buttons, notify user of error
						disableAllButtons();
						OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
					}
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.startdaemon")); //$NON-NLS-1$
				}});
			_startDaemonButton = startDaemonButton;
			
			Button stopDaemonButton = new Button(area, SWT.PUSH);
			stopDaemonButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			stopDaemonButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.stopdaemon")); //$NON-NLS-1$
			stopDaemonButton.setEnabled(false);		//disabled at start
			stopDaemonButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					try {
						OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();
						_startDaemonButton.setEnabled(true);
						_stopDaemonButton.setEnabled(false);
					} catch (OpcontrolException oe) {
						//disable buttons, notify user of error
						disableAllButtons();
						OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
					}
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.stopdaemon")); //$NON-NLS-1$
				}});
			_stopDaemonButton = stopDaemonButton;
			
			Button saveSessionButton = new Button(area, SWT.PUSH);
			saveSessionButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			saveSessionButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.savesession")); //$NON-NLS-1$
			saveSessionButton.setEnabled(false);		//disabled at start
			saveSessionButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.save")); //$NON-NLS-1$
					OprofileViewSaveDefaultSessionAction hack = new OprofileViewSaveDefaultSessionAction();
					hack.run();
				}});
			_saveSessionButton = saveSessionButton;
			
			Button resetSessionButton = new Button(area, SWT.PUSH);
			resetSessionButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			resetSessionButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.resetsession")); //$NON-NLS-1$
			resetSessionButton.setEnabled(false);		//disabled at start
			resetSessionButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					try {
						OprofileCorePlugin.getDefault().getOpcontrolProvider().reset();
					} catch (OpcontrolException oe) {
						//disable buttons, notify user of error
						disableAllButtons();
						OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
					}
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.reset")); //$NON-NLS-1$
				}});
			_resetSessionButton = resetSessionButton;
			
			Button refreshViewButton = new Button(area, SWT.PUSH);
			refreshViewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			refreshViewButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.refreshview")); //$NON-NLS-1$
			refreshViewButton.setEnabled(false);		//disabled at start
			refreshViewButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.dumpsamples")); //$NON-NLS-1$
					try {
						OprofileCorePlugin.getDefault().getOpcontrolProvider().dumpSamples();
					} catch (OpcontrolException oe) {
						//no error in this case; the user might refresh when the daemon isnt running
					}

					refreshOprofileView();
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.refreshed")); //$NON-NLS-1$
				}});
			_refreshViewButton = refreshViewButton;
			

			List feedback = new List(area, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
			feedback.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
			feedback.add(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.init")); //$NON-NLS-1$
			_feedbackList = feedback;
			
	        return area;
	    }
		
		//helper function
		private void disableAllButtons() {
			_startDaemonButton.setEnabled(false);
			_stopDaemonButton.setEnabled(false);
			_refreshViewButton.setEnabled(false);
			_resetSessionButton.setEnabled(false);
			_saveSessionButton.setEnabled(false);
		}
		
		//a little hack to get the list to auto scroll to the newly added item
		private void addToFeedbackList(String s) {
			_feedbackList.add(s,0);
			_feedbackList.setTopIndex(0);
		}
	}
	
	//Helper function to refresh the oprofile view. Opens and focuses the view 
	// if it isn't already. 
	private void refreshOprofileView() {
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
}
