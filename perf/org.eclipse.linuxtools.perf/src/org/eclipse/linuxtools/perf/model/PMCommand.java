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

public class PMCommand extends TreeParent {

	public PMCommand(String name) {
		super(name, 0);
	}

	public String toString() {
		String prefix = "";
		if (getPercent() != -1)
			prefix = getPercent() + "% in ";
		return prefix + getName();
	}	

}
