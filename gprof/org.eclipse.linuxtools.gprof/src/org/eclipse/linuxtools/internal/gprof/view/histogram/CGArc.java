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

import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gprof.symbolManager.CallGraphArc;


/**
 * Node in displayed call graph corresponding to {@link CallGraphArc}
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CGArc extends AbstractTreeElement {

    /** The displayed arc - or function call */
    public final CallGraphArc arc;

    /**
     * Constructor
     * @param cat the parent category of this tree node
     * @param arc the object to display in the tree
     */
    public CGArc(CGCategory cat, CallGraphArc arc) {
        super(cat);
        this.arc = arc;
    }

    @Override
    public LinkedList<? extends TreeElement> getChildren() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public int getCalls() {
        return arc.getCount();
    }

    @Override
    public int getSamples() {
        Object o = this.getParent();
        CGCategory category = (CGCategory) o;
        ISymbol symbol;
        if (CGCategory.CHILDREN.equals(category.category)) {
            symbol = arc.child.getSymbol();
        } else {
            symbol = arc.parent.getSymbol();
        }
        return HistFunction.getSamples(symbol);
    }

    public String getFunctionName() {
        Object o = this.getParent();
        CGCategory category = (CGCategory) o;
        ISymbol symbol;
        if (CGCategory.CHILDREN.equals(category.category)) {
            symbol = arc.child.getSymbol();
        } else {
            symbol = arc.parent.getSymbol();
        }
        return STSymbolManager.sharedInstance.demangle(symbol, arc.getProject());
    }

    @Override
    public String getName() {
        String functionName = getFunctionName();
        Path p = new Path(getSourcePath());
        return functionName + " (" + p.lastSegment() + ":" + getSourceLine() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int getSourceLine() {
        Object o = this.getParent();
        CGCategory category = (CGCategory) o;
        if (CGCategory.CHILDREN.equals(category.category)) {
            ISymbol symbol = arc.child.getSymbol();
            return STSymbolManager.sharedInstance.getLineNumber(symbol, arc.getProject());
        } else {
            return arc.parentLine;
        }
    }

    @Override
    public String getSourcePath() {
        Object o = this.getParent();
        CGCategory category = (CGCategory) o;
        if (CGCategory.CHILDREN.equals(category.category)) {
            ISymbol symbol  = arc.child.getSymbol();
            return ((HistRoot)getRoot()).decoder.getFileName(symbol);
        } else {
            if (arc.parentPath == null) {
                return "??"; //$NON-NLS-1$
            }
            return arc.parentPath;
        }
    }

}
