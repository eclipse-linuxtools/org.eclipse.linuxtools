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
	private String tag;
	private IDockerImage image;

	public ImageRemoveTag(IDockerImage image) {
		super();
		this.image = image;
	}

	public String getTag() {
		return tag;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		mainPage = new ImageRemoveTagPage(image);
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		tag = mainPage.getTag();

		return true;
	}

}
