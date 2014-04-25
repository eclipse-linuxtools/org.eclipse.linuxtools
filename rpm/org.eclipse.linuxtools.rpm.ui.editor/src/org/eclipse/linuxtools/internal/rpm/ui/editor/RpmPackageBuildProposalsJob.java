/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.core.utils.BufferedProcessInputStream;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.osgi.util.NLS;

public final class RpmPackageBuildProposalsJob extends Job {

    private RpmPackageBuildProposalsJob(String name) {
        super(name);
        this.addJobChangeListener(updateFinishedListener);
    }

    private static RpmPackageBuildProposalsJob job = null;

    private Object updatingLock = false;
    private boolean updating = false;

    private IJobChangeListener updateFinishedListener = new JobChangeAdapter(){

        @Override
        public void done(IJobChangeEvent event) {
            synchronized (updatingLock) {
                updating = false;
                updatingLock.notifyAll();
            }
        }
    };

    /**
     * If the updates thread has not finished updating this function blocks
     * the current thread until that job is done.
     */
    public static void waitForUpdates (){
        if (job != null){
            try {
                synchronized (job.updatingLock){
                    if (job.updating){
                        job.updatingLock.wait();
                    }
                }
            } catch (InterruptedException e) {}
        }
    }

    protected static final IEclipsePreferences.IPreferenceChangeListener PROPERTY_LISTENER = new IEclipsePreferences.IPreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent event) {
            if (event.getKey().equals(PreferenceConstants.P_CURRENT_RPMTOOLS)) {
                update();
            }
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        return retrievePackageList(monitor);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.jobs.Job#shouldSchedule()
     */
    @Override
    public boolean shouldSchedule() {
        return equals(job);
    }

    /**
     * Run the Job if it's needed according with the configuration set in the
     * preference page.
     */
    public static void update() {
        boolean runJob = false;
        // Today's date
        Date today = new Date();
        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        if (preferences
                .getBoolean(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD, PreferenceConstants.DP_RPM_LIST_BACKGROUND_BUILD)) {
            int period = preferences
                    .getInt(PreferenceConstants.P_RPM_LIST_BUILD_PERIOD, PreferenceConstants.DP_RPM_LIST_BUILD_PERIOD);
            // each time that the plugin is loaded.
            if (period == 1) {
                runJob = true;
            } else {
                long lastBuildTime = preferences
                        .getLong(PreferenceConstants.P_RPM_LIST_LAST_BUILD, PreferenceConstants.DP_RPM_LIST_LAST_BUILD);
                if (lastBuildTime == 0) {
                    runJob = true;
                } else {
                    long interval = (today.getTime() - lastBuildTime)
                            / (1000 * 60 * 60 * 24);
                    // run the job once a week
                    if (period == 2 && interval >= 7) {
                        runJob = true;
                    // run the job once a month
                    } else if (period == 3 && interval >= 30) {
                        runJob = true;
                    }
                }
            }
            if (runJob) {
                if (job == null) {
                    job = new RpmPackageBuildProposalsJob(Messages.RpmPackageBuildProposalsJob_0);
                    job.lockAndSchedule();
                    preferences.putLong(PreferenceConstants.P_RPM_LIST_LAST_BUILD, today
                            .getTime());
                } else {
                    job.cancel();
                    job.lockAndSchedule();
                    preferences.putLong(
                            PreferenceConstants.P_RPM_LIST_LAST_BUILD, today
                                    .getTime());
                }
            }
        } else {
            if (job != null) {
                job.cancel();
                job = null;
            }
        }
    }


    /**
     * Puts the object in the updating state so that any objets
     * requesting information will be made to wait until the update
     * is complete.
     */
    private void lockAndSchedule() {
        synchronized(this.updatingLock){
            this.updating = true;
        }
        this.schedule();
    }

    /**
     * Retrieve the package list
     *
     * @param monitor
     *            to update
     * @return a <code>IStatus</code>
     */
    private IStatus retrievePackageList(IProgressMonitor monitor) {
        String rpmListCmd = Activator.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.P_CURRENT_RPMTOOLS);
        String rpmListFilepath = Activator.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.P_RPM_LIST_FILEPATH);
        File bkupFile = new File(rpmListFilepath + ".bkup"); //$NON-NLS-1$
        try {
            monitor.beginTask(Messages.RpmPackageBuildProposalsJob_1,
                    IProgressMonitor.UNKNOWN);
            if (Utils.fileExist("/bin/sh")) { //$NON-NLS-1$
                BufferedProcessInputStream in = Utils.runCommandToInputStream(
                        "/bin/sh", "-c", rpmListCmd); //$NON-NLS-1$ //$NON-NLS-2$
                // backup pkg list file
                File rpmListFile = new File(rpmListFilepath);
                if (rpmListFile.exists()) {
                    Utils.copyFile(new File(rpmListFilepath), bkupFile);
                }

                BufferedWriter out = new BufferedWriter(new FileWriter(
                        rpmListFile, false));
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                monitor.subTask(Messages.RpmPackageBuildProposalsJob_2
                        + rpmListCmd + Messages.RpmPackageBuildProposalsJob_3);
                String line;
                while ((line = reader.readLine()) != null) {
                    monitor.subTask(line);
                    out.write(line + "\n"); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        in.destroyProcess();
                        in.close();
                        out.close();
                        // restore backup
                        if (rpmListFile.exists() && bkupFile.exists()) {
                            Utils.copyFile(bkupFile, rpmListFile);
                            bkupFile.delete();
                        }
                        Activator.packagesList = new RpmPackageProposalsList();
                        return Status.CANCEL_STATUS;
                    }
                }
                in.close();
                out.close();
                bkupFile.delete();
                int processExitValue = 0;
                try {
                    processExitValue = in.getExitValue();
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                }
                if (processExitValue != 0){
                    SpecfileLog
                            .log(IStatus.WARNING,
                                    processExitValue,
                                    NLS.bind(
                                            Messages.RpmPackageBuildProposalsJob_NonZeroReturn,
                                            processExitValue), null);
                }
            }
        } catch (IOException e) {
            SpecfileLog.logError(e);
            return Status.CANCEL_STATUS;
        } finally {
            monitor.done();
        }
        // Update package list
        Activator.packagesList = new RpmPackageProposalsList();
        return Status.OK_STATUS;
    }

    /**
     * Enable and disable the property change listener.
     *
     * @param activated Flag indicating whether the listener to be enabled or disabled.
     */
    public static void setPropertyChangeListener(boolean activated) {
        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        if (activated) {
            preferences.addPreferenceChangeListener(PROPERTY_LISTENER);
        } else {
            preferences.removePreferenceChangeListener(PROPERTY_LISTENER);
        }
    }

}
