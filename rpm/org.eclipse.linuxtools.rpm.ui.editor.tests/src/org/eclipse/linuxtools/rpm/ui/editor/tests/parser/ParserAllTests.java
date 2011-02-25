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
package org.eclipse.linuxtools.rpm.ui.editor.tests.parser;

import junit.framework.TestSuite;

public class ParserAllTests extends TestSuite {
	public static TestSuite suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.rpm.ui.editor.tests.parser");
		suite.addTestSuite(SourceComparatorTest.class);
		suite.addTestSuite(SpecfileDefineTest.class);
		suite.addTestSuite(SpecfileTest.class);
		return suite;
	}
}
