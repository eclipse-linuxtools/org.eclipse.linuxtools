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

import java.util.ArrayList;

public class TreeParent {
	private String name;
	private TreeParent parent;
	private ArrayList<TreeParent> children;
	private float percent = -1;
	
	public String getName() {
		return name;
	}

	public void setParent(TreeParent parent) {
		this.parent = parent;
	}

	public TreeParent getParent() {
		return parent;
	}

	public String toString() {
		return getName();
	}

	public Boolean equals(String s) {
		return getName().equals(s);
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}

	public TreeParent(String name) {
		this.name = name;
		children = new ArrayList<TreeParent>();
	}

	public TreeParent(String name, float percent) {
		this.name = name;
		this.percent = percent;
		children = new ArrayList<TreeParent>();
	}

	public void addChild(TreeParent child) {
		children.add(child);
		child.setParent(this);
		recalculatePercentage();
	}

	public TreeParent getChild(String name) {
		//check if it exists
		for(TreeParent t : children) {
			if (t.equals(name))
				return t;
		}
		return null;
	}

	public void removeChild(TreeParent child) {
		children.remove(child);
		child.setParent(null);
		recalculatePercentage();
	}

	public TreeParent [] getChildren() {
		return (TreeParent [])children.toArray(new TreeParent[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public void clear() {
		children.clear();
	}

	public void recalculatePercentage() {
		if (getPercent() != -1 && (this instanceof PMDso || this instanceof PMFile)){
			percent = 0;
			// Re-sum its children percentages
			for (TreeParent c : getChildren()) {
				percent += c.getPercent();
			}
			// Tell its parent to re-sum too.
			if (getParent().getPercent() != -1) {
				getParent().recalculatePercentage();
			}
		}
	}

}
