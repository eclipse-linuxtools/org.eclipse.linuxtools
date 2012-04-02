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
package org.eclipse.linuxtools.oprofile.launch.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllLaunchTests {
	


	public static Test suite() {
		/**
		 *  Java system properties.
		 *  usage: -Dorg.eclipse.linuxtools.oprofile.launch.tests.runOprofile=<yes|no> [default: yes]
		 *  if yes, will run the launch tests
		 *     no, will skip the launch tests (they all require oprofile to be set up)
		 */
		String SYSTEM_PROPERTY_RUN_OPROFILE = "org.eclipse.linuxtools.oprofile.launch.tests.runOprofile"; //$NON-NLS-1$
		boolean RUN_OPROFILE = System.getProperty(SYSTEM_PROPERTY_RUN_OPROFILE, "yes").equals("yes"); //$NON-NLS-1$ //$NON-NLS-2$
		TestSuite suite = new TestSuite("Test for org.eclipse.linuxtools.oprofile.launch.tests"); //$NON-NLS-1$
		
		if (RUN_OPROFILE) {
			suite.addTestSuite(TestLaunching.class);
			suite.addTestSuite(TestManualLaunching.class);
			suite.addTestSuite(TestSetup.class);
			suite.addTestSuite(TestLaunchingExternalProject.class);
		}
		
		suite.addTestSuite(TestDummy.class);
		
		return suite;
	}

}
