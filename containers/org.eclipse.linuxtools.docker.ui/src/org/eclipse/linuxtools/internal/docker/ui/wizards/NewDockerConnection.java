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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Wizard to add a Docker daemon connection
 * @author xcoulon
 *
 */
public class NewDockerConnection extends Wizard {
	
	private NewDockerConnectionPage wizardPage;
	private IDockerConnection dockerConnection;

	public NewDockerConnection() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		wizardPage = new NewDockerConnectionPage();
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		try {
			dockerConnection = wizardPage.getDockerConnection();
			DockerConnectionManager.getInstance().addConnection(dockerConnection);
			return true;
		} catch (DockerException e) {
			new MessageDialog(Display.getDefault().getActiveShell(),
					WizardMessages.getString("NewDockerConnection.failure"), //$NON-NLS-1$
					null,
					WizardMessages.getString("NewDockerConnection.failMessage"), //$NON-NLS-1$
					SWT.ICON_ERROR,
					new String[] { WizardMessages
							.getString("NewDockerConnectionPage.ok") }, //$NON-NLS-1$
					0).open(); // ;
		}
		return false;
	}
	
	public IDockerConnection getDockerConnection() {
		return dockerConnection;
	}

}
