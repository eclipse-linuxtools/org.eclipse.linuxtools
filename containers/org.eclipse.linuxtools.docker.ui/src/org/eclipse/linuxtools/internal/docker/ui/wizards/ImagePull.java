/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
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

public class ImagePull extends Wizard {

	private ImagePullPage mainPage;
	private String imageId;

	public ImagePull() {
		super();
	}

	public String getImageId() {
		return imageId;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		mainPage = new ImagePullPage();
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		imageId = mainPage.getImageId();

		return true;
	}

}
