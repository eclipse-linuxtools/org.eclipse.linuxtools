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
 * Tree Item displaying a bucket.
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistBucket extends AbstractTreeElement {

	public final Bucket bucket;
	
	/**
	 * Constructor
	 * @param parent the parent of this tree node
	 * @param b the object to display in the tree
	 */
	public HistBucket(HistLine parent, Bucket b) {
		super(parent);
		this.bucket = b;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.AbstractTreeElement#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement#getChildren()
	 */
	@Override
	public LinkedList<? extends TreeElement> getChildren() {
		return null;
	}


	@Override
	public int getCalls() {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement#getName()
	 */
	@Override
	public String getName() {
		return "0x" + Long.toHexString(bucket.start_addr); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.AbstractTreeElement#getSamples()
	 */
	@Override
	public int getSamples() {
		return bucket.time;
	}

}
