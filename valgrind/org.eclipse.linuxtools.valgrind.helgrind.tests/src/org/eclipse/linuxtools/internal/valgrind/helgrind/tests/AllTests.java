/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind.tests;

import org.eclipse.linuxtools.internal.valgrind.helgrind.HelgrindPlugin;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Tests for " + HelgrindPlugin.PLUGIN_ID); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(BasicHelgrindTest.class);
		suite.addTestSuite(DoubleClickTest.class);
		suite.addTestSuite(LaunchConfigTabTest.class);
		suite.addTestSuite(ExpandCollapseTest.class);
		//$JUnit-END$
		return suite;
	}

}
