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

import java.util.LinkedList;

import org.eclipse.cdt.core.IBinaryParser.ISymbol;

/**
 * This class represents a node of a call graph
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CallGraphNode {

    private final ISymbol symbol;
    private final LinkedList<CallGraphArc> parents = new LinkedList<CallGraphArc>();
    private final LinkedList<CallGraphArc> children = new LinkedList<CallGraphArc>();

    /**
     * Constructor
     *
     * @param symbol
     */
    public CallGraphNode(ISymbol symbol) {
        this.symbol = symbol;
    }

    /**
     * @param parent
     * @return the input arc caming from the given parent, if any.
     */
    public CallGraphArc getInputArc(CallGraphNode parent) {
        for (CallGraphArc inputArc : parents) {
            if (inputArc.parent == parent)
                return inputArc;
        }
        return null;
    }

    /**
     *
     * @param child
     * @return the arc to the given child, if any.
     */
    public CallGraphArc getOutputArc(CallGraphNode child) {
        for (CallGraphArc outputArc : children) {
            if (outputArc.child == child)
                return outputArc;
        }
        return null;
    }

    /**
     * @return the symbol
     */
    public ISymbol getSymbol() {
        return symbol;
    }

    /**
     * @return the parents
     */
    public LinkedList<CallGraphArc> getParents() {
        return parents;
    }

    /**
     * @return the children
     */
    public LinkedList<CallGraphArc> getChildren() {
        return children;
    }

    /**
     * Print on System.out, for debugging purpose
     */
    public void print() {
        System.out.println(this.symbol.getName());
        System.out.println(" -- parents --"); //$NON-NLS-1$
        for (CallGraphArc arc : this.parents) {
            arc.print();
        }
        System.out.println(" -- children --"); //$NON-NLS-1$
        for (CallGraphArc arc : this.children) {
            arc.print();
        }
    }

    /**
     * @return the total calls for this function
     */
    public int getCalls() {
        int ret = 0;
        for (CallGraphArc arc : this.parents) {
            ret += arc.getCount();
        }
        return ret;
    }

}
