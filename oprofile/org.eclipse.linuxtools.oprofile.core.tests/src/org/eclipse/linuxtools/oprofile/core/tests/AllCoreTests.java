/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCoreTests {

	public static Test suite() {
		
		/**
		 *  Java system properties.
		 *  usage: -Dorg.eclipse.linuxtools.oprofile.tests.runOprofile=<yes|no> [default: yes]
		 *  if yes, will run the core tests
		 *     no, will skip the core tests (they all require oOProfile to be set up)
		 */
		String SYSTEM_PROPERTY_RUN_OPROFILE = "org.eclipse.linuxtools.oprofile.tests.runOprofile"; //$NON-NLS-1$
		boolean RUN_OPROFILE = System.getProperty(SYSTEM_PROPERTY_RUN_OPROFILE, "yes").equals("yes"); //$NON-NLS-1$ //$NON-NLS-2$
		
		TestSuite suite = new TestSuite("Test for org.eclipse.linuxtools.oprofile.core.tests"); //$NON-NLS-1$
		
		if (RUN_OPROFILE) {
		suite.addTestSuite(TestModelDataParse.class);
		suite.addTestSuite(TestSessionsParse.class);
		suite.addTestSuite(TestCheckEventsParse.class);
		suite.addTestSuite(TestInfoParse.class);
		suite.addTestSuite(TestDataModel.class);
		}
		
		suite.addTestSuite(TestDummy.class);
		
		return suite;
	}

}
