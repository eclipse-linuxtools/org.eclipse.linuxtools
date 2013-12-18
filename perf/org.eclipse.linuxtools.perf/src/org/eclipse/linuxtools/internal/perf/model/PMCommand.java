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

public class PMCommand extends TreeParent {

	public PMCommand(String name) {
		super(name, 100);
		/*
		 *  The underlying report truncates percentages in some cases
		 *  so taking a sum will not always give 100, but we know that
		 *  the only command that could have run is the binary we specified.
		 */
	}

	@Override
	public String toString() {
		String prefix = "";
		if (getPercent() != -1) {
			prefix = getPercent() + "%  (" + getFormattedSamples() + " samples) in ";
		}
		return prefix + getName();
	}	

}
