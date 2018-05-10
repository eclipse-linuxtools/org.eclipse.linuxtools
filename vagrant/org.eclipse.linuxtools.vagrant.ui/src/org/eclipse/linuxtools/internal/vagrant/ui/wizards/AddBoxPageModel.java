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
