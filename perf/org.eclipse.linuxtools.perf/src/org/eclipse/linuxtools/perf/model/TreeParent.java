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

import org.eclipse.core.runtime.IAdaptable;
import java.util.ArrayList;

public class TreeParent implements IAdaptable {
	protected String name;
	private TreeParent parent;
	private ArrayList<TreeParent> children;
	protected float percent = -1;
	
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
	public Object getAdapter(Class key) {
		return null;
	}

	public float getPercent() {
		return percent;
	}	
	
	public TreeParent(String name) {
		this.name = name;
		children = new ArrayList<TreeParent>();
	}
	public void addChild(TreeParent child) {
		children.add(child);
		child.setParent(this);
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
	}
	public TreeParent [] getChildren() {
		return (TreeParent [])children.toArray(new TreeParent[children.size()]);
	}
	public boolean hasChildren() {
		return children.size()>0;
	}
	public void clear() {
		children.clear();
	}
}
