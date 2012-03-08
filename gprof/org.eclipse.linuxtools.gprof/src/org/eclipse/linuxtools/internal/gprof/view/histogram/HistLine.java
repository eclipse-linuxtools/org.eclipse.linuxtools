/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.view.histogram;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gprof.symbolManager.Bucket;


/**
 * Tree node corresponding to a line
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistLine extends AbstractTreeElement {


	public final int line;
	private final LinkedList<HistBucket> children = new LinkedList<HistBucket>();

	/**
	 * Constructor 
	 * @param parent
	 * @param lineNumber
	 */
	public HistLine(HistFunction parent, int lineNumber) {
		super(parent);
		this.line = lineNumber;
	}

	void addBucket(Bucket b) {
		this.children.add(new HistBucket(this,b));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement#getChildren()
	 */
	public LinkedList<? extends TreeElement> getChildren() {
		return this.children;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.AbstractTreeElement#getCalls()
	 */
	public int getCalls() {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement#getName()
	 */
	public String getName() {
		String functionName = getParent().getName();
		return functionName + " (" + getParent().getParent().getName() + ":" + this.line + ")";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.AbstractTreeElement#getSamples()
	 */
	public int getSamples() {
		int ret = 0;
		for (HistBucket b : children) {
			ret += b.getSamples();
		}
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.AbstractTreeElement#getSourceLine()
	 */
	public int getSourceLine() {
		return this.line;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.AbstractTreeElement#getSourcePath()
	 */
	public String getSourcePath() {
		return getParent().getParent().getSourcePath();
	}

}
