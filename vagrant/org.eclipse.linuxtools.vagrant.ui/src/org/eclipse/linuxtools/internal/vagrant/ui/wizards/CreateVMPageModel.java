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

public class CreateVMPageModel extends BaseDatabindingModel {

	public static final String VM_NAME = "VMName";
	public static final String BOX_NAME = "boxName";
	public static final String VM_FILE = "VMFile";
	public static final String BOX_LOC_MODE = "boxLocMode";

	private String vmName;
	private String vmFile;
	private String boxName;
	private boolean boxLocMode;

	public String getVMName() {
		return vmName;
	}

	public String getBoxName() {
		return boxName;
	}

	public String getVMFile() {
		return vmFile;
	}

	public boolean getBoxLocMode() {
		return boxLocMode;
	}

	public void setVMName(final String vmName) {
		firePropertyChange(VM_NAME, this.vmName, this.vmName = vmName);
	}

	public void setBoxName(final String boxName) {
		firePropertyChange(BOX_NAME, this.boxName, this.boxName = boxName);
	}

	public void setVMFile(final String vmFile) {
		firePropertyChange(VM_FILE, this.vmFile, this.vmFile = vmFile);
	}

	public void setBoxLocMode(final boolean boxLocMode) {
		firePropertyChange(BOX_LOC_MODE, this.boxLocMode, this.boxLocMode = boxLocMode);
	}

}
