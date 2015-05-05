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

public class ImagePush extends Wizard {

	private ImagePushPage mainPage;
	private String imageTag;
	private IDockerImage image;

	public ImagePush() {
		super();
	}

	public ImagePush(IDockerImage image) {
		super();
		this.image = image;
	}

	public String getImageTag() {
		return imageTag;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		mainPage = new ImagePushPage(image);
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		imageTag = mainPage.getImageTag();

		return true;
	}

}
