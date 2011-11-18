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
package org.eclipse.linuxtools.perf.model;

import org.eclipse.linuxtools.perf.model.PMFile;

public class PMDso extends TreePercentable {
	private String path = null;
	private boolean kernel = false; //Is this DSO a kernel dso?
	
	public boolean isKernelDso() {
		return kernel;
	}
	
	public PMDso(String dsoName, boolean kernel) {
		super(dsoName);
		this.kernel = kernel;
	}
	public PMFile getFile(String fileName) {
		//check if exists else make a new one.
		PMFile tmp = (PMFile)getChild(fileName);
		if (tmp != null) {
			return tmp;
		} else {
			tmp = new PMFile(fileName);
			addChild(tmp);
			return tmp;
		} 
	}

	public void setPath(String filePath) {
		path = filePath;
	}
	public String toString() {
		String prefix = "";
		if (percent != -1)
			prefix += percent + "% in ";
		if (kernel == true)
			prefix += "[k] ";
		if (path != null) {
			return prefix + name + " (at " + path + ")";
		}
		return prefix + name;
	}
	public String getPath() {
		return path;
	}
	
	public PMCommand getCommand() {
		if (!(super.getParent() instanceof PMCommand))
			return null;
		return (PMCommand)super.getParent();
	}
}
