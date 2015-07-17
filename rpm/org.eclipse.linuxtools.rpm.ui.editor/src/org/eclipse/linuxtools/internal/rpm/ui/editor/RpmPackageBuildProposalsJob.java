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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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

    private IJobChangeListener updateFinishedListener = new JobChangeAdapter();

    protected static final IPropertyChangeListener PROPERTY_LISTENER = new IPropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(PreferenceConstants.P_CURRENT_RPMTOOLS)) {
                updateAsync();
            }
        }
    };

    protected static final IPreferenceStore STORE = Activator.getDefault().getPreferenceStore();

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        return retrievePackageList(monitor);
    }

    @Override
    public boolean shouldSchedule() {
        return equals(job);
    }

    public static void updateSync() {
        update(false);
    }

    public static void updateAsync() {
        update(true);
    }

    /**
     * Run the Job if it's needed according with the configuration set in the
     * preference page.
     */
    private static void update(boolean async) {
        boolean runJob = false;
        // Today's date
        Date today = new Date();
        if (STORE.getBoolean(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD)) {
            int period = STORE.getInt(PreferenceConstants.P_RPM_LIST_BUILD_PERIOD);
            // each time that the plugin is loaded.
            if (period == 1) {
                runJob = true;
            } else {
                long lastBuildTime = STORE.getLong(PreferenceConstants.P_RPM_LIST_LAST_BUILD);
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
                } else {
                    job.cancel();
                }
                if (async) {
                    job.schedule();
                } else {
                    job.run(new NullProgressMonitor());
                }
                STORE.setValue(PreferenceConstants.P_RPM_LIST_LAST_BUILD, today.getTime());
            }
        } else {
            if (job != null) {
                job.cancel();
                job = null;
            }
        }
    }

    /**
     * Retrieve the package list
     *
     * @param monitor
     *            to update
     * @return a <code>IStatus</code>
     */
    private IStatus retrievePackageList(IProgressMonitor monitor) {
        String rpmListCmd = STORE.getString(PreferenceConstants.P_CURRENT_RPMTOOLS);
        String rpmListFilepath = STORE.getString(PreferenceConstants.P_RPM_LIST_FILEPATH);
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

                try (BufferedWriter out = new BufferedWriter(new FileWriter(rpmListFile, false));
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
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
                    if (processExitValue != 0) {
                        SpecfileLog
                        .log(IStatus.WARNING,
                                processExitValue,
                                NLS.bind(
                                        Messages.RpmPackageBuildProposalsJob_NonZeroReturn,
                                        processExitValue), null);
                    }
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

    public static Set<String> getPackages() throws InterruptedException, IOException {
        if (job.getThread() != Thread.currentThread()) {
            job.join();
        }
        final Set<String> list = new HashSet<>();
        String rpmpkgsFile = Activator.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.P_RPM_LIST_FILEPATH);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(rpmpkgsFile)))) {
            String line = reader.readLine();
            while (line != null) {
                list.add(line.trim());
                line = reader.readLine();
            }
        }
        return list;
    }

    /**
     * Enable and disable the property change listener.
     *
     * @param activated Flag indicating whether the listener to be enabled or disabled.
     */
    public static void setPropertyChangeListener(boolean activated) {
        if (activated) {
            STORE.addPropertyChangeListener(PROPERTY_LISTENER);
        } else {
            STORE.removePropertyChangeListener(PROPERTY_LISTENER);
        }
    }

}
