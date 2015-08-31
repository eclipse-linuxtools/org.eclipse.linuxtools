/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
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
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;

/**
 * 
 */
public class ImagePull extends Wizard {

	private final ImagePullPage imagePullPage;

	/**
	 * Constructor when an {@link IDockerConnection} has been selected to run an
	 * {@link IDockerImage}.
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} pointing to a specific Docker
	 *            daemon/host.
	 * @throws DockerException
	 */
	public ImagePull(final IDockerConnection connection) {
		super();
		setWindowTitle(WizardMessages.getString("ImagePull.title")); //$NON-NLS-1$
		this.imagePullPage = new ImagePullPage(connection);
	}

	@Override
	public void addPages() {
		addPage(imagePullPage);
	}

	@Override
	public boolean canFinish() {
		return this.imagePullPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public String getImageName() {
		return this.imagePullPage.getImageName();
	}

}
