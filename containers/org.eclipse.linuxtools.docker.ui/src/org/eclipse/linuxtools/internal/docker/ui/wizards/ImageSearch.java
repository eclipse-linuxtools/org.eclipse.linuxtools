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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImageSearchResult;;

/**
 * Wizard to search for images.
 * 
 * @author xcoulon
 *
 */
public class ImageSearch extends Wizard {

	/** the Image Search {@link WizardPage}. */
	private final ImageSearchPage searchPage;

	/** the databinding model for the {@link ImageSearchPage}. */
	private final ImageSearchModel model;

	/**
	 * Default Constructor
	 */
	public ImageSearch(final IDockerConnection connection) {
		setNeedsProgressMonitor(true);
		this.model = new ImageSearchModel(connection);
		this.searchPage = new ImageSearchPage(this.model);
	}

	@Override
	public void addPages() {
		addPage(searchPage);
	}

	@Override
	public boolean canFinish() {
		return this.searchPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public IDockerImageSearchResult getSelectedImage() {
		return this.model.getSelectedImage();
	}
}
