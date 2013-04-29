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
		TestSuite suite = new TestSuite("Test for org.eclipse.linuxtools.oprofile.core.tests"); //$NON-NLS-1$

		suite.addTestSuite(TestModelDataParse.class);
		suite.addTestSuite(TestSessionsParse.class);
		suite.addTestSuite(TestCheckEventsParse.class);
		suite.addTestSuite(TestInfoParse.class);
		suite.addTestSuite(TestDataModel.class);
		suite.addTestSuite(TestDummy.class);
		
		return suite;
	}

}
