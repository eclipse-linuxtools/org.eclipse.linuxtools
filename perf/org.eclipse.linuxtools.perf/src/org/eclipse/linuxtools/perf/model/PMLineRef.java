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

public class PMLineRef extends TreeParent {
	protected int ln;
	protected float pc;
	public PMLineRef(int lineNum, float percent) {
		super(""+lineNum);
		ln = lineNum;
		pc = percent;
	}
	public void addPercent(Float addpc) {
		pc += addpc;
	}
	public float getPercent() {
		return pc;
	}
	public PMSymbol getSymbol() {
		if (!(super.getParent() instanceof PMSymbol))
			return null;
		return (PMSymbol)super.getParent();
	}
	public String toString() {
		return pc + "% on line " + ln;
	}
}
