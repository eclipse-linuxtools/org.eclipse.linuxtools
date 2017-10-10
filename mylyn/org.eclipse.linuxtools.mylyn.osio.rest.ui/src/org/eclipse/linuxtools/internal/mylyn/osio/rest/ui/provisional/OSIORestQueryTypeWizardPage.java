/*******************************************************************************
 * Copyright (c) 2015, 2017 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class OSIORestQueryTypeWizardPage extends WizardPage {

	private final AbstractRepositoryQueryPage2 customPage;

	private final AbstractRepositoryQueryPage2 searchPage;

	private Button buttonCustom;

	private Button buttonForm;

	private Composite composite;

	public OSIORestQueryTypeWizardPage(TaskRepository repository, AbstractRepositoryConnector connector) {
		super(Messages.OSIORestQueryTypeWizardPage_ChooseQueryType);
		setTitle(Messages.OSIORestQueryTypeWizardPage_ChooseQueryType);
		setDescription(Messages.OSIORestQueryTypeWizardPage_SelectAvailableQueryTypes);
		setImageDescriptor(TasksUiImages.BANNER_REPOSITORY);
		OSIORestConnector connectorREST = (OSIORestConnector) connector;
		TaskData taskDataSimpleURL = new TaskData(new OSIORestTaskAttributeMapper(repository, connectorREST),
				repository.getConnectorKind(), Messages.OSIORestQueryTypeWizardPage_Query,
				Messages.OSIORestQueryTypeWizardPage_Query);
		TaskData taskDataSearch = new TaskData(new OSIORestTaskAttributeMapper(repository, connectorREST),
				repository.getConnectorKind(), Messages.OSIORestQueryTypeWizardPage_Query,
				Messages.OSIORestQueryTypeWizardPage_Query);
		customPage = OSIORestUIUtil.createOSIORestSearchPage(true, false, taskDataSimpleURL, connectorREST,
				repository, null);
		searchPage = OSIORestUIUtil.createOSIORestSearchPage(false, false, taskDataSearch, connectorREST,
				repository, null);
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessVerticalSpace = false;
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(1, false));

		buttonForm = new Button(composite, SWT.RADIO);
		buttonForm.setText(Messages.OSIORestQueryTypeWizardPage_CreateQueryUsingForm);
		buttonForm.setSelection(true);

		buttonCustom = new Button(composite, SWT.RADIO);
		buttonCustom.setText(Messages.OSIORestQueryTypeWizardPage_CreateQueryFromExistingURL);

		setPageComplete(true);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	@Override
	public IWizardPage getNextPage() {
		if (buttonForm.getSelection()) {
			searchPage.setWizard(this.getWizard());
			return searchPage;
		}
		customPage.setWizard(this.getWizard());
		return customPage;
	}
}
