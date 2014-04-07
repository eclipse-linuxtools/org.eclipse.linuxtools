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

package org.eclipse.linuxtools.systemtap.ui.ide.test.structures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TreeSettings;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.junit.Before;
import org.junit.Test;

public class TreeSettingsTest {

	@Before
	public void setUp() {
		TreeSettings.setTrees(new TreeNode("func", false), new TreeNode("probe", false));
	}

	@Test
	public void testSetTrees() {
		TreeNode t = new TreeNode("f", false);
		TreeNode t1 = new TreeNode("as", "as3", false);
		TreeNode t2 = new TreeNode(null, false);
		TreeNode t3 = null;

		assertTrue("Set tree fine1", TreeSettings.setTrees(t, t));
		assertTrue("Set tree fine2", TreeSettings.setTrees(t1, t1));
		assertTrue("Set tree null obj", TreeSettings.setTrees(t2, t2));
		assertFalse("Set trees null", TreeSettings.setTrees(t3, t3));
		assertFalse("Set func null", TreeSettings.setTrees(t3, t));
		assertFalse("Set probe null", TreeSettings.setTrees(t, t3));
	}

	@Test
	public void testGetTreeFileDate() {
		long d1 = TreeSettings.getTreeFileDate();
		long d2 = TreeSettings.getTreeFileDate();
		assertEquals("TreeDate same", d1, d2);

		TreeSettings.setTrees(new TreeNode("f", false), new TreeNode("p", false));
		d2 = TreeSettings.getTreeFileDate();

		assertTrue("TreeDate changed", d1 < d2);
	}

	@Test
	public void testGetFunctionTree() {
		TreeNode temp;
		TreeNode t = new TreeNode("f", false);
		TreeNode t1 = new TreeNode(null, false);
		TreeNode t2 = new TreeNode(new StringBuilder("asfd"), true);

		TreeSettings.setTrees(t, t);
		temp = TreeSettings.getFunctionTree();
		assertEquals("Funcs no children", 0, temp.getChildCount());
		assertEquals("Funcs object", t.getData().toString(), temp.getData().toString());
		assertEquals("Funcs display", t.toString(),temp.toString());

		TreeSettings.setTrees(t1, t);
		temp = TreeSettings.getFunctionTree();
		assertEquals("Funcs no children", 0, temp.getChildCount());
		assertEquals("Funcs object", t1.getData(), temp.getData());
		assertEquals("Funcs display", t1.toString(), temp.toString());

		TreeSettings.setTrees(t2, t);
		temp = TreeSettings.getFunctionTree();
		assertEquals("Funcs no children", 0, temp.getChildCount());
		assertEquals("Funcs object", t2.getData().toString(),temp.getData());
		assertEquals("Funcs display", t2.toString(), temp.toString());

		t.add(t2);
		t.add(t1);
		TreeSettings.setTrees(t, t);
		temp = TreeSettings.getFunctionTree();
		assertEquals("Funcs has children", 2, temp.getChildCount());
		assertEquals("Funcs child object", t2.getData().toString(), temp.getChildAt(0).getData());
		assertEquals("Funcs child display", t2.toString(), temp.getChildAt(0).toString());
	}

	@Test
	public void testGetProbeTree() {
		TreeNode temp;
		TreeNode t = new TreeNode("f", false);
		TreeNode t1 = new TreeNode(null, false);
		TreeNode t2 = new TreeNode(new StringBuilder("asfd"), true);

		TreeSettings.setTrees(t, t);
		temp = TreeSettings.getProbeTree();
		assertEquals("Probs no children", 0, temp.getChildCount());
		assertEquals("Probs object", t.getData().toString(), temp.getData().toString());
		assertEquals("Probs display", t.toString(), temp.toString());

		TreeSettings.setTrees(t, t1);
		temp = TreeSettings.getProbeTree();
		assertEquals("Probs no children", 0, temp.getChildCount());
		assertEquals("Probs object", t1.getData(), temp.getData());
		assertEquals("Probs display", t1.toString(), temp.toString());

		TreeSettings.setTrees(t, t2);
		temp = TreeSettings.getProbeTree();
		assertEquals("Probs no children", 0, temp.getChildCount());
		assertEquals("Probs object", t2.getData().toString(), temp.getData());
		assertEquals("Probs display", t2.toString(), temp.toString());

		t.add(t2);
		t.add(t1);
		TreeSettings.setTrees(t, t);
		temp = TreeSettings.getProbeTree();
		assertEquals("Probs has children", 2, temp.getChildCount());
		assertEquals("Probs child object", t2.getData().toString(), temp.getChildAt(0).getData());
		assertEquals("Probs child display", t2.toString(), temp.getChildAt(0).toString());

	}
}
