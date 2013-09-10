/*******************************************************************************
 * Copyright (c) 2004-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.rpm.core.RPMProjectCreator;
import org.eclipse.linuxtools.rpm.ui.SRPMImportOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * RPM GUI import page. Defines the page the is shown to the user when they
 * choose to export to and RPM. Defines the UI elements shown, and the basic
 * validation (need to add to this) SRPMImportPage. Called by SRPMImportwizard.
 * Class can not be subclassed extends WizardPage and implements Listener (for
 * events)
 * 
 */
public class SRPMImportPage extends WizardPage {

	// GUI Control variables
	private Text sourceSRPM;
	private RPMDetailsPanel detailsPanel;

	/**
	 * Constructor for SRPMImportPage class
	 */
	public SRPMImportPage() {
		super(
				Messages.getString("SRPMImportPage.Import_SRPM"), //$NON-NLS-1$
				Messages.getString("SRPMImportPage.Select_project_to_import"), null); //$NON-NLS-1$

		setPageComplete(false);
		setDescription(Messages
				.getString("SRPMImportPage.Select_project_to_import")); //$NON-NLS-1$
	}

	@Override
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
		detailsPanel = new RPMDetailsPanel(composite);
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

		sourceSRPM = new Text(sourceSpecComposite, SWT.BORDER);
		sourceSRPM.setToolTipText(Messages
				.getString("SRPMImportPage.toolTip_SRPM_Name")); //$NON-NLS-1$

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		sourceSRPM.setLayoutData(gridData);
		sourceSRPM.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				boolean validSRPM = canFinish();
				if (validSRPM) {
					setPageComplete(validSRPM);
					changeProjectSettings();
				}
			}

		});

		Button srpmBrowse = new Button(sourceSpecComposite, SWT.PUSH);
		srpmBrowse.setToolTipText(Messages
				.getString("SRPMImportPage.toolTip_Open_file_navigator")); //$NON-NLS-1$
		srpmBrowse.setText(Messages.getString("RPMPage.Browse")); //$NON-NLS-1$
		srpmBrowse.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				FileDialog srpmBrowseDialog = new FileDialog(getContainer()
						.getShell(), SWT.OPEN);
				String selectedSRPMName = srpmBrowseDialog.open();
				if (selectedSRPMName != null) {
					File testSRPMfilename = new File(selectedSRPMName);
					if (testSRPMfilename.isFile()) {
						sourceSRPM.setText(selectedSRPMName);
					}
				}
			}
		});
	}

	private void changeProjectSettings() {
		String srpmName = sourceSRPM.getText();

		detailsPanel.setLocationPath(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().append(getProjectName(srpmName)).toFile()
				.getAbsolutePath());
	}

	private String getProjectName(String srpmName) {
		String projectName = srpmName.substring(srpmName.lastIndexOf('/') + 1);
		return projectName.replaceAll("-[\\d|\\.]+-[\\d|\\.].+", ""); //$NON-NLS-1$ //$NON-NLS-2$
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
		if (!sourceSRPMName.isEmpty()
				&& sourceSRPM.getText().lastIndexOf(".src.rpm") == -1) {//$NON-NLS-1$
			setErrorMessage(Messages.getString("SRPMImportPage.No_src_rpm_ext")); //$NON-NLS-1$
			return false;
		}
		if (sourceSRPMName.startsWith("http://")) { //$NON-NLS-1$
			try {
				URL url = new URL(sourceSRPMName);
				if (HttpURLConnection.HTTP_NOT_FOUND == ((HttpURLConnection) url
						.openConnection()).getResponseCode()) {
					setErrorMessage(Messages
							.getString("SRPMImportPage.Source_not_Valid")); //$NON-NLS-1$
					return false;
				}
			} catch (MalformedURLException e) {
				setErrorMessage(Messages
						.getString("SRPMImportPage.Source_not_Valid")); //$NON-NLS-1$
				return false;
			} catch (IOException e) {
				setErrorMessage(Messages
						.getString("SRPMImportPage.Source_not_Valid")); //$NON-NLS-1$
				return false;
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
		SRPMImportOperation srpmImportOp = null;
		try {
			IProject detailedProject = getNewProject();
			String srpmName = sourceSRPM.getText();
			if (srpmName.startsWith("http://")) { //$NON-NLS-1$
				URL sourceRPMURL = new URL(srpmName);
				srpmImportOp = new SRPMImportOperation(detailedProject,
						sourceRPMURL, detailsPanel.getSelectedLayout());
			} else {
				File sourceRPMFile = new File(srpmName);
				srpmImportOp = new SRPMImportOperation(detailedProject,
						sourceRPMFile, detailsPanel.getSelectedLayout());
			}
			getContainer().run(true, true, srpmImportOp);
		} catch (InterruptedException e) {
			setErrorMessage(e.toString());
			return false;
		} catch (InvocationTargetException e) {
			setErrorMessage(e.toString());
			return false;
		} catch (MalformedURLException e) {
			setErrorMessage(e.toString());
			return false;
		} catch (CoreException e) {
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
	 * Creates a new project.
	 * @throws CoreException If project creation failed.
	 */
	private IProject getNewProject() throws CoreException {
		IPath path = detailsPanel.getLocationPath();
		RPMProjectCreator projectCreator = new RPMProjectCreator(
				detailsPanel.getSelectedLayout());
		return projectCreator.create(getProjectName(path.lastSegment()),
				path.removeLastSegments(1), new NullProgressMonitor());
	}
}
