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
package org.eclipse.linuxtools.internal.gprof.symbolManager;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;

/**
 * Arc structure for call-graph.
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CallGraphArc {
	
	/** source vertice of arc. */
    public final CallGraphNode parent;
    /** dest vertice of arc. */
    public final CallGraphNode child;
    /** number of occurence */
    private int count;
	private IProject project;
    
    /** The location (source path) of the function call */
    public String parentPath;
    /** The location (line number) of the function call */
    public int parentLine;

    /**
     * Constructor
     * @param parent the caller node
     * @param parentAddress the location of the function call
     * @param child the callee node
     * @param count how many function calls have been performed
     * @param program the program
     */
    public CallGraphArc(CallGraphNode parent, IAddress parentAddress, CallGraphNode child, int count, IBinaryObject program, IProject project) {
    	this.parent = parent;
    	this.child  = child;
    	this.count  = count;
		this.parentPath = STSymbolManager.sharedInstance.getFileName(program, parentAddress, project);
		this.parentLine = STSymbolManager.sharedInstance.getLineNumber(program, parentAddress, project);
		this.project = project;
    }

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Print on System.out, for debugging purpose
	 */
	public void print() {
		System.out.print("    ");
		System.out.print(this.parent.getSymbol().getName());
		System.out.print(" -> ");
		System.out.print(this.child.getSymbol().getName());
		System.out.print(" :: ");
		System.out.println(this.count);
	}

	public IProject getProject() {
		return this.project;
	}
    
    
}
