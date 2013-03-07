/*******************************************************************************
* Copyright (c) 2012 Red Hat, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Red Hat, Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllPerfTests {

	public static Test suite() {
		/**
		 * Java system properties. usage:
		 * -Dorg.eclipse.linuxtools.perf.tests.runPerf=<yes|no>
		 * [default: yes] if yes, will run the launch tests no, will skip the
		 * launch tests (they all require perf to be installed)
		 */
		String SYSTEM_PROPERTY_RUN_PERF = "org.eclipse.linuxtools.perf.tests.runPerf"; //$NON-NLS-1$
		boolean RUN_PERF = System
				.getProperty(SYSTEM_PROPERTY_RUN_PERF, "yes").equals("yes"); //$NON-NLS-1$ //$NON-NLS-2$
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.perf.tests"); //$NON-NLS-1$

		suite.addTestSuite(LaunchTabsTest.class);
		suite.addTestSuite(ModelTest.class);
		suite.addTestSuite(DataManipulatorTest.class);
		suite.addTestSuite(SaveSessionTest.class);
		suite.addTestSuite(StatsComparisonTest.class);

		if (RUN_PERF) {
			suite.addTestSuite(LaunchTest.class);
		}

		return suite;
	}

}
