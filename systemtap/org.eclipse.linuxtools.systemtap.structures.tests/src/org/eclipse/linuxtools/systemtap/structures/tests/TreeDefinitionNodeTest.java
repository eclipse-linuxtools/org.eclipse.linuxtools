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
package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TreeDefinitionNodeTest {

    @BeforeEach
    public void setUp() {
        data = new StringBuilder("Object");
        data2 = "Data";
        d = "/usr/share";
        d2 = "/usr";
        s = "String";
        s2 = "bah";

        t = new TreeDefinitionNode(data, s, d, true);
        child = new TreeDefinitionNode(data2, s2, d2, false);
        t.add(child);
    }

    @Test
    public void testTreeDefinitionNode() {
        String d1 = "One";
        String d2 = "two";
        String s1 = "one";

        TreeDefinitionNode t = new TreeDefinitionNode(d1, s1, d2, false);
        assertEquals(0, t.getChildCount(), "Create child count");
        assertEquals(s1, t.toString(), "Create child string");
        assertEquals(d1, t.getData(), "Create child data");
        assertEquals(d2, t.getDefinition(), "Create child definition");
        assertFalse(t.isClickable(), "Create child clickable");
    }

    @Test
    public void testGetDefinition() {
        assertNotSame(d2, t.getDefinition(), "Correct definition");
        assertEquals(d2, ((TreeDefinitionNode)t.getChildAt(0)).getDefinition(), "Correct definition2");
    }

    @Test
    public void testSetDefinition() {
        String s1 = "/user/share/systemtap";
        t.setDefinition(s1);
        assertEquals(s1, t.getDefinition(), "Replaced definition");
    }

    @Test
    public void testDispose() {
        assertNotNull(t.getDefinition());
        t.dispose();
        assertNull(t.getDefinition());
    }

    private TreeDefinitionNode t;
    private TreeDefinitionNode child;
    private Object data;
    private String data2;
    private String s, s2;
    private String d, d2;
}
