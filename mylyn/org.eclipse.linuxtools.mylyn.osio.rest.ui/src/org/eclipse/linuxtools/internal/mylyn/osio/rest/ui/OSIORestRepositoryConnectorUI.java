/*******************************************************************************
 * Copyright (c) 2014, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestCore;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskAttributeMapper;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional.OSIORestQueryTypeWizardPage;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional.OSIORestTaskAttachmentPage;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional.OSIORestUIUtil;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

public class OSIORestRepositoryConnectorUI extends AbstractRepositoryConnectorUi {

	public OSIORestRepositoryConnectorUI() {
	}

	public OSIORestRepositoryConnectorUI(AbstractRepositoryConnector connector) {
		super(connector);
	}

	@Override
	public String getConnectorKind() {
		return OSIORestCore.CONNECTOR_KIND;
	}

	@Override
	public ITaskRepositoryPage getSettingsPage(TaskRepository repository) {
		return new OSIORestRepositorySettingsPage(repository, getConnector(), this);
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
		AbstractRepositoryConnector connector = getConnector();
		OSIORestConnector connectorREST = (OSIORestConnector) connector;

		TaskData taskData = new TaskData(new OSIORestTaskAttributeMapper(repository, connectorREST),
				repository.getConnectorKind(), "Query", "Query"); //$NON-NLS-1$ //$NON-NLS-2$

		if (query == null) {
			wizard.addPage(new OSIORestQueryTypeWizardPage(repository, connector));
		} else {
			if (isCustomQuery(query)) {
				wizard.addPage(OSIORestUIUtil.createOSIORestSearchPage(true, true, taskData, connectorREST,
						repository, query));
			} else {
				wizard.addPage(OSIORestUIUtil.createOSIORestSearchPage(false, true, taskData, connectorREST,
						repository, query));
			}
		}
		return wizard;
	}

	private boolean isCustomQuery(IRepositoryQuery query2) {
		String custom = query2.getAttribute("SimpleURLQueryPage"); //$NON-NLS-1$
		return custom != null && custom.equals(Boolean.TRUE.toString());
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping selection) {
		return new NewTaskWizard(repository, selection);
	}

	@Override
	public boolean hasSearchPage() {
		return true;
	}

	@Override
	public IWizardPage getTaskAttachmentPage(TaskAttachmentModel model) {
		return new OSIORestTaskAttachmentPage(model);
	}

}
