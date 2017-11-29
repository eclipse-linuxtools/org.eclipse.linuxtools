/*******************************************************************************
 * Copyright (c) 2014, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestCore;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;

public class OSIORestRepositorySettingsPage extends AbstractRepositorySettingsPage {
	private static final String DESCRIPTION = Messages.OSIORestRepositorySettingsPage_Description;

	private StyledText authToken;
	private Text userId;

	private Label authTokenLabel;
	private Label userIdLabel;

	public OSIORestRepositorySettingsPage(TaskRepository taskRepository, AbstractRepositoryConnector connector,
			AbstractRepositoryConnectorUi connectorUi) {
		super(Messages.OSIORestRepositorySettingsPage_RestRepositorySetting, DESCRIPTION, taskRepository, connector,
				connectorUi);
		setNeedsAnonymousLogin(true);
		setNeedsEncoding(false);
		setNeedsAdvanced(true);
		setNeedsValidateOnFinish(true);
	}

	@Override
	public String getConnectorKind() {
		return OSIORestCore.CONNECTOR_KIND;
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		Composite authTokenContainer = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(authTokenContainer);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(authTokenContainer);


		userIdLabel = new Label(authTokenContainer, SWT.NONE);
		userIdLabel.setText(Messages.OSIORestRepositorySettingsPage_auth_username);
		userId = new Text(authTokenContainer, SWT.BORDER);
		GridDataFactory.fillDefaults()
		.grab(true, false)
		.align(SWT.FILL, SWT.CENTER)
		.hint(300, SWT.DEFAULT)
		.applyTo(userId);
		
		authTokenLabel = new Label(authTokenContainer, SWT.NONE);
		authTokenLabel.setText(Messages.OSIORestRepositorySettingsPage_auth_token);
		authToken = new StyledText(authTokenContainer, SWT.BORDER | SWT.WRAP | SWT.PASSWORD);
		authToken.setEnabled(true);
		GridDataFactory.fillDefaults()
				.grab(true, true)
				.align(SWT.FILL, SWT.CENTER)
				.hint(300, 600)
				.applyTo(authToken);
		if (repository != null) {
			String apiKeyValue = repository.getProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN);
			authToken.setText(Strings.nullToEmpty(apiKeyValue));
			authToken.setEnabled(true);
		}

		updateURLInformation();
		serverUrlCombo.add("https://openshift.io/api"); //$NON-NLS-1$
		serverUrlCombo.select(0);
		serverUrlCombo.setEnabled(false);
		serverUrlCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateURLInformation();
			}
		});
		serverUrlCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateURLInformation();
			}
		});
	}

	protected void updateURLInformation() {
		authTokenLabel.setToolTipText(
				NLS.bind(Messages.OSIORestRepositorySettingsPage_Please_copy_the_Auth_Token_from, null));
	}

	@Override
	public void applyTo(TaskRepository repository) {
		repository.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, authToken.getText());
		repository.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, userId.getText());
		repository.setCategory(TaskRepository.CATEGORY_TASKS);
		super.applyTo(repository);
	}
}
