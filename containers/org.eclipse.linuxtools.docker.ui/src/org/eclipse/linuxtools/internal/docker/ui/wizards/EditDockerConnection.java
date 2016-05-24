/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 * @author xcoulon
 *
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
