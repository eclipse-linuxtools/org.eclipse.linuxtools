/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.rpm.core.utils.FileDownloadJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * RPM GUI import page. Defines the page the is shown to the user when they
 * choose to export to and RPM. Defines the UI elements shown, and the basic
 * validation (need to add to this) SRPMImportPage. Called by SRPMImportwizard.
 * Class can not be subclassed extends WizardPage and implements Listener (for
 * events)
 * 
 */
public class SRPMImportPage extends WizardPage {

	/**
	 * This is a copy of
	 * org.eclipse.team.internal.ccvs.ui.wizards.CheckoutAsWizard
	 * .NewProjectListener
	 **/
	class NewProjectListener implements IResourceChangeListener {
		private IProject newProject = null;

		/**
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta root = event.getDelta();
			IResourceDelta[] projectDeltas = root.getAffectedChildren();
			for (int i = 0; i < projectDeltas.length; i++) {
				IResourceDelta delta = projectDeltas[i];
				IResource resource = delta.getResource();
				if (delta.getKind() == IResourceDelta.ADDED) {
					newProject = (IProject) resource;
				}
			}
		}

		/**
		 * Gets the newProject.
		 * 
		 * @return Returns a IProject
		 */
		public IProject getNewProject() {
			return newProject;
		}
	}

	// GUI Control variables
	private Combo sourceSRPM;

	static private Vector<String> srpmVector;

	/**
	 * @see java.lang.Object#Object()
	 * 
	 *      Constructor for SRPMImportPage class
	 * @param aWorkbench
	 *            - Workbench
	 */
	public SRPMImportPage(IWorkbench aWorkbench) {
		super(
				Messages.getString("SRPMImportPage.Import_SRPM"), //$NON-NLS-1$
				Messages.getString("SRPMImportPage.Select_project_to_import"), null); //$NON-NLS-1$

		setPageComplete(false);
		setDescription(Messages
				.getString("SRPMImportPage.Select_project_to_import")); //$NON-NLS-1$
	}

	private File getSelectedSRPM() {
		String srpmName = sourceSRPM.getText();
		if (srpmName == null || srpmName.equals("")) { //$NON-NLS-1$
			return null;
		}
		if (srpmName.startsWith("http://")) { //$NON-NLS-1$
			try {
				URL url = new URL(srpmName);
				URLConnection content = url.openConnection();
				File tempFile = new File(System.getProperty("java.io.tmpdir"),srpmName.substring(srpmName.lastIndexOf('/') + 1)); //$NON-NLS-1$
				if(tempFile.exists()){
					tempFile.delete();
				}
				final FileDownloadJob downloadJob = new FileDownloadJob(
						tempFile, content);
				getContainer().run(false, true, new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						downloadJob.run(monitor);
						

					}
				});
				return tempFile;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return new File(sourceSRPM.getText());
	}

	public void createControl(Composite parent) {
		// Set Page complete to false. Don't allow the user to execute wizard
		// until we have all the required data
		setPageComplete(false);

		// Create a generic composite to hold ui variable
		Composite composite = new Composite(parent, SWT.NULL);

		// Create a layout for the wizard page
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		// Create contols on the page
		createSourceRPMCombo(composite);
	}

	private void createSourceRPMCombo(Composite parent) {
		Group specGrid = new Group(parent, SWT.NONE);
		specGrid.setLayout(new GridLayout());
		specGrid.setText(Messages.getString("SRPMImportPage.SRPM_Name")); //$NON-NLS-1$
		specGrid.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));

		Composite sourceSpecComposite = new Composite(specGrid, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		sourceSpecComposite.setLayout(layout);
		sourceSpecComposite.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		sourceSRPM = new Combo(sourceSpecComposite, SWT.BORDER);
		sourceSRPM.setToolTipText(Messages
				.getString("SRPMImportPage.toolTip_SRPM_Name")); //$NON-NLS-1$

		if (srpmVector == null)
			srpmVector = new Vector<String>();
		for (int i = srpmVector.size(); i > 0; i--)
			sourceSRPM.add((srpmVector.elementAt(i - 1)));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		sourceSRPM.setLayoutData(gridData);
		sourceSRPM.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(canFinish());
			}
		});

		Button srpmBrowse = new Button(sourceSpecComposite, SWT.PUSH);
		srpmBrowse.setToolTipText(Messages
				.getString("SRPMImportPage.toolTip_Open_file_navigator")); //$NON-NLS-1$
		srpmBrowse.setText(Messages.getString("RPMPage.Browse")); //$NON-NLS-1$
		srpmBrowse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog srpmBrowseDialog = new FileDialog(getContainer()
						.getShell(), SWT.OPEN);
				String selectedSRPM_name = srpmBrowseDialog.open();
				if (selectedSRPM_name != null) {
					File testSRPMfilename = new File(selectedSRPM_name);
					if (testSRPMfilename.isFile())
						sourceSRPM.setText(selectedSRPM_name);
				}
			}
		});
	}


	/**
	 * canFinish()
	 * 
	 * Hot validation. Called to determine whether Finish button can be set to
	 * true
	 * 
	 * @return boolean. true if finish can be activated
	 */
	public boolean canFinish() {
		// Make sure an srpm name has been provided
		String sourceSRPMName = sourceSRPM.getText();
		if (!sourceSRPMName.isEmpty() && sourceSRPM.getText().lastIndexOf(".src.rpm") == -1) //$NON-NLS-1$
		{
			setErrorMessage(Messages.getString("SRPMImportPage.No_src_rpm_ext")); //$NON-NLS-1$
			return false;
		}
		if (sourceSRPMName.startsWith("http://")) { //$NON-NLS-1$
			try {
				URL url = new URL(sourceSRPMName);
				if (HttpURLConnection.HTTP_NOT_FOUND == ((HttpURLConnection) url
						.openConnection()).getResponseCode()) {
					setErrorMessage("HTTP not found!!!");
					return false;
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (sourceSRPMName.equals("")) { //$NON-NLS-1$
				return false;
			}
			File srpm = new File(sourceSRPMName);
			if (!srpm.isFile()) {
				setErrorMessage(Messages
						.getString("SRPMImportPage.Source_not_Valid")); //$NON-NLS-1$
				return false;
			}
		}
		setErrorMessage(null);
		return true;
	}

	/**
	 * finish()
	 * 
	 * Perform finish after finish button is pressed
	 * 
	 * @return boolean
	 */
	public boolean finish() {
		IProject detailedProject = getNewProject();
		// Add this SRPM to srpmList
		for (int i = 0; i < srpmVector.size(); i++) { // There can only be one
														// occurance
			if (srpmVector.elementAt(i).equals(sourceSRPM.getText())) {
				srpmVector.remove(i);
				break;
			}
		}
		srpmVector.add((sourceSRPM.getText()));

		SRPMImportOperation srpmImportOp = null;
		try {
			srpmImportOp = new SRPMImportOperation(detailedProject,
					getSelectedSRPM());
			getContainer().run(true, true, srpmImportOp);
		} catch (Exception e) {
			setErrorMessage(e.toString());
			return false;
		}

		// Get the status of the operation
		IStatus srpmImportStatus = srpmImportOp.getStatus();

		// If the status does not come back clean, open error dialog
		if (!srpmImportStatus.isOK()) {
			ErrorDialog.openError(getContainer().getShell(),
					Messages.getString("SRPMImportPage.Errors_importing_SRPM"), //$NON-NLS-1$
					null, // no special message
					srpmImportStatus);

			return false;
		}

		return true;
	}
	
	/**
	 * Get a new project that is configured by the new project wizard. This is
	 * currently the only way to do this. This is a copy of
	 * org.eclipse.team.internal
	 * .ccvs.ui.wizards.CheckoutAsWizard.getNewProject()
	 */
	private IProject getNewProject() {
		NewProjectListener listener = new NewProjectListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
				IResourceChangeEvent.POST_CHANGE);
		RPMNewProject wizard = new RPMNewProject();
		wizard.init(PlatformUI.getWorkbench(), null);
		// Instantiates the wizard container with the wizard and opens it
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.open();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		IProject project = listener.getNewProject();
		return project;
	}
}
