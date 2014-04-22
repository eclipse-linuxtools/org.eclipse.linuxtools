/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.FunctionParser;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;


/**
 * A structure for containing extra information of SystemTap function parameters.
 * @since 3.0
 */
public class FuncparamNodeData implements ISingleTypedNode {
    static final String ID = "FuncparamNodeData"; //$NON-NLS-1$
    private final String type;

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public String getType() {
        return type;
    }

    /**
     * Create a new instance of function parameter information. (Note that the name of a function
     * or parameter is stored in a {@link TreeNode}, not here.)
     * @param type The <code>String</code> representation of the parameter's type.
     * Pass <code>null</code> if the type is unknown.
     */
    public FuncparamNodeData(String type) {
        this.type = type == null ? FunctionParser.UNKNOWN_TYPE : type; // Parameters can't be void.
    }
}
