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

package org.eclipse.linuxtools.systemtap.structures;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	public TreeNode(Object d, boolean c) {
		children = new ArrayList<>();
		data = d;
		clickable = c;

		if(null == data) {
			display = null;
		} else {
			display = d.toString();
		}
	}

	public TreeNode(Object d, String disp, boolean c) {
		children = new ArrayList<>();
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
		if(children.size() > i) {
			return children.get(i);
		} else {
			return null;
		}
	}

	public Object getData() {
		return data;
	}

	public boolean isClickable() {
		return clickable;
	}

	public boolean remove(int i) {
		if(children.size() > i) {
			return(null != children.remove(i));
		} else {
			return false;
		}
	}

	public boolean removeAll() {
		for(int i=children.size()-1; i>=0; i--) {
			remove(i);
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
		sortLevel();
		for (TreeNode temp : children) {
			temp.sortTree();
		}
	}

	/**
	 * Performs quicksort on the level.
	 */
	public void sortLevel() {
		TreeNode[] children = this.children.toArray(new TreeNode[0]);
		removeAll();
		Sort.quicksort(children, 0, children.length - 1);

		for (TreeNode child : children) {
			add(child);
		}
	}

	@Override
	public String toString() {
		return display;
	}

	public void dispose() {
		if (null != children) {
			for (TreeNode child : children) {
				child.dispose();
			}
		}
		children = null;
		data = null;
		display = null;
	}

	/**
	 * @since 2.0
	 */
	public TreeNode getChildByName(String name){
		for (TreeNode child : children) {
			if (child.toString().equals(name)) {
				return child;
			}
		}

		return null;
	}

	private List<TreeNode> children;
	private Object data;
	private String display;
	private boolean clickable;
}
