/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.actions;


import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * 
 * @author klee
 *
 */
public class PatchFile {

	private IPath fpath;
	private ArrayList pranges = new ArrayList();
	
	
	public PatchFile(String filePath) {
		fpath = new Path(filePath);
	}
	
	public void addLineRange(int from, int to) {
	
		pranges.add(new PatchRangeElement(from, to, ""));
		
		
	}
	
	public PatchRangeElement[] getRanges() {
		
		
		Object[] tmpEle = pranges.toArray();
		PatchRangeElement[] ret = new PatchRangeElement[tmpEle.length];
		
		for (int i = 0; i < tmpEle.length; i++) {
			ret[i] = (PatchRangeElement) tmpEle[i];
		}
		
	
		return ret;
	}


	public void appendTxtToLastRange(String txt) {
		
		
		((PatchRangeElement)pranges.get(pranges.size()-1)).appendTxt(txt);
	}
	
	public IPath getPath() {
		return fpath;
	}
	
	public int countRanges() {
		return pranges.size();
	}
	
	public boolean equals(Object o) {
		
		if (!(o instanceof PatchFile))
			return false;
		
		PatchFile that = (PatchFile) o;
		// check  fpath  +  count
		if (!this.fpath.removeTrailingSeparator().toString().equals(that.getPath().removeTrailingSeparator().toString()) ||
				this.countRanges() != that.countRanges())
			return false;
		
		// check range elements
		PatchRangeElement[] thatsrange = that.getRanges();
		
		for(int i=0; i<this.countRanges();i++)
			if (!thatsrange[i].equals(pranges.get(i)))
				return false;
		return true;
	}

}