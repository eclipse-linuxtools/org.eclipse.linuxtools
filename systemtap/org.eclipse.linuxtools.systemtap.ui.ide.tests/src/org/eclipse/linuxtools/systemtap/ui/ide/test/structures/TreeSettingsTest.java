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

package org.eclipse.linuxtools.systemtap.ui.ide.test.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TreeSettings;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.FuncparamNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.FunctionNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbeNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbevarNodeData;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TreeSettingsTest {

    @BeforeEach
    public void setUp() {
        TreeSettings.setTrees(new TreeNode("func", false), new TreeNode("probe", false));
    }

    @Test
    public void testSetTrees() {
        TreeNode t = new TreeNode("f", false);
        TreeNode t1 = new TreeNode("as", "as3", false);
        TreeNode t2 = new TreeNode(null, false);
        TreeNode t3 = null;

        assertTrue(TreeSettings.setTrees(t, t), "Set tree fine1");
        assertTrue(TreeSettings.setTrees(t1, t1), "Set tree fine2");
        assertTrue(TreeSettings.setTrees(t2, t2), "Set tree null obj");
        assertFalse(TreeSettings.setTrees(t3, t3), "Set trees null");
        assertFalse(TreeSettings.setTrees(t3, t), "Set func null");
        assertFalse(TreeSettings.setTrees(t, t3), "Set probe null");
    }

    @Test
    public void testGetTreeFileDate() {
        long d1 = TreeSettings.getTreeFileDate();
        long d2 = TreeSettings.getTreeFileDate();
        assertEquals(d1, d2, "TreeDate same");

        TreeSettings.setTrees(new TreeNode("f", false), new TreeNode("p", false));
        d2 = TreeSettings.getTreeFileDate();

        assertTrue(d1 < d2, "TreeDate changed");
    }

    @Test
    public void testGetFunctionTree() {
        TreeNode temp;
        TreeNode t = new TreeNode("f", false);
        TreeNode t1 = new TreeNode(null, false);
        TreeNode t2 = new TreeNode(new StringBuilder("asfd"), true);

        TreeSettings.setTrees(t, t);
        temp = TreeSettings.getFunctionTree();
        assertEquals(0, temp.getChildCount(), "Funcs no children");
        assertEquals(t.getData().toString(), temp.getData().toString(), "Funcs object");
        assertEquals(t.toString(), temp.toString(), "Funcs display");
        assertEquals(t.isClickable(), temp.isClickable(), "Funcs clickable");

        TreeSettings.setTrees(t1, t);
        temp = TreeSettings.getFunctionTree();
        assertEquals(0, temp.getChildCount(), "Funcs no children");
        assertEquals(t1.getData(), temp.getData(), "Funcs object");
        assertEquals(t1.toString(), temp.toString(), "Funcs display");
        assertEquals(t1.isClickable(), temp.isClickable(), "Funcs clickable");

        TreeSettings.setTrees(t2, t);
        temp = TreeSettings.getFunctionTree();
        assertEquals(0, temp.getChildCount(), "Funcs no children");
        assertEquals(t2.getData().toString(), temp.getData(), "Funcs object");
        assertEquals(t2.toString(), temp.toString(), "Funcs display");
        assertEquals(t2.isClickable(), temp.isClickable(), "Funcs clickable");

        t.add(t2);
        t.add(t1);
        TreeSettings.setTrees(t, t);
        temp = TreeSettings.getFunctionTree();
        assertEquals(2, temp.getChildCount(), "Funcs has children");
        assertEquals(t2.getData().toString(), temp.getChildAt(0).getData(), "Funcs child object");
        assertEquals(t2.toString(), temp.getChildAt(0).toString(), "Funcs child display");
    }

    @Test
    public void testGetProbeTree() {
        TreeNode temp;
        TreeNode t = new TreeNode("f", false);
        TreeNode t1 = new TreeNode(null, false);
        TreeNode t2 = new TreeNode(new StringBuilder("asfd"), true);

        TreeSettings.setTrees(t, t);
        temp = TreeSettings.getProbeTree();
        assertEquals(0, temp.getChildCount(), "Probs no children");
        assertEquals(t.getData().toString(), temp.getData().toString(), "Probs object");
        assertEquals(t.toString(), temp.toString(), "Probs display");
        assertEquals(t.isClickable(), temp.isClickable(), "Probs clickable");

        TreeSettings.setTrees(t, t1);
        temp = TreeSettings.getProbeTree();
        assertEquals(0, temp.getChildCount(), "Probs no children");
        assertEquals(t1.getData(), temp.getData(), "Probs object");
        assertEquals(t1.toString(), temp.toString(), "Probs display");
        assertEquals(t1.isClickable(), temp.isClickable(), "Probs clickable");

        TreeSettings.setTrees(t, t2);
        temp = TreeSettings.getProbeTree();
        assertEquals(0, temp.getChildCount(), "Probs no children");
        assertEquals(t2.getData().toString(), temp.getData(), "Probs object");
        assertEquals(t2.toString(), temp.toString(), "Probs display");
        assertEquals(t2.isClickable(), temp.isClickable(), "Probs clickable");

        t.add(t2);
        t.add(t1);
        TreeSettings.setTrees(t, t);
        temp = TreeSettings.getProbeTree();
        assertEquals(2, temp.getChildCount(), "Probs has children");
        assertEquals(t2.getData().toString(), temp.getChildAt(0).getData(), "Probs child object");
        assertEquals(t2.toString(), temp.getChildAt(0).toString(), "Probs child display");
    }

    @Test
    public void testDefinitionTrees() {
        TreeNode temp;
        TreeDefinitionNode t1 = new TreeDefinitionNode("t1", "deftree1", "location", true);
        TreeDefinitionNode t2 = new TreeDefinitionNode("t2", "deftree2", null, true);

        TreeSettings.setTrees(t1, t2);
        temp = TreeSettings.getFunctionTree();
        assertTrue(temp instanceof TreeDefinitionNode, "Tree should have been saved as a definition node");
        assertEquals(t1.getDefinition(), ((TreeDefinitionNode) temp).getDefinition(), "Funcs definition");
        assertEquals(t1.getData(), temp.getData(), "Funcs object");
        assertEquals(t1.toString(), temp.toString(), "Funcs display");
        assertEquals(t1.isClickable(), temp.isClickable(), "Funcs clickable");

        temp = TreeSettings.getProbeTree();
        assertTrue(temp instanceof TreeDefinitionNode, "Even with a null definition, tree should have been saved as a definition node");
        assertEquals(t2.getDefinition(), ((TreeDefinitionNode) temp).getDefinition(), "Probs definition");
        assertEquals(t2.getData(), temp.getData(), "Probs object");
        assertEquals(t2.toString(), temp.toString(), "Probs display");
        assertEquals(t2.isClickable(), temp.isClickable(), "Probs clickable");
    }

    @Test
    public void testStapNodeData() {
        TreeNode temp;
        TreeNode t1 = new TreeNode(new FunctionNodeData("function ftest(x:long)", null), true);
        t1.add(new TreeNode(new FuncparamNodeData("long"), "x", false));

        TreeNode t2 = new TreeNode(new ProbeNodeData("ptest"), true);
        t2.add(new TreeNode(new ProbevarNodeData("x:long"), false));

        TreeSettings.setTrees(t1, t2);
        temp = TreeSettings.getFunctionTree();
        assertTrue(temp.getData() instanceof FunctionNodeData, "Improper data type - expected FunctionNodeData but was "+ temp.getData().getClass().getSimpleName());
        assertEquals(t1.getData().toString(), temp.getData().toString(), "Function data not saved");
        assertEquals(t1.getChildCount(), temp.getChildCount(), "Funcs has children");
        assertTrue(temp.getChildAt(0).getData() instanceof FuncparamNodeData, "Improper data type");
        assertEquals(t1.getChildAt(0).getData().toString(), temp.getChildAt(0).getData().toString(), "Function parameter data not saved");

        temp = TreeSettings.getProbeTree();
        assertTrue(temp.getData() instanceof ProbeNodeData, "Improper data type");
        assertEquals(t2.getData().toString(), temp.getData().toString(), "Probe data not saved");
        assertEquals(t2.getChildCount(), temp.getChildCount(), "Probs has children");
        assertTrue(temp.getChildAt(0).getData() instanceof ProbevarNodeData, "Improper data type");
        assertEquals(t2.getChildAt(0).getData().toString(), temp.getChildAt(0).getData().toString(), "Probe variable data not saved");
    }
}
