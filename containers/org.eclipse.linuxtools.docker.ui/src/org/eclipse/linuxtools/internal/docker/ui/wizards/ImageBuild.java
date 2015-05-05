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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.Wizard;

public class ImageBuild extends Wizard {

	private ImageBuildPage mainPage;
	private String imageName;
	private IPath directory;
	private int lines;

	public ImageBuild() {
		super();
	}

	public String getImageName() {
		return imageName;
	}

	public IPath getDirectory() {
		return directory;
	}

	public int getNumberOfLines() {
		return lines;
	}

	@Override
	public void addPages() {
		mainPage = new ImageBuildPage();
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	private int numberOfLines() throws IOException {
		String fileName = directory.append("Dockerfile").toString(); //$NON-NLS-1$
		InputStream is = null;
		int count = 0;
		boolean empty = false;
		try {
			is = new BufferedInputStream(new FileInputStream(fileName));
			byte[] c = new byte[1024];
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
		} finally {
			if (is != null)
				is.close();
		}
		return (count == 0 && !empty) ? 1 : count;
	}

	@Override
	public boolean performFinish() {
		imageName = mainPage.getImageName();
		directory = new Path(mainPage.getDirectory());

		try {
			lines = numberOfLines();
		} catch (IOException e) {
			// do nothing
		}

		return true;
	}

}
