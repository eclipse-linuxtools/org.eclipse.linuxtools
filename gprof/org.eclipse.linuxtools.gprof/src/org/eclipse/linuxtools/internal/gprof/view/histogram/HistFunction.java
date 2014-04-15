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

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gprof.symbolManager.Bucket;
import org.eclipse.linuxtools.internal.gprof.symbolManager.CallGraphArc;
import org.eclipse.linuxtools.internal.gprof.symbolManager.CallGraphNode;

/**
 * Tree node corresponding to a function
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistFunction extends AbstractTreeElement {

    /** The sympbol to display */
    public final ISymbol symbol;
    private final LinkedList<HistLine> children = new LinkedList<>();
    private CGCategory parentsFunctions;
    private CGCategory childrenFunctions;

    private static HashMap<ISymbol, Integer> histSym = new HashMap<>();

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
            if (l.line == line) {
                return l;
            }
        }
        HistLine l = new HistLine(this, line);
        this.children.add(l);
        return l;
    }


    void addBucket(Bucket b, IBinaryObject program) {
        int lineNumber = -1;
        IAddress address = program.getAddressFactory().createAddress(String.valueOf(b.startAddr));
        lineNumber = STSymbolManager.sharedInstance.getLineNumber(program, address, getProject());
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

    @Override
    public LinkedList<? extends TreeElement> getChildren() {
        return this.children;
    }

    @Override
    public int getCalls() {
        return this.calls;
    }

    @Override
    public String getName() {
        return STSymbolManager.sharedInstance.demangle(this.symbol, getProject());
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

    @Override
    public int getSourceLine() {
        return STSymbolManager.sharedInstance.getLineNumber(symbol, getProject());
    }

    @Override
    public String getSourcePath() {
        return ((HistRoot)getRoot()).decoder.getFileName(symbol);
    }

    /**
     * @return the function samples
     */
    public static int getSamples(ISymbol sym){
        if (histSym.containsKey(sym)) {
            return histSym.get(sym);
        } else {
            return 0;
        }
    }

    private IProject getProject() {
        return ((HistRoot)getParent().getParent()).getProject();
    }

}
