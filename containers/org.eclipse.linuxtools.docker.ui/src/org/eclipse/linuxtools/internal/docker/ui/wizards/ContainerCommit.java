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

public class ContainerCommit extends Wizard {

	private String container;
	private String repo;
	private String tag;
	private String author;
	private String comment;
	private ContainerCommitPage mainPage;

	public ContainerCommit(String container) {
		this.container = container;
	}

	public String getContainerId() {
		return container;
	}

	public String getRepo() {
		return repo;
	}

	public String getTag() {
		return tag;
	}

	public String getAuthor() {
		return author;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		mainPage = new ContainerCommitPage(container);
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		repo = mainPage.getRepo();
		tag = mainPage.getTag();
		author = mainPage.getAuthor();
		comment = mainPage.getComment();

		return true;
	}
}
