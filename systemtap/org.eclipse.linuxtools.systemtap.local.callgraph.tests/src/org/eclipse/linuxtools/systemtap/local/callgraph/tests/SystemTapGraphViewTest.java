/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.local.callgraph.tests;

import junit.framework.TestCase;

import org.eclipse.linuxtools.systemtap.local.callgraph.CallgraphView;

public class SystemTapGraphViewTest extends TestCase {
	private CallgraphView stapView = new CallgraphView();
	private String testText = "blah";
	
	public void test() {
		System.out.println("\n\nLaunching RunSystemTapActionTest\n");

		CallgraphView.forceDisplay();
		
		stapView.println(testText);
		assertEquals(stapView.getText(), testText);
		
		stapView.clearAll();
		assertEquals(stapView.getText(), "");
	}
	
}
