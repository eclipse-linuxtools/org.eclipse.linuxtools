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

package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.junit.Before;
import org.junit.Test;

public class TreeNodeTest {

    @Before
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
        assertEquals("Create child count", 0, t1.getChildCount());
        assertEquals("Create child string", s1, t1.toString());
        assertEquals("Create child data", d1, t1.getData());
        assertFalse("Create child clickable", t1.isClickable());

        TreeNode t2 = new TreeNode(d2, true);
        assertEquals("Create child count", 0, t2.getChildCount());
        assertTrue("Create child string", t2.toString().equals(d2));
        assertEquals("Create child data", d2, t2.getData());
        assertTrue("Create child clickable", t2.isClickable());

        TreeNode t3 = new TreeNode(null, true);
        assertEquals("Create child count", 0, t3.getChildCount());
        assertEquals("Create child string", null, t3.toString());
        assertEquals("Create child data", null, t3.getData());
        assertTrue("Create child clickable", t3.isClickable());

        TreeNode t4 = new TreeNode(d1, s1, false);
        assertEquals("Create child count", 0, t4.getChildCount());
        assertEquals("Create child string", s1, t4.toString());
        assertEquals("Create child data", d1, t4.getData());
        assertFalse("Create child clickable", t4.isClickable());
    }

    @Test
    public void testAdd() {
        t.add(new TreeNode("One", "tne", false));
        assertEquals("Add child", 3, t.getChildCount());

        t.add(new TreeNode("two", false));
        assertEquals("Add child2", 4, t.getChildCount());
    }

    @Test
    public void testAddAt() {
        TreeNode test1 = new TreeNode("one", false);
        t.addAt(test1, 0);
        assertEquals("Child added to front", test1, t.getChildAt(0));
        assertEquals("Child correctly added", 3, t.getChildCount());

        TreeNode test2 = new TreeNode("two", false);
        t.addAt(test2, 1);
        assertEquals("Child added to middle", test2, t.getChildAt(1));
        assertEquals("Child correctly added", 4, t.getChildCount());

        TreeNode test3 = new TreeNode("three", false);
        t.addAt(test3, 3);
        assertEquals("Child added to end", test3, t.getChildAt(3));
        assertEquals("Child correctly added", 5, t.getChildCount());

        TreeNode test4 = new TreeNode("four", false);
        t.addAt(test4, 30);
        assertEquals("Child added to end", test4, t.getChildAt(5));
        assertEquals("Child correctly added", 6, t.getChildCount());
    }

    @Test
    public void testGetChildAt() {
        TreeNode child1 = new TreeNode("1", false);
        t.add(child1);
        TreeNode child2 = new TreeNode("2", false);
        t.add(child2);

        TreeNode c = t.getChildAt(0);
        assertEquals("Retreive first child from tree", child, c);

        c = t.getChildAt(2);
        assertEquals("Retreive middle child from tree", child1, c);

        c = t.getChildAt(3);
        assertEquals("Retreive last child from tree", child2, c);

        assertNull("No child here", t.getChildAt(10));
    }

    @Test
    public void testGetChildCount() {
        assertEquals("Tree child count", 2, t.getChildCount());
        assertEquals("Child child count", 0, t.getChildAt(0).getChildCount());
    }

    @Test
    public void testGetData() {
        assertEquals("Correct data", data, t.getData());
        assertEquals("Correct data2", data2, t.getChildAt(0).getData());
    }

    @Test
    public void testIsClickable() {
        assertFalse("Nonclickable root", t.isClickable());
        assertTrue("Clickable child", t.getChildAt(0).isClickable());
    }

    @Test
    public void testRemove() {
        TreeNode child1 = new TreeNode("1", false);
        t.add(child1);
        TreeNode child2 = new TreeNode("2", false);
        t.add(child2);
        TreeNode child3 = new TreeNode("3", false);
        t.add(child3);

        assertEquals("Full tree before remove", 5, t.getChildCount());

        assertTrue(t.remove(1));
        assertEquals("Remove middle child from tree", 4, t.getChildCount());

        assertTrue(t.remove(0));
        assertEquals("Remove first child from tree", 3, t.getChildCount());

        assertTrue(t.remove(1));
        assertEquals("Remove last child from tree", 2, t.getChildCount());

        assertFalse(t.remove(10));
        assertEquals("Remove IndexOutOfBounds", 2, t.getChildCount());
    }

    @Test
    public void testRemoveAll() {
        t.add(new TreeNode("Child", false));

        assertTrue(t.removeAll());
        assertEquals("No children", 0, t.getChildCount());

        assertTrue(t.removeAll());
        assertEquals("Still no children", 0, t.getChildCount());
    }

    @Test
    public void testSetData() {
        Object o = "asdf";
        t.setData(o);
        assertEquals("Replaced data", o, t.getData());

        String o1 = "aaaa";
        t.setData(o1);
        assertEquals("Replaced data with string", o1, t.getData());
    }

    @Test
    public void testSetDisplay() {
        String s1 = "aaaa";
        t.setDisplay(s1);
        assertEquals("Replaced display", s1, t.toString());
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
        assertEquals("Same number of children", 5, t.getChildCount());
        assertEquals("Sorted first node correct", child2, t.getChildAt(0));
        assertEquals("Sorted last node correct", child, t.getChildAt(3));
        assertEquals("Sorted middle correct", child1, t.getChildAt(1));
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
        assertEquals("Same number of children", 5, t.getChildCount());
        assertEquals("Sorted first node correct", child2, t.getChildAt(0));
        assertEquals("Sorted last node correct", child, t.getChildAt(3));
        assertEquals("Sorted middle correct", child1, t.getChildAt(1));
    }

    @Test
    public void testToString() {
        assertEquals("Object to string", data2, child.toString());
        assertEquals("Dispaly to string", s, t.toString());
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
