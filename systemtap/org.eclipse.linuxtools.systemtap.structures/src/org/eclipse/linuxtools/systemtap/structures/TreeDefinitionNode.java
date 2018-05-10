/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
