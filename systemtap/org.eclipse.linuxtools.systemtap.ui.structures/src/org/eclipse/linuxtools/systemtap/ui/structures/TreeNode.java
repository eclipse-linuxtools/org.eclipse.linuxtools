/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures;

import java.util.ArrayList;

public class TreeNode {
	public TreeNode(Object d, boolean c) {
		children = new ArrayList<TreeNode>();
		data = d;
		clickable = c;

		if(null == data)
			display = null;
		else
			display = d.toString();
	}
	
	public TreeNode(Object d, String disp, boolean c) {
		children = new ArrayList<TreeNode>();
		data = d;
		display = disp;
		clickable = c;
	}

	public void add(TreeNode item) {
		children.add(item);
	}
	
	public void addAt(TreeNode item, int location) {
		children.add(Math.min(children.size(), location), item);
	}
	
	public int getChildCount() {
		return children.size();
	}
	
	public TreeNode getChildAt(int i){
		if(children.size() > i)
			return children.get(i);
		else
			return null;
	}
	
	public Object getData() {
		return data;
	}
	
	public boolean isClickable() {
		return clickable;
	}
	
	public boolean remove(int i) {
		if(children.size() > i)
			return(null != children.remove(i));
		else 
			return false;
	}
	
	public boolean removeAll() {
		for(int i=children.size()-1; i>=0; i--) {
			this.remove(i);
		}
		return true;
	}
	
	public void setData(Object d) {
		data = d;
	}

	public void setDisplay(String disp) {
		display = disp;
	}

	/**
	 * Restructures the tree so that probes are grouped by type and
	 * functions are sorted alphabetically.
	 */
	public void sortTree() {
		TreeNode temp = null;
		
		sortLevel();
		for(int i=0; i<this.getChildCount(); i++) {
			temp = this.getChildAt(i);
			
			temp.sortTree();
		}
	}

	/**
	 * Performs quicksort on the level.
	 */
	public void sortLevel() {
		int j;
		
		Object children[] = this.children.toArray();
		this.removeAll();
		Sort.quicksort(children, 0, children.length-1);
		
		for(j=0; j<children.length; j++)
			this.add((TreeNode)children[j]);
	}
	
	public String toString() {
		return display;
	}
	
	public void dispose() {
		if(null != children)
			for(int i=children.size()-1; i>=0; i--)
				children.get(i).dispose();
		children = null;
		data = null;
		display = null;
	}
	
	private ArrayList<TreeNode> children;
	private Object data;
	private String display;
	private boolean clickable;
}
