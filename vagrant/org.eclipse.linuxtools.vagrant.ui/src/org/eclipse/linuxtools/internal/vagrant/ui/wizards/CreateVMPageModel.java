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

import java.util.HashMap;
import java.util.Map;

public class CreateVMPageModel extends BaseDatabindingModel {

	public static final String VM_NAME = "VMName"; //$NON-NLS-1$
	public static final String BOX_REF = "boxRef"; //$NON-NLS-1$
	public static final String VM_FILE = "VMFile"; //$NON-NLS-1$
	public static final String V_FILE_MODE = "VFileMode"; //$NON-NLS-1$
	public static final String ENVIRONMENT = "environment"; //$NON-NLS-1$

	private String vmName;
	private String vmFile;
	private String boxRef;
	private boolean vFileMode;
	private Map<String, String> environment = new HashMap<>();

	public String getVMName() {
		return vmName;
	}

	public String getBoxRef() {
		return boxRef;
	}

	public String getVMFile() {
		return vmFile;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public boolean getVFileMode() {
		return vFileMode;
	}

	public void setVMName(final String vmName) {
		firePropertyChange(VM_NAME, this.vmName, this.vmName = vmName);
	}

	public void setBoxRef(final String boxRef) {
		firePropertyChange(BOX_REF, this.boxRef, this.boxRef = boxRef);
	}

	public void setVMFile(final String vmFile) {
		firePropertyChange(VM_FILE, this.vmFile, this.vmFile = vmFile);
	}

	public void setVFileMode(final boolean vFileMode) {
		firePropertyChange(V_FILE_MODE, this.vFileMode,
				this.vFileMode = vFileMode);
	}

	public void setEnvironment(final Map<String, String> map) {
		firePropertyChange(ENVIRONMENT, this.environment,
				this.environment = map);
	}
}
