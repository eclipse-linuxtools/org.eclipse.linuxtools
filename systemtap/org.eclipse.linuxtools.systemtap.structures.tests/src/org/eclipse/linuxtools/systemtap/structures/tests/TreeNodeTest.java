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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TreeNodeTest {

    @BeforeEach
    public void setUp() {

        data = new StringBuilder("Object");
        data2 = "Data";
        s = "String";

        t = new TreeNode(data, s, false);
        child = new TreeNode(data2, true);
        child2 = new TreeNode(data, s, false);
        t.add(child);
        t.add(child2);
    }

    @Test
    public void testTreeNode() {
        String d1 = "One";
        String d2 = "two";
        String s1 = "one";

        TreeNode t1 = new TreeNode(d1, s1, false);
        assertEquals(0, t1.getChildCount(), "Create child count");
        assertEquals(s1, t1.toString(), "Create child string");
        assertEquals(d1, t1.getData(), "Create child data");
        assertFalse(t1.isClickable(), "Create child clickable");

        TreeNode t2 = new TreeNode(d2, true);
        assertEquals(0, t2.getChildCount(), "Create child count");
        assertTrue(t2.toString().equals(d2), "Create child string");
        assertEquals(d2, t2.getData(), "Create child data");
        assertTrue(t2.isClickable(), "Create child clickable");

        TreeNode t3 = new TreeNode(null, true);
        assertEquals(0, t3.getChildCount(), "Create child count");
        assertNull(t3.toString(), "Create child string");
        assertNull(t3.getData(), "Create child data");
        assertTrue(t3.isClickable(), "Create child clickable");

        TreeNode t4 = new TreeNode(d1, s1, false);
        assertEquals(0, t4.getChildCount(), "Create child count");
        assertEquals(s1, t4.toString(), "Create child string");
        assertEquals(d1, t4.getData(), "Create child data");
        assertFalse(t4.isClickable(), "Create child clickable");
    }

    @Test
    public void testAdd() {
        t.add(new TreeNode("One", "tne", false));
        assertEquals(3, t.getChildCount(), "Add child");

        t.add(new TreeNode("two", false));
        assertEquals(4, t.getChildCount(), "Add child2");
    }

    @Test
    public void testAddAt() {
        TreeNode test1 = new TreeNode("one", false);
        t.addAt(test1, 0);
        assertEquals(test1, t.getChildAt(0), "Child added to front");
        assertEquals(3, t.getChildCount(), "Child correctly added");

        TreeNode test2 = new TreeNode("two", false);
        t.addAt(test2, 1);
        assertEquals(test2, t.getChildAt(1), "Child added to middle");
        assertEquals(4, t.getChildCount(), "Child correctly added");

        TreeNode test3 = new TreeNode("three", false);
        t.addAt(test3, 3);
        assertEquals(test3, t.getChildAt(3), "Child added to end");
        assertEquals(5, t.getChildCount(), "Child correctly added");

        TreeNode test4 = new TreeNode("four", false);
        t.addAt(test4, 30);
        assertEquals(test4, t.getChildAt(5), "Child added to end");
        assertEquals(6, t.getChildCount(), "Child correctly added");
    }

    @Test
    public void testGetChildAt() {
        TreeNode child1 = new TreeNode("1", false);
        t.add(child1);
        TreeNode child2 = new TreeNode("2", false);
        t.add(child2);

        TreeNode c = t.getChildAt(0);
        assertEquals(child, c, "Retreive first child from tree");

        c = t.getChildAt(2);
        assertEquals(child1, c, "Retreive middle child from tree");

        c = t.getChildAt(3);
        assertEquals(child2, c, "Retreive last child from tree");

        assertNull(t.getChildAt(10), "No child here");
    }

    @Test
    public void testGetChildCount() {
        assertEquals(2, t.getChildCount(), "Tree child count");
        assertEquals(0, t.getChildAt(0).getChildCount(), "Child child count");
    }

    @Test
    public void testGetData() {
        assertEquals(data, t.getData(), "Correct data");
        assertEquals(data2, t.getChildAt(0).getData(), "Correct data2");
    }

    @Test
    public void testIsClickable() {
        assertFalse(t.isClickable(), "Nonclickable root");
        assertTrue(t.getChildAt(0).isClickable(), "Clickable child");
    }

    @Test
    public void testRemove() {
        TreeNode child1 = new TreeNode("1", false);
        t.add(child1);
        TreeNode child2 = new TreeNode("2", false);
        t.add(child2);
        TreeNode child3 = new TreeNode("3", false);
        t.add(child3);

        assertEquals(5, t.getChildCount(), "Full tree before remove");

        assertTrue(t.remove(1));
        assertEquals(4, t.getChildCount(), "Remove middle child from tree");

        assertTrue(t.remove(0));
        assertEquals(3, t.getChildCount(),"Remove first child from tree");

        assertTrue(t.remove(1));
        assertEquals(2, t.getChildCount(), "Remove last child from tree");

        assertFalse(t.remove(10));
        assertEquals(2, t.getChildCount(), "Remove IndexOutOfBounds");
    }

    @Test
    public void testRemoveAll() {
        t.add(new TreeNode("Child", false));

        assertTrue(t.removeAll());
        assertEquals(0, t.getChildCount(), "No children");

        assertTrue(t.removeAll());
        assertEquals(0, t.getChildCount(), "Still no children");
    }

    @Test
    public void testSetData() {
        Object o = "asdf";
        t.setData(o);
        assertEquals(o, t.getData(), "Replaced data");

        String o1 = "aaaa";
        t.setData(o1);
        assertEquals(o1, t.getData(), "Replaced data with string");
    }

    @Test
    public void testSetDisplay() {
        String s1 = "aaaa";
        t.setDisplay(s1);
        assertEquals(s1, t.toString(), "Replaced display");
    }

    @Test
    public void testSortTree() {
        TreeNode child1 = new TreeNode("2", false);
        t.add(child1);
        TreeNode child2 = new TreeNode("1", false);
        t.add(child2);
        TreeNode child3 = new TreeNode("3", false);
        t.add(child3);

        t.sortTree();
        assertEquals(5, t.getChildCount(), "Same number of children");
        assertEquals(child2, t.getChildAt(0), "Sorted first node correct");
        assertEquals(child, t.getChildAt(3), "Sorted last node correct");
        assertEquals(child1, t.getChildAt(1), "Sorted middle correct");
    }

    @Test
    public void testSortLevel() {
        TreeNode child1 = new TreeNode("2", false);
        t.add(child1);
        TreeNode child2 = new TreeNode("1", false);
        t.add(child2);
        TreeNode child3 = new TreeNode("3", false);
        t.add(child3);


        t.sortLevel();
        assertEquals(5, t.getChildCount(), "Same number of children");
        assertEquals(child2, t.getChildAt(0), "Sorted first node correct");
        assertEquals(child, t.getChildAt(3),"Sorted last node correct");
        assertEquals(child1, t.getChildAt(1), "Sorted middle correct");
    }

    @Test
    public void testToString() {
        assertEquals(data2, child.toString(), "Object to string");
        assertEquals(s, t.toString(), "Dispaly to string");
    }

    @Test
    public void testDispose() {
        t.dispose();
    }

    private TreeNode t;
    private TreeNode child;
    private TreeNode child2;
    private Object data;
    private String data2;
    private String s;
}
