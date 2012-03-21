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

import org.eclipse.linuxtools.perf.model.PMLineRef;

public class PMSymbol extends TreeParent {
	private Double samples;
	private boolean pathConflictFound = false;

	public String getFunctionName() {
		String tmpName = getName();
		if (tmpName.startsWith("[")) { // filer out the "[.] "
			tmpName = tmpName.substring(4);
		}
		int argloc = tmpName.indexOf("("); // and the (....
		if (argloc != -1) {
			tmpName = tmpName.substring(0,argloc);
		}
		return tmpName;
	}

	public PMSymbol(String symbolName, double samples, float pc) {
		super(symbolName, pc);
		this.samples = samples;
	}

	public void addPercent(Integer lineNum, Float percent) {  //Adds percent to a lineref within this symbol.
		PMLineRef current = (PMLineRef)getChild(lineNum.toString());
		if (current == null) {
			current = new PMLineRef(lineNum, percent);
			addChild(current);
		} else {
			current.addPercent(percent);
		}
	}

	public String toString() {
		return getPercent() + "% in " + getName() + " (" + samples + " samples)" + (pathConflictFound ? "(Warning multiple paths found for this symbol!)" : "");
	}

	public void markConflict() {
		pathConflictFound = true;
	}

	public boolean conflicted() {
		return pathConflictFound;
	}
}
