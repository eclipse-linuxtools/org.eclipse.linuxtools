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

package org.eclipse.linuxtools.docker.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageSearchModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageSearchPage;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageTagSelectionPage;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;

/**
 * Wizard to search for images.
 * 
 */
public class ImageSearch extends Wizard {

	/** the Image Search {@link WizardPage}. */
	private final ImageSearchPage imageSearchPage;

	/** the Image Tag selection {@link WizardPage}. */
	private final ImageTagSelectionPage imageTagSelectionPage;

	/**
	 * shared databinding model for {@link ImageSearchPage} and
	 * {@link ImageTagSelectionPage}.
	 */
	private final ImageSearchModel imageSearchModel;

	/**
	 * Default Constructor
	 * 
	 * @param connection
	 *            the current connection to a target Docker daemon
	 * @param name
	 *            the current image name or <code>null</code> if not applicable
	 */
	public ImageSearch(final IDockerConnection connection, final String name) {
		setWindowTitle(WizardMessages.getString("ImageSearch.title")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
		this.imageSearchModel = new ImageSearchModel(connection, name);
		this.imageSearchPage = new ImageSearchPage(this.imageSearchModel);
		this.imageTagSelectionPage = new ImageTagSelectionPage(
				this.imageSearchModel);
	}

	@Override
	public void addPages() {
		addPage(imageSearchPage);
		addPage(imageTagSelectionPage);
	}

	@Override
	public boolean canFinish() {
		return this.imageTagSelectionPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public String getSelectedImage() {
		return this.imageSearchPage.getSelectedImage().getName() + ":"
				+ this.imageTagSelectionPage.getSelectedImageTag().getName();
	}

}
