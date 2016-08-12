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

public class ImageTag extends Wizard {

	private ImageTagPage mainPage;

	private final String imageName;

	public ImageTag(final String imageName) {
		super();
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
