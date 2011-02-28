/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import org.eclipse.linuxtools.internal.valgrind.memcheck.MemcheckPlugin;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Tests for " + MemcheckPlugin.PLUGIN_ID); //$NON-NLS-1$
		// $JUnit-BEGIN$
		suite.addTestSuite(BasicMemcheckTest.class);
		suite.addTestSuite(DoubleClickTest.class);
		suite.addTestSuite(LaunchConfigTabTest.class);
		suite.addTestSuite(MarkerTest.class);
		suite.addTestSuite(LinkedResourceDoubleClickTest.class);
		suite.addTestSuite(LinkedResourceMarkerTest.class);
		suite.addTestSuite(MultiProcessTest.class);
		suite.addTestSuite(ExpandCollapseTest.class);
		suite.addTestSuite(ShortcutTest.class);
		suite.addTestSuite(SignalTest.class);
		suite.addTestSuite(MinVersionTest.class);
		// $JUnit-END$
		return suite;
	}

}
