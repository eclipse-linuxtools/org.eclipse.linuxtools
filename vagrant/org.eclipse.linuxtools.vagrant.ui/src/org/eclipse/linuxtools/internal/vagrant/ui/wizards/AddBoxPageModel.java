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

public class AddBoxPageModel extends BaseDatabindingModel {

	public static final String BOX_NAME = "boxName"; //$NON-NLS-1$
	public static final String BOX_LOC = "boxLoc"; //$NON-NLS-1$

	private String boxName;
	private String boxLoc;

	public String getBoxName() {
		return boxName;
	}

	public String getBoxLoc() {
		return boxLoc;
	}

	public void setBoxName(final String boxName) {
		firePropertyChange(BOX_NAME, this.boxName, this.boxName = boxName);
	}

	public void setBoxLoc(final String boxLoc) {
		firePropertyChange(BOX_LOC, this.boxLoc, this.boxLoc = boxLoc);
	}
}
