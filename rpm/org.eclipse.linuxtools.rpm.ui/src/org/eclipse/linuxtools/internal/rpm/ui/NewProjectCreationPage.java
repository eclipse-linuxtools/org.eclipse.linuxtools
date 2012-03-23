/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * Standard page for project creation adding some rpm specific controls.
 *
 */
public class NewProjectCreationPage extends WizardNewProjectCreationPage {

	private ComboViewer typeCombo;

	/**
	 * Instantiate the page.
	 * @param pageName The name of the page.
	 */
	public NewProjectCreationPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite control = (Composite) getControl();
		Composite projectTypeGroup = new Composite(control, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectTypeGroup.setLayout(layout);
		projectTypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label typeLabel = new Label(projectTypeGroup, SWT.NONE);
		typeLabel.setText(Messages.getString("SRPMImportPage.4")); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		typeCombo = new ComboViewer(projectTypeGroup, SWT.READ_ONLY);
		typeCombo.getControl().setLayoutData(data);
		typeCombo.setContentProvider(ArrayContentProvider.getInstance());
		typeCombo.setInput(RPMProjectLayout.values());
		typeCombo.getCombo().select(0);

	}

	/**
	 * Returns the selected layout if any or the default one - RPMBUILD.
	 * 
	 * @return The selected project layout.
	 */
	public RPMProjectLayout getSelectedLayout() {
		return RPMProjectLayout.valueOf(typeCombo.getCombo().getItem(
				typeCombo.getCombo().getSelectionIndex()));

	}

}
