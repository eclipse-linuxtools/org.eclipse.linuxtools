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

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.eclipse.linuxtools.internal.gprof.symbolManager.Bucket;
import org.eclipse.linuxtools.internal.gprof.symbolManager.CallGraphNode;

/**
 * Root node of histogram
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistRoot extends AbstractTreeElement {

	private final LinkedList<HistFile> children = new LinkedList<>();
	
	/** The decoded gmon to display */
	public final GmonDecoder decoder;
	
	/**
	 * Constructor
	 * @param decoder 
	 */
	public HistRoot(GmonDecoder decoder) {
		super(null);
		this.decoder = decoder;
	}
	
	private HistFile getChild(String p) {
		for (HistFile f : this.children) {
			if (p != null) {
				if (p.equals(f.sourcePath)) return f;
			} else if (f.sourcePath == null) return f;
		}
		HistFile f = new HistFile(this, p);
		this.children.add(f);
		return f;
	}
	
	/**
	 * Add a bucket to the tree representation of the gmon file
	 * @param b a bucket
	 * @param s a symbol (the bucket belong to this symbol)
	 * @param program the program
	 */
	public void addBucket(Bucket b, ISymbol s, IBinaryObject program) {
		String path = decoder.getFileName(s);
		HistFile hf = getChild(path);
		hf.addBucket(b, s, program);
	}
	
	/**
	 * Add a callgraph node to the tree representation of the gmon file
	 * @param node
	 */
	public void addCallGraphNode(CallGraphNode node) {
		ISymbol s = node.getSymbol();
		String path = decoder.getFileName(s);
		HistFile hf = getChild(path);
		hf.addCallGraphNode(node);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement#getChildren()
	 */
	@Override
	public LinkedList<? extends TreeElement> getChildren() {
		return this.children;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement#getName()
	 */
	@Override
	public String getName() {
		return Messages.HistRoot_Summary;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.histogram.AbstractTreeElement#getCalls()
	 */
	@Override
	public int getCalls() {
		return -1;
	}

	public IProject getProject() {
		return decoder.getProject();
	}

}
