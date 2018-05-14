/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

public class ImageTag extends Wizard {

	private ImageTagPage mainPage;

	private final String imageName;

	public ImageTag(final String imageName) {
		super();
		setWindowTitle(WizardMessages.getString("ImageTag.title")); //$NON-NLS-1$
		this.imageName = imageName;
	}

	public String getTag() {
		if (this.mainPage != null) {
			return this.mainPage.getTag();
		}
		return null;
	}

	@Override
	public void addPages() {
		this.mainPage = new ImageTagPage(this.imageName);
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
