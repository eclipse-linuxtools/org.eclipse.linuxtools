/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009, 2012 Red Hat, Inc. and others
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
 *    Red Hat Inc. - Added automatic enablement of options if they are not set
 *    Red Hat Inc. - modification of OProfileLaunchConfigurationDelegate to here
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.launch;

import java.io.File;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.gcov.parser.CovManager;
import org.eclipse.linuxtools.internal.gcov.view.CovView;
import org.eclipse.linuxtools.internal.gcov.view.annotatedsource.GcovAnnotationModelTracker;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.profiling.ui.CProjectBuildHelpers;
import org.eclipse.linuxtools.profiling.ui.CProjectBuildHelpers.ProjectBuildType;
import org.eclipse.linuxtools.profiling.ui.MessageDialogSyncedRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class GcovLaunchConfigurationDelegate extends AbstractCLaunchDelegate {
    protected ILaunchConfiguration config;
    protected IProject project; //used in many places.

    @Override
    public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        this.config = config;
        this.project = getProject();
        IPath exePath = getExePath(config);

        //If pre-requisites fail, (e.g flag is not set & user does not wish to have them set automatically),
        //then cancle the launch process.
        if (! preRequisiteCheck()) {
           return;
        }

        /*
         * this code written by QNX Software Systems and others and was
         * originally in the CDT under LocalCDILaunchDelegate::RunLocalApplication
         */
        //set up and launch the local c/c++ program
        IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(getProject());

        File workDir = getWorkingDirectory(config);
        if (workDir == null) {
            workDir = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String arguments[] = getProgramArgumentsArray( config );

        //add a listener for termination of the launch
        ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
        lmgr.addLaunchListener(new LaunchTerminationWatcher(launch, exePath));

       //gcov.out is generated here:
        Process process = launcher.execute(exePath, arguments, getEnvironment(config), new Path(workDir.getAbsolutePath()), monitor);
        DebugPlugin.newProcess( launch, process, renderProcessLabel( exePath.toOSString() ) );

    }

    /**
     * <h1>Prerequiste check for gcov option.</h1>
     *   <p>
     *   Check if the pg flag was specified in the configuration. <br>
     *   If not, prompt user. <br>
     *   If user wishes to have the option added, add the option and re-build the project. <br>
     *   </p>
     */
    private boolean preRequisiteCheck() {

        /* Test notes:
         * CDT:
         *      [tested] Cpp
         *      [tested] C
         * Autotools
         *      [tested] C
         *      [tested] Cpp (with Jeff's fix.)
         *
         * Last tested:
         * 2014.07.25
         */

        //Different project types are handled differently. (see this enum for details)
        ProjectBuildType projectBuildType = CProjectBuildHelpers.getProjectType(project);

        if (projectBuildType == ProjectBuildType.AUTO_TOOLS) {
            return preRequisiteCheckAutotools(projectBuildType);

        } else if (projectBuildType == ProjectBuildType.MANAGED_MAKEFILE) {
            return preRequisiteCheckManagedBuild(projectBuildType);
        }
        else {
            //This is a Makefile project or a project type that we have not checked.
            //we don't check flag in this case.
            //(there is room for expanding to check other project types thou).
            //User will be given a generic prompt in case gmon.out is not produced.
            return true;
           }
    }

    private boolean preRequisiteCheckAutotools(ProjectBuildType projectBuildType) {
        String optionId = "cflags-gcov"; //$NON-NLS-1$
        //See if option was checked.
        if (CProjectBuildHelpers.isOptionCheckedInAutotoolsPrefStore(project, optionId)) {
            return true;
        } else {
            return askUserAboutFlag(optionId, projectBuildType);
        }
    }

    private boolean preRequisiteCheckManagedBuild(ProjectBuildType projectBuildType) {
        // Distinguish between C & C++.
        // Find out what flag should we check for
        String optionId = null;
        if (CProjectBuildHelpers.isCppType(project)) {
            optionId = "gnu.cpp.compiler.option.debugging.codecov";    //$NON-NLS-1$
        } else if (CProjectBuildHelpers.isCType(project)) {
            optionId = "gnu.c.compiler.option.debugging.codecov";    //$NON-NLS-1$
        }

        // Check that both flags are checked.
        if ( CProjectBuildHelpers.isOptionCheckedInCDT(project, optionId) ) {
            return true;
        } else {
            //If either one of the flags is not checked, check it.
            return askUserAboutFlag(optionId, projectBuildType);
        }
    }

    /**
     * <h1>Ask user for flags.</h1>
     * Tell the user that the required gcov flag is not set in the configuration.
     *
     * <p> Offer to have it automatically checked. </p>
     */
    private boolean askUserAboutFlag(final String optionId, final ProjectBuildType projectBuildType) {

        //Construct Message box that will prompt the user.
        //Content of message will vary depending on project type
        String title = GcovLaunchMessages.GcovMissingFlag_Title;
        String msg = GcovLaunchMessages.GcovMissingFlag_MainMsg;

        if (projectBuildType == ProjectBuildType.AUTO_TOOLS) {
            msg += GcovLaunchMessages.GcovMissingFlag_AutotoolsInfo;
        } else if (projectBuildType == ProjectBuildType.MANAGED_MAKEFILE) {
            msg += GcovLaunchMessages.GcovMissingFlag_CDTInfo;
        }
        msg += GcovLaunchMessages.GcovMissingFlag_PostQuestion;

        //Open Dialogue.
        boolean okPressed = MessageDialogSyncedRunnable.openQuestionSyncedRunnable(title, msg);

        if (okPressed) {
            enableOption(optionId, projectBuildType);
            CProjectBuildHelpers.rebuildProject(project);
            return true;
        } else {
            return false;
        }
    }

    private void enableOption(String optionId, ProjectBuildType projectBuildType) {
        if (projectBuildType == ProjectBuildType.MANAGED_MAKEFILE)
             CProjectBuildHelpers.setOptionInCDT(project, optionId, true);
        else if (projectBuildType == ProjectBuildType.AUTO_TOOLS) {
             CProjectBuildHelpers.setOptionInAutotools(project, optionId, "true"); //$NON-NLS-1$
        }
    }

    //A class used to listen for the termination of the current launch, and
    // run some functions when it is finished.
    class LaunchTerminationWatcher implements ILaunchesListener2 {
        private ILaunch launch;
        private IPath exePath;
        public LaunchTerminationWatcher(ILaunch il, IPath exePath) {
            launch = il;
            this.exePath = exePath;
        }
        @Override
        public void launchesTerminated(ILaunch[] launches) {

            for (ILaunch l : launches) {
                /**
                 * Dump samples from the daemon,
                 * shut down the daemon,
                 * activate the OProfile view (open it if it isn't already),
                 * refresh the view (which parses the data/ui model and displays it).
                 */
                if (l.equals(launch)) {
                    //need to run this in the ui thread otherwise get SWT Exceptions
                    // based on concurrency issues
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            String s = exePath.toOSString();
                            CovManager cvrgeMnger = new CovManager(s, getProject());

                            try {
                                List<String> gcdaPaths = cvrgeMnger.getGCDALocations();
                                if (gcdaPaths.isEmpty()) {
                                    String title = GcovLaunchMessages.GcovCompilerOptions_msg;
                                    String message = GcovLaunchMessages.GcovCompileAgain_msg;
                                    Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                                    MessageDialog.openWarning(parent, title, message);
                                }
                                CovView.displayCovResults(s, null);
                                GcovAnnotationModelTracker.getInstance().addProject(getProject(), exePath);
                                GcovAnnotationModelTracker.getInstance().annotateAllCEditors();
                            } catch (InterruptedException e) {
                                // Do nothing
                            }
                        }
                    });
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

    @Override
    protected String getPluginID() {
        return GcovLaunchPlugin.PLUGIN_ID;
    }

    /* all these functions exist to be overridden by the test class in order to allow launch testing */

    private IProject getProject(){
        try{
            return CDebugUtils.verifyCProject(config).getProject();
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }
     /**
      *
      * Return the exe path of the binary to be profiled.
      * @param config
      * @return the exe path of the binary stored in the configuration
      * @throws CoreException
      * @since 1.1
      */
    private static IPath getExePath(ILaunchConfiguration config) throws CoreException{
        return CDebugUtils.verifyProgramPath( config );
    }

}
