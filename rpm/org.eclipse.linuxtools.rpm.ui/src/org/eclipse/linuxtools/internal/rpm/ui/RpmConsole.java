/*******************************************************************************
 * Copyright (c) 2010, 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.ui.RPMExportOperation;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * RpmConsole is used to output rpm/rpmbuild output.
 *
 */
public class RpmConsole extends IOConsole {

    /** Id of this console. */
    public static final String ID = "rpmbuild"; //$NON-NLS-1$
    private final RPMProject rpmProject;
    private final Set<RpmConsoleObserver> activeConsoleObservers = new HashSet<>();
    private final Set<RpmConsoleObserver> inactiveConsoleObservers = new HashSet<>();
    private RPMExportOperation currentJob;
    private boolean running = false;

    private JobChangeAdapter jobWatcher = new JobChangeAdapter() {

        @Override
        public void aboutToRun(IJobChangeEvent event) {
            running = true;
            notifyConsoleObservers();
        }

        @Override
        public void done(IJobChangeEvent event) {
            running = false;
            notifyConsoleObservers();
        }
    };

    /**
     * A listener for responding to the run state of a build operation.
     */
    public static interface RpmConsoleObserver {
        /**
         * This method is called whenever the run state of the build operation changes.
         * @param running Whether or not the build is currently running.
         */
        void runningStateChanged(boolean running);
    }

    /**
     * Returns a reference to the console that is for the given {@link RPMProject}. If such
     * a console does not yet exist, it will be created.
     *
     * @param rpmProject The project this console will be for. Must not be <code>null</code>.
     * @return A console instance.
     */
    public static RpmConsole findConsole(RPMProject rpmProject) {
        RpmConsole ret = null;
        for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
                .getConsoles()) {
            if (cons instanceof RpmConsole &&
                    ((RpmConsole) cons).rpmProject.getSpecFile().getProject().getName()
                    .equals(rpmProject.getSpecFile().getProject().getName())) {
                ret = (RpmConsole) cons;
            }
        }
        // no existing console, create new one
        if (ret == null) {
            ret = new RpmConsole(rpmProject);
            ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {ret});
        }
        return ret;
    }

    /**
     * Creates the console.
     *
     * @param rpmProject
     *            The RPM project to use.
     */
    public RpmConsole(RPMProject rpmProject) {
        super(ID+'('+rpmProject.getSpecFile().getProject().getName()+')', ID, null, true);
        this.rpmProject = rpmProject;
    }

    /**
     * Returns the spec file for this rpm project.
     *
     * @return The spec file.
     */
    public IResource getSpecfile() {
        return rpmProject.getSpecFile();
    }

    /**
     * Initializes a listener that will respond to this console's run state.
     * @param observer The listener to initialize.
     */
    public synchronized void addConsoleObserver(RpmConsoleObserver observer) {
        activeConsoleObservers.add(observer);
        observer.runningStateChanged(running);
    }

    /**
     * Stop a previously-added listener from receiving any more events.
     * @param observer The listener to remove.
     */
    public synchronized void removeConsoleObserver(RpmConsoleObserver observer) {
        inactiveConsoleObservers.add(observer);
    }

    private synchronized void notifyConsoleObservers() {
        for (RpmConsoleObserver observer : inactiveConsoleObservers) {
            activeConsoleObservers.remove(observer);
        }
        inactiveConsoleObservers.clear();
        for (RpmConsoleObserver observer : activeConsoleObservers) {
            observer.runningStateChanged(running);
        }
    }

    /**
     * Cancels the currently-running job.
     */
    public synchronized void stop() {
        if (currentJob != null && running) {
            currentJob.cancel();
        }
    }

    /**
     * Tells this console to watch the run state of the provided job, and
     * prepares the console to receive the job's output (ie is activated & cleared).
     * If this console is already running a job, it will be stopped.
     * @param rpmJob The job to watch. If <code>null</code> or equal to the currently
     * running job, this method will have no effect.
     * @return This console's output stream where the linked job may send its output,
     * or <code>null</code> if an invalid job is provided.
     */
    public synchronized IOConsoleOutputStream linkJob(RPMExportOperation rpmJob) {
        if (rpmJob == null || rpmJob.equals(currentJob)) {
            return null;
        }
        if (currentJob != null) {
            if (currentJob.getResult() == null) {
                showErrorDialog();
                return null;
            }
            currentJob.removeJobChangeListener(jobWatcher);
            clearConsole();
        }
        activate();
        currentJob = rpmJob;
        currentJob.addJobChangeListener(jobWatcher);
        running = currentJob.getState() == Job.RUNNING;
        notifyConsoleObservers();
        return newOutputStream();
    }

    @Override
    protected synchronized void dispose() {
        activeConsoleObservers.clear();
        inactiveConsoleObservers.clear();
        super.dispose();
    }

    private void showErrorDialog() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(),
                Messages.getString("RPMConsole.OperationRunningTitle"), //$NON-NLS-1$
                Messages.getString("RPMConsole.OperationRunningMessage")); //$NON-NLS-1$
            }
        });
    }
}
