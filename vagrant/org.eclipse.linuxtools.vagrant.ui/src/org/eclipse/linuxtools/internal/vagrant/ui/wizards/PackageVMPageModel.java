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

package org.eclipse.linuxtools.internal.vagrant.ui.wizards;

public class PackageVMPageModel extends BaseDatabindingModel {

	public static final String BOX_NAME = "boxName"; //$NON-NLS-1$
	public static final String BOX_FOLDER = "boxFolder"; //$NON-NLS-1$

	private String boxName;
	private String boxFolder;

	public String getBoxName() {
		return boxName;
	}

	public String getBoxFolder() {
		return boxFolder;
	}

	public void setBoxName(final String boxName) {
		firePropertyChange(BOX_NAME, this.boxName, this.boxName = boxName);
	}

	public void setBoxFolder(final String boxFolder) {
		firePropertyChange(BOX_FOLDER, this.boxFolder,
				this.boxFolder = boxFolder);
	}
}
