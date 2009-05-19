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
package com.st.stgprof.view.histogram;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;

import com.st.flexperf.binutils.utils.STSymbolManager;
import com.st.stgprof.symbolManager.Bucket;
import com.st.stgprof.symbolManager.CallGraphArc;
import com.st.stgprof.symbolManager.CallGraphNode;

/**
 * Tree node corresponding to a function
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistFunction extends AbstractTreeElement {

	/** The sympbol to display */
	public final ISymbol symbol;
	private final LinkedList<HistLine> children = new LinkedList<HistLine>();
	private CGCategory parentsFunctions;
	private CGCategory childrenFunctions;
	
	private static HashMap<ISymbol, Integer> histSym = new HashMap<ISymbol, Integer>();
	
	/**
	 * Constructor 
	 * @param parent
	 * @param s
	 */
	public HistFunction(HistFile parent, ISymbol s) {
		super(parent);
		this.symbol = s;
		histSym.put(s, 0);
	}
	
	/**
	 * Gets the tree item corresponding to the given line.
	 * Lazily create it if needed.
	 * @param line
	 * @return a {@link HistFunction}
	 */
	private HistLine getChild(int line) {
		for (HistLine l : this.children) {
			if (l.line == line) return l;
		}
		HistLine l = new HistLine(this, line);
		this.children.add(l);
		return l;
	}
	

	void addBucket(Bucket b, IBinaryObject program) {
		int lineNumber = -1;
		IAddress address = program.getAddressFactory().createAddress(String.valueOf(b.start_addr));
		lineNumber = STSymbolManager.sharedInstance.getLineNumber(program, address);
		HistLine hf = getChild(lineNumber);
		hf.addBucket(b);
		histSym.put(symbol, b.time + histSym.get(symbol));
	}

	void addCallGraphNode(CallGraphNode node) {
		LinkedList<CallGraphArc> parents = node.getParents();
		LinkedList<CallGraphArc> children = node.getChildren();
		if (parents.size() != 0) {
			this.parentsFunctions = new CGCategory(this, CGCategory.PARENTS, node.getParents());
		}
		if (children.size() != 0) {
			this.childrenFunctions = new CGCategory(this, CGCategory.CHILDREN, node.getChildren());
		}
		this.calls = node.getCalls();
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.visualprof.stgprof.view.histogram.TreeElement#getChildren()
	 */
	public LinkedList<? extends TreeElement> getChildren() {
		return this.children;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.visualprof.stgprof.view.histogram.TreeElement#getCalls()
	 */
	public int getCalls() {
		return this.calls;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.visualprof.stgprof.view.histogram.TreeElement#getName()
	 */
	public String getName() {
		return STSymbolManager.sharedInstance.demangle(this.symbol);
	}

	/**
	 * @return the parentsFunctions
	 */
	public CGCategory getParentsFunctions() {
		return parentsFunctions;
	}

	/**
	 * @return the childrenFunctions
	 */
	public CGCategory getChildrenFunctions() {
		return childrenFunctions;
	}

	/* (non-Javadoc)
	 * @see com.st.visualprof.stgprof.view.histogram.TreeElement#getSourceLine()
	 */
	public int getSourceLine() {
		return STSymbolManager.sharedInstance.getLineNumber(symbol);
	}

	/* (non-Javadoc)
	 * @see com.st.visualprof.stgprof.view.histogram.TreeElement#getSourcePath()
	 */
	public String getSourcePath() {
		return ((HistRoot)getRoot()).decoder.getFileName(symbol);
	}
	
	/**
	 * @return the function samples
	 */
	public static int getSamples(ISymbol sym){
		if (histSym.containsKey(sym))
			return histSym.get(sym);
		else return 0;
	}

}
