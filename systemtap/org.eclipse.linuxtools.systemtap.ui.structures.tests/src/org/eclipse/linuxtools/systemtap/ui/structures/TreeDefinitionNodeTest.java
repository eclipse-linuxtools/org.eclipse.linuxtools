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

package org.eclipse.linuxtools.systemtap.ui.structures;

import org.eclipse.linuxtools.systemtap.ui.structures.TreeDefinitionNode;

import junit.framework.TestCase;

public class TreeDefinitionNodeTest extends TestCase {
	public TreeDefinitionNodeTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
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

	public void testTreeDefinitionNode() {
		String d1 = "One";
		String d2 = "two";
		String s1 = "one";

		TreeDefinitionNode t = new TreeDefinitionNode(d1, s1, d2, false);
		assertEquals("Create child count", 0, t.getChildCount());
		assertEquals("Create child string", s1, t.toString());
		assertEquals("Create child data", d1, t.getData());
		assertEquals("Create child definition", d2, t.getDefinition());
		assertFalse("Create child clickable", t.isClickable());
	}

	
	public void testGetDefinition() {
		assertNotSame("Correct definition", d2, t.getDefinition());
		assertEquals("Correct definition2", d2, ((TreeDefinitionNode)t.getChildAt(0)).getDefinition());
	}
	
	public void testSetDefinition() {
		String s1 = "/user/share/systemtap";
		t.setDefinition(s1);
		assertEquals("Replaced definition", s1, t.getDefinition());
	}
	
	public void testDispose() {
		assertNotNull(t.getDefinition());
		t.dispose();
		assertNull(t.getDefinition());
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	TreeDefinitionNode t;
	TreeDefinitionNode child;
	Object data;
	String data2;
	String s, s2;
	String d, d2;
}
