/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009, 2012, 2014 Red Hat, Inc. and others
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
 *    Red Hat Inc. - modification of OProfileLaunchConfigurationDelegate to here
 *    Red Hat Inc. - fix #408543
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.launch;

import java.io.File;
import java.net.URI;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.linuxtools.internal.gprof.view.GmonView;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.profiling.ui.CProjectBuildHelpers;
import org.eclipse.linuxtools.profiling.ui.CProjectBuildHelpers.ProjectBuildType;
import org.eclipse.linuxtools.profiling.ui.MessageDialogSyncedRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


public class GprofLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

    private ILaunchConfiguration config;
    private IProject project;

    /**
     *  Checks if neccessary flags are set in Managed Build/Autotools <br>
     *  If they are not, asks the user if he want's to have them addded. <br>
     *  If so, it enables the option, rebuilds the project and continues with launch </p>
     *
     *  <p> Otherwise by this time the project is already build and it proceeds with launch of the plugin. </p>
     *
     */
    @Override
    public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

        //Save passed variables. useful later.
        this.config = config;
        this.project = getProject();

        //Check pre-requisites, (this means check if the pg flag is set).
        //If it is not and the user does not wish to have them set automatically,
        //then cancle the launch process as there won't a gmon.out anyway.
        if (! preRequisiteCheck()) {
            return;
        }

        IPath exePath = getExePath(config);

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

        //gmon.out is generated here:
        Process process = launcher.execute(exePath, arguments, getEnvironment(config), new Path(workDir.getAbsolutePath()), monitor);

        DebugPlugin.newProcess( launch, process, renderProcessLabel( exePath.toOSString() ) );

    }

    /**
     *   Check if the pg flag was specified in the configuration. <br>
     *   If not, prompt user. <br>
     *   If user wishes to have the option added, add the option and re-build the project. <br>
     */
    private boolean preRequisiteCheck() {

        /* Tests:
         *  [x] C    Managed Executable
         *  [x] C++  Managed Executable
         *
         *  [x] C    Autotools
         *  [x] C++  Autotools
         *
         *  2014.07.09 All Retested after update to shared api.
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
        String optionId = "cflags-gprof"; //$NON-NLS-1$
        //See if option was checked.
        if (CProjectBuildHelpers.isOptionCheckedInAutotoolsPrefStore(project, optionId)) {
            return true;
        } else {
            return askUserAboutFlag(optionId, projectBuildType);
        }
    }

    private boolean preRequisiteCheckManagedBuild(ProjectBuildType projectBuildType) {
        //Distinguish between C & C++.
        //Find out what flag should we check for
        String optionId = null;
        if (CProjectBuildHelpers.isCppType(project)) {
            optionId = "gnu.cpp.compiler.option.debugging.gprof"; //$NON-NLS-1$
        } else if (CProjectBuildHelpers.isCType(project)) {
            optionId = "gnu.c.compiler.option.debugging.gprof"; //$NON-NLS-1$
        }

        //Check if flag is checked.
        if ( CProjectBuildHelpers.isOptionCheckedInCDT(project, optionId) ) {
            return true;
        } else
            return askUserAboutFlag(optionId, projectBuildType);
    }

    /**
     *  Tell the user that the required PG flag is not set in the configuration.
     *  Offer to have it automatically checked.
     */
    private boolean askUserAboutFlag(final String optionID, final ProjectBuildType projectBuildType) {

        //Construct Message box that will prompt the user.
        //Content of message will vary depending on project type
        String title = GprofLaunchMessages.GprofMissingFlag_Title;
        String msg = GprofLaunchMessages.GprofMissingFlag_Body_shared;

        if (projectBuildType == ProjectBuildType.AUTO_TOOLS) {
            msg += GprofLaunchMessages.GprofMissingFlag_Body_Autotools;
        } else if (projectBuildType == ProjectBuildType.MANAGED_MAKEFILE) {
            msg += GprofLaunchMessages.GprofMissingFlag_Body_Managed;
        }
        msg += GprofLaunchMessages.GprofMissingFlag_BodyPost_autoAddFlagQuestion;

        //Open Dialogue.
        boolean okPressed = MessageDialogSyncedRunnable.openQuestionSyncedRunnable(title, msg);

        if (okPressed) {
            enablePgOption(optionID, projectBuildType);
            CProjectBuildHelpers.rebuildProject(project);
            return true;
        } else
            return false;
    }

    private void enablePgOption(String optionId, ProjectBuildType projectBuildType) {
        if (projectBuildType == ProjectBuildType.MANAGED_MAKEFILE){
             CProjectBuildHelpers.setOptionInCDT(project, optionId, true);
        }
        else if (projectBuildType == ProjectBuildType.AUTO_TOOLS) {
             CProjectBuildHelpers.setOptionInAutotools(project, optionId, "true"); //$NON-NLS-1$
        }
    }

    /*
     *   Post-Termination.
     */
    //A class used to listen for the termination of the current launch, and
    //run some functions when it is finished.
    class LaunchTerminationWatcher implements ILaunchesListener2 {
        private ILaunch launch;
        private IPath exePath;

        class LaunchTerminationWatcherRunnable implements Runnable {

            private String exePath;
            private String gmonPath;

            public LaunchTerminationWatcherRunnable(String exePath, String gmonPath) {
                this.exePath = exePath;
                this.gmonPath = gmonPath;
            }

            @Override
            public void run() {
                GmonView.displayGprofView(exePath, gmonPath, getProject());
            }
        }

        public LaunchTerminationWatcher(ILaunch il, IPath exePath) {
            launch = il;
            this.exePath = exePath;
        }
        /**
         *  This is ran after the process completes.
         *
         *  <p> It checks if the gmon.out file is available for viewing. <br>
         *  If it's not there, it prompts the user wit the GprofNoGmonDialog <br>
         *  Otherwise it opens the gmon viewer. </p>
         */
        @Override
        public void launchesTerminated(ILaunch[] launches) {

            for (ILaunch l : launches) {
                /**
                 * Retrieve the gmon file, and open profiling results.
                 */
                if (l.equals(launch)) {
                    //need to run this in the ui thread otherwise get SWT Exceptions
                    // based on concurrency issues
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String s = exePath.toOSString();
                                URI workingDirURI = getProject().getLocationURI();
                                RemoteProxyManager rpmgr = RemoteProxyManager.getInstance();
                                IRemoteFileProxy proxy = rpmgr.getFileProxy(getProject());
                                String workingDirPath = proxy.toPath(workingDirURI);
                                // Because we set the working directory on execution to the top-level
                                // project directory, the gmon.out file should be found there
                                String gmonExpected = workingDirPath + "/gmon.out"; //$NON-NLS-1$
                                IFileStore gmonFileStore = proxy.getResource(gmonExpected);
                                if (!gmonFileStore.fetchInfo().exists()) {
                                    Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();

                                   //Missing gmon.out logic:
                                   //    This point is reached if pg flags were not set. (e.g in an unmanaged makefile project.)
                                   //    or PG flag was set but gmon.out could not be found. (e.g chDir was used by the program

                                   //Prompt user about missing gmon.out
                                   GprofNoGmonDialog noGmonDialog = new GprofNoGmonDialog(project, parent);
                                   gmonExpected = noGmonDialog.getGmonExpected();

                                   //See if user specified a path to gmon.
                                   if (gmonExpected == null) {
                                       // If user gmon.out selection was bad, we cancle launch.
                                       return;
                                   } else {
                                       // Otherwise we try to retrive the gmon.out file.
                                       gmonFileStore = proxy.getResource(gmonExpected);
                                   }

                                }

                                // We found gmon.out.  Make sure it was generated after the executable was formed.
                                IFileStore exe = proxy.getResource(exePath.toString());
                                if (exe.fetchInfo().getLastModified() > gmonFileStore.fetchInfo().getLastModified()) {
                                    String title = GprofLaunchMessages.GprofGmonStale_msg;
                                    String message = GprofLaunchMessages.GprofGmonStaleExplanation_msg;
                                    Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                                    MessageDialog.openWarning(parent, title, message);
                                }
                                Display.getDefault().asyncExec(new LaunchTerminationWatcherRunnable(s, gmonExpected));

                            } catch (NullPointerException e) {
                                // Do nothing
                            } catch (CoreException e) {
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
        return GprofLaunch.PLUGIN_ID;
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
    private IPath getExePath(ILaunchConfiguration config) throws CoreException{
        return CDebugUtils.verifyProgramPath( config );
    }

}
