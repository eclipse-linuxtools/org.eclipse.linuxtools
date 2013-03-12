/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.model;

import org.eclipse.linuxtools.internal.perf.model.PMFile;

public class PMDso extends TreeParent {
	private String path = null;
	private boolean kernel = false; //Is this DSO a kernel dso?
	
	public boolean isKernelDso() {
		return kernel;
	}
	
	public PMDso(String dsoName, boolean kernel) {
		super(dsoName, 0);
		this.kernel = kernel;
	}

	public PMFile getFile(String fileName) {
		//check if exists else make a new one.
		PMFile tmp = (PMFile) getChild(fileName);
		if (tmp != null) {
		} else {
			tmp = new PMFile(fileName);
			addChild(tmp);
		}
		return tmp;
	}

	public void setPath(String filePath) {
		path = filePath;
	}
	@Override
	public String toString() {
		String prefix = "";
		if (getPercent() != -1)
			prefix += getPercent() + "% (" + getFormattedSamples() + " samples) in ";
		if (kernel == true)
			prefix += "[k] ";
		if (path != null) {
			return prefix + getName() + " (at " + path + ")";
		}
		return prefix + getName();
	}
	public String getPath() {
		return path;
	}
}
