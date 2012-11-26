/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllGprofTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Tests for org.eclipse.linuxtools.internal.gprof.test");
		//$JUnit-BEGIN$
		suite.addTest(GprofBinaryTest.suite());
		suite.addTest(GprofTest.suite());
		suite.addTest(GprofAggregatorTest.suite());
		suite.addTest(GprofParserTest.suite());
		suite.addTestSuite(GprofLaunchTest.class);
		suite.addTestSuite(GprofShortcutTest.class);
		//$JUnit-END$
		return suite;
	}

}
