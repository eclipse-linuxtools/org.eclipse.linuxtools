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
	public boolean performFinish() {
		return true;
	}

}
