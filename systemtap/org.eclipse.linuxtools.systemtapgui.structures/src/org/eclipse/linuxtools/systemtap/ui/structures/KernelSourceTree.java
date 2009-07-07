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

import java.io.File;

public class KernelSourceTree {
	public TreeNode getTree() {
		return kernelTree;
	}

	/**
	 * Builds the kernel tree from file parameter direct and stores the excluded string array.
	 * 
	 * @param direct The file to include into the tree.
	 * @param excluded The string array to store as excluded.
	 */
	public void buildKernelTree(String direct, String[] excluded) {
		this.excluded = excluded;
		try {
			File f = new File(direct);
			
			kernelTree = new TreeNode(f, f.getName(), false);
			addLevel(kernelTree);
		} catch(Exception e) {
			kernelTree = null;
		}
	}
	
	/**
	 * Adds a level to the kernel source tree.
	 * 
	 * @param top The top of the tree to add a level to.
	 */
	private void addLevel(TreeNode top) {
		boolean add;
		TreeNode current;
		File f = (File)top.getData();
		
		File[] fs = f.listFiles(new CCodeFileFilter());
		for(int i=0; i<fs.length; i++) {
			add = true;
			for(int j=0; j<excluded.length; j++) {
				if(fs[i].isDirectory() && fs[i].getName().equals(excluded[j].substring(0, excluded[j].length()-1)))
					add = false;
			}
			if(add) {
				current = new TreeNode(fs[i], fs[i].getName(), !fs[i].isDirectory());
				top.add(current);
				if(fs[i].isDirectory()) {
					addLevel(top.getChildAt(top.getChildCount()-1));
					if(0 == current.getChildCount())
						top.remove(top.getChildCount()-1);
				}
			}
		}
		top.sortLevel();
	}
	
	public void dispose() {
		kernelTree = null;
	}
	
	private TreeNode kernelTree;
	private String[] excluded;
}
