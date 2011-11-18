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

public class TreePercentable extends TreeParent {
	
	public TreePercentable(String name) {
		super(name);
	}
	
	public void addChild(TreeParent child) {
		super.addChild(child);
		recalculatePercentage();
	}
	
	public void removeChild(TreeParent child) {
		super.removeChild(child);
		recalculatePercentage();
	}
	
	public void recalculatePercentage() {
		percent = 0;
		//Re-sum its children percentages
		for(TreeParent c : this.getChildren()) {
			percent += c.getPercent();
		}
		//Tell its parent to re-sum too.
		if (this.getParent() instanceof TreePercentable) {
			((TreePercentable)this.getParent()).recalculatePercentage();
		}
	}
	
}
