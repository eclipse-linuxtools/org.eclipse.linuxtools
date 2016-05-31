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
 * Wizard to add a Docker connection
 * 
 * @author xcoulon
 *
 */
public class NewDockerConnection extends Wizard {
	
	private NewDockerConnectionPage wizardPage;
	private IDockerConnection dockerConnection;
	
	/**
	 * Constructor.
	 */
	public NewDockerConnection() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle(WizardMessages.getString("NewDockerConnection.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		wizardPage = new NewDockerConnectionPage();
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		dockerConnection = wizardPage.getDockerConnection();
		DockerConnectionManager.getInstance().addConnection(dockerConnection);
		return true;
	}
	
	/**
	 * @return the {@link IDockerConnection} that was configured
	 */
	public IDockerConnection getDockerConnection() {
		return dockerConnection;
	}
}
