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

public class PMFile extends TreePercentable {
	protected String path;
	
	public PMFile(String fileName) {
		super(fileName);
		path = fileName;
	}

	/*public void setPath(String filePath) {
		this.path = filePath;
	}*/
	public String getPath() {
		return this.path;
	}
	
	public String toString() {
		String prefix = "";
		if (percent != -1)
			prefix = percent + "% in ";
		return prefix + path;
	}
	public PMDso getDso() {
		if (!(super.getParent() instanceof PMDso))
			return null;
		return (PMDso)super.getParent();
	}
}
