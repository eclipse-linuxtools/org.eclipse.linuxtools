/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.massif.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Tests for org.eclipse.linuxtools.valgrind.massif"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(DoubleClickTest.class);
		suite.addTestSuite(ChartTests.class);
		suite.addTestSuite(TreeTest.class);
		suite.addTestSuite(BasicMassifTest.class);
		suite.addTestSuite(LaunchConfigTabTest.class);
		//$JUnit-END$
		return suite;
	}

}
