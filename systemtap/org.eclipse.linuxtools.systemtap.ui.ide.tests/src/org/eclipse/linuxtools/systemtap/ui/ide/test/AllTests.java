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

package org.eclipse.linuxtools.systemtap.ui.ide.test;

import org.eclipse.linuxtools.systemtap.ui.ide.test.structures.StapErrorParserTest;
import org.eclipse.linuxtools.systemtap.ui.ide.test.structures.TreeSettingsTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.linuxtools.systemtap.ui.ide.test");

		//Root
		suite.addTestSuite(IDESessionSettingsTest.class);
		
		//Structures
		suite.addTestSuite(StapErrorParserTest.class);
		suite.addTestSuite(TreeSettingsTest.class);

		return suite;
	}
}
