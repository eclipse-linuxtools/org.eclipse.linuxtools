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
import org.eclipse.linuxtools.docker.core.IDockerImage;

public class ImageRemoveTag extends Wizard {

	private ImageRemoveTagPage mainPage;
	private IDockerImage image;

	public ImageRemoveTag(IDockerImage image) {
		super();
		this.image = image;
	}

	public String getTag() {
		if (this.mainPage != null) {
			return mainPage.getTag();
		}
		return null;
	}

	@Override
	public void addPages() {
		this.mainPage = new ImageRemoveTagPage(image);
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return this.mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
