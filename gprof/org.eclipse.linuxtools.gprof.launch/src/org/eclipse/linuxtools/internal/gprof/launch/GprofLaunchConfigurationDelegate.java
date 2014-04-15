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
 *    Red Hat Inc. - modification of OProfileLaunchConfigurationDelegate to here
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.launch;

import java.io.File;
import java.net.URI;

import org.eclipse.cdt.debug.core.CDebugUtils;
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
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class GprofLaunchConfigurationDelegate extends ProfileLaunchConfigurationDelegate {
    private ILaunchConfiguration config;

    @Override
    public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        this.config = config;
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

        Process process = launcher.execute(exePath, arguments, getEnvironment(config), new Path(workDir.getAbsolutePath()), monitor);

        DebugPlugin.newProcess( launch, process, renderProcessLabel( exePath.toOSString() ) );

    }

    //A class used to listen for the termination of the current launch, and
    // run some functions when it is finished.
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
                                IFileStore f = proxy.getResource(gmonExpected);
                                if (!f.fetchInfo().exists()) {
                                    Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                                    GprofNoGmonDialog dialog = new GprofNoGmonDialog(parent, getProject());
                                    if (dialog.open() != 0) {
                                        gmonExpected = dialog.getPathToGmon();
                                        f = proxy.getResource(gmonExpected);
                                    } else {
                                        return;
                                    }
                                }

                                // We found gmon.out.  Make sure it was generated after the executable was formed.
                                IFileStore exe = proxy.getResource(exePath.toString());
                                if (exe.fetchInfo().getLastModified() > f.fetchInfo().getLastModified()) {
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
