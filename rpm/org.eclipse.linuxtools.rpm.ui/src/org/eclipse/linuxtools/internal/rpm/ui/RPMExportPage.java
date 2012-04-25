/*******************************************************************************
 * Copyright (c) 2004, 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * S/RPM export Defines the page that is shown to the user when they choose to
 * export to an S/RPM. Defines the UI elements shown, and the basic validation.
 * RPMExportPage. Called by RPMExportWizard. Class can not be subclassed extends
 * WizardPage for the RPM export wizard, implements Listener
 */
public class RPMExportPage extends WizardPage implements Listener {

	// Checkbox Buttons
	private Button exportBinary;
	private Button exportSource;

	// The currently selected RPM project
	private RPMProject rpmProject;

	/**
	 * Creates the page for the given project.
	 * 
	 * @param rpmProject The project we would export.
	 */
	public RPMExportPage(IProject rpmProject) {
		super(
				Messages.getString("RPMExportPage.Export_SRPM"), //$NON-NLS-1$
				Messages.getString("RPMExportPage.Export_SRPM_from_project"), null); //$NON-NLS-1$
		setDescription(Messages
				.getString("RPMExportPage.Select_project_export")); //$NON-NLS-1$
		try {
			if (rpmProject.getPersistentProperty(new QualifiedName(IRPMConstants.RPM_CORE_ID, IRPMConstants.SOURCES_FOLDER)) != null){
				this.rpmProject = new RPMProject(rpmProject, RPMProjectLayout.RPMBUILD);
			} else {
				this.rpmProject = new RPMProject(rpmProject, RPMProjectLayout.FLAT);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		setPageComplete(true);
	}

	/**
	 * Returns the selected project.
	 * 
	 * @return The selected project.
	 */
	public RPMProject getSelectedRPMProject() {
		return rpmProject;
	}

	/**
	 * Retursn what needs to be exported.
	 * @return The export type.
	 */
	public BuildType getExportType() {
		BuildType exportType = BuildType.NONE;
		if (exportBinary.getSelection() && exportSource.getSelection()) {
			exportType = BuildType.ALL;
		} else if (exportBinary.getSelection()) {
			exportType = BuildType.BINARY;
		} else if (exportSource.getSelection()) {
			exportType = BuildType.SOURCE;
		}
		return exportType;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 * 
	 *      Parent control. Creates the listbox, Destination box, and options
	 *      box
	 * 
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		// Create a layout for the wizard page
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		if (rpmProject != null) {
			// Create contols on the page
			createExportTypeControls(composite);

			// Check if the project has changed
			// and therefore the project needs a patch
			setPageComplete(false);
		} else {
			createErrorControls(composite);
			setPageComplete(true);
		}
	}

	private void createErrorControls(Composite parent) {
		Label label = new Label(parent, SWT.CENTER);
		label.setText(Messages.getString("RPMExportPage.0")); //$NON-NLS-1$
	}

	private void createExportTypeControls(Composite parent) {
		// Create a group for the control and set up the layout.
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group
				.setText(Messages
						.getString("RPMExportPage.Composite_Export_Type")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));

		// Create the export binary checkbox
		exportBinary = new Button(group, SWT.CHECK);
		exportBinary.setText(Messages.getString("RPMExportPage.Export_Binary")); //$NON-NLS-1$
		exportBinary.setSelection(true);
		exportBinary.setToolTipText(Messages
				.getString("RPMExportPage.toolTip_Export_Binary")); //$NON-NLS-1$

		// Create the export source checkbox
		exportSource = new Button(group, SWT.CHECK);
		exportSource.setText(Messages.getString("RPMExportPage.Export_Source")); //$NON-NLS-1$
		exportSource.setSelection(true);
		exportSource.setToolTipText(Messages
				.getString("RPMExportPage.toolTip_Export_Source")); //$NON-NLS-1$

		SelectionListener listener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				handleEvent(null);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				handleEvent(null);
			}
		};
		exportBinary.addSelectionListener(listener);
		exportSource.addSelectionListener(listener);
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
		if (rpmProject != null && !exportBinary.getSelection()
				&& !exportSource.getSelection()) {
			// Make sure either export binary/source is checked
			setErrorMessage(Messages
					.getString("RPMExportPage.Select_export_type")); //$NON-NLS-1$
			return false;
		}

		setDescription(null);
		setErrorMessage(null);
		return true;
	}

	public void handleEvent(Event e) {
		setPageComplete(canFinish());
	}
}
