/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

public class ImageRemove extends Wizard {

	private ImageRemovePage mainPage;
	private List<String> imageNames;

	public ImageRemove() {
		super();
	}

	public List<String> getImageNames() {
		return imageNames;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		mainPage = new ImageRemovePage();
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		imageNames = mainPage.getImageNames();

		return true;
	}

}
