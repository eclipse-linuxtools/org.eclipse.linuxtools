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
package org.eclipse.linuxtools.rpm.ui.editor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.rpm.ui.editor.preferences.PreferenceConstants;

/**
 * Job to initialize and update the RPM packages proposal list.
 * 
 * FIXME: The job seems to be run twice on 3.3 (in the job progress view) but when break point are strategically placed,
 * the job seems to run only once, these symptoms appear only when the job is trigged from Activator#start method.
 * 
 * @author Alphonse Van Assche
 *
 */
public class RpmPackageBuildProposalsJob extends Job {
	
	private RpmPackageBuildProposalsJob(String name) {
		super(name);
	}
	
	private static final String JOB_NAME =  "Update RPM packages proposal list"; 
	
	private static RpmPackageBuildProposalsJob job = null; 

	private static final Preferences preferences =  Activator.getDefault().getPluginPreferences(); 

	protected static final Preferences.IPropertyChangeListener propertyListener = new Preferences.IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty() != PreferenceConstants.P_RPM_LIST_LAST_BUILD)
				update();
		}
	};
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		return retrievePackageList(monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#shouldSchedule()
	 */
	public boolean shouldSchedule() { 
		   return equals(job); 
	} 
	
	/**
	 * Run the Job if it's needed according with the configuration set in the preference page.
	 */
	protected static void update() {   
		boolean runJob = false;
	    // Today's date
	    Date today = new Date();
		if (preferences.getBoolean(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD)) {
			int period = preferences.getInt(PreferenceConstants.P_RPM_LIST_BUILD_PERIOD); 
			// each time that the plugin is loaded.
			if (period == 1) {
				runJob = true;
			} else {
				long lastBuildTime = preferences.getLong(PreferenceConstants.P_RPM_LIST_LAST_BUILD);
				if (lastBuildTime == 0) {
					runJob = true;
				} else {
					long interval = (today.getTime() - lastBuildTime) / (1000 * 60 * 60 * 24);
					// run the job once a week
					if (period == 2 && interval >= 7)
						runJob = true;
					// run the job once a month
					else if (period == 3 && interval >= 30)
						runJob = true;
				}
			}
			if (job == null && runJob) {
				job = new RpmPackageBuildProposalsJob(JOB_NAME);
				job.schedule();
				// update last build preference with the current date.
				preferences.setValue(PreferenceConstants.P_RPM_LIST_LAST_BUILD, today.getTime());
			}
		} else {
			if (job != null) {
				job.cancel();
				job = null;
			}
		}
	} 
	
	/**
	 * Initialize the list, used if .pkgList is not found. 
	 */
	protected static void initializeList() {  
	    // Today's date
	    Date today = new Date();
		if (job == null) {
			job = new RpmPackageBuildProposalsJob(JOB_NAME);
			job.schedule();
			// update last build preference with the current date.
			preferences.setValue(PreferenceConstants.P_RPM_LIST_LAST_BUILD, today.getTime());
		}
	}

	/**
	 * Retrieve the package list
	 * 
	 * @param monitor to update
	 * @return a <code>IStatus</code>
	 */
	private IStatus retrievePackageList(IProgressMonitor monitor) {
		String rpmListCmd = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_CURRENT_RPMTOOLS);
		String rpmListFilepath = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_RPM_LIST_FILEPATH);
		try {
			String[] cmd = new String[] {"/bin/sh", "-c", rpmListCmd};
			monitor.beginTask("Get the list of RPM packages...", IProgressMonitor.UNKNOWN);
			Process child = Runtime.getRuntime().exec(cmd);
			InputStream in = child.getInputStream();
			BufferedWriter out = new BufferedWriter(new FileWriter(rpmListFilepath, false));
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			monitor.subTask("Write the list of RPM packages into "
			+ rpmListFilepath + " file ...");
			String line;
			while ((line = reader.readLine()) != null) {
				monitor.subTask("Add package: " + line);
				out.write(line + "\n");
	        }
			in.close();
			out.close();
		} catch (IOException e) {
			SpecfileLog.logError(e);
			return null;
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
	 * @param activated
	 */
	protected static void setPropertyChangeListener(boolean activated) {
		if (activated) {
			preferences.addPropertyChangeListener(propertyListener);
		} else {
			preferences.removePropertyChangeListener(propertyListener);
		}
		
	}
	   
}
