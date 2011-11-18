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

public class PMCommand extends TreePercentable {

	public PMCommand(String name) {
		super(name);
	}
	public PMDso getDso(String name) {
		TreeParent tmp = getChild(name);
		if ((tmp != null) && (tmp instanceof PMDso)) {
			return (PMDso)tmp;
		}
		return null;
	}
	public String toString() {
		String prefix = "";
		if (percent != -1)
			prefix = percent + "% in ";
		return prefix + name;
	}	

}
