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
		String tmpName = name;
		if (tmpName.startsWith("[")) { // filer out the "[.] "
			tmpName = tmpName.substring(4);
		}
		int argloc = tmpName.indexOf("("); // and the (....
		if (argloc != -1) {
			tmpName = tmpName.substring(0,argloc);
		}
		return tmpName;
	}
	public PMSymbol(String symbolName, Double samples2, float pc) {
		super(symbolName);
		this.samples = samples2;
		this.percent = pc;
	}
	public void addChild(TreeParent child) {
		//TODO clean this up, this is a bit hacky. TreePercentable should be changed to an interface and the add/remove-child percent methods should be moved to TreeParent 
		float tmp = percent;
		super.addChild(child);
		percent = tmp;
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
		return percent + "% in " + name + " (" + samples + " samples)" + (pathConflictFound ? "(Warning multiple paths found for this symbol!)" : "");
	}
	public PMFile getFile() {
		if (!(super.getParent() instanceof PMFile))
			return null;
		return (PMFile)super.getParent();
	}
	
	public void markConflict() {
		pathConflictFound = true;
	}
	public boolean conflicted() {
		return pathConflictFound;
	}
}
