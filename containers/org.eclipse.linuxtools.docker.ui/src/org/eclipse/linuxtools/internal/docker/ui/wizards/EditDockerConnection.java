/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;

/**
 * Wizard to edit a Docker connection.
 */
public class EditDockerConnection extends Wizard {
	
	private EditDockerConnectionPage wizardPage;
	private final IDockerConnection currentConnection;

	/**
	 * Constructor
	 * 
	 * @param currentConnection
	 *            the {@link IDockerConnection} to edit
	 */
	public EditDockerConnection(final IDockerConnection currentConnection) {
		super();
		this.currentConnection = currentConnection;
		setNeedsProgressMonitor(true);
		setWindowTitle(WizardMessages.getString("EditDockerConnection.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		wizardPage = new EditDockerConnectionPage(this.currentConnection);
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		final IDockerConnection updatedConnection = wizardPage
				.getDockerConnection();
		DockerConnectionManager.getInstance()
				.updateConnection(this.currentConnection,
						updatedConnection.getName(),
						updatedConnection.getSettings());
		return true;
	}

}
