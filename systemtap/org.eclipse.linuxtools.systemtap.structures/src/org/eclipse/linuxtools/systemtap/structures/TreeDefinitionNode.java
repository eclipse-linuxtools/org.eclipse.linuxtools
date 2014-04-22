/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures;

public class TreeDefinitionNode extends TreeNode {
    private String definition;

    public TreeDefinitionNode(Object d, String disp, String def, boolean c) {
        super(d, disp, c);
        definition = def;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String d) {
        definition = d;
    }

    @Override
    public void dispose() {
        super.dispose();
        definition = null;
    }
}
