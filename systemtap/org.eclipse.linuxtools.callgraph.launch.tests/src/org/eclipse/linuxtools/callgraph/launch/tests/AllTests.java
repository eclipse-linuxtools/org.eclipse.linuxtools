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
package org.eclipse.linuxtools.callgraph.launch.tests;

import org.eclipse.linuxtools.internal.callgraph.core.SystemTapUIErrorMessages;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		
		SystemTapUIErrorMessages.setActive(false);
		
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.callgraph.launch.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(SystemTapCommandTest.class);
		suite.addTestSuite(ConfigurationTest.class);
		suite.addTestSuite(SystemTapCommandGeneratorTest.class);
		if (TestConstants.canRunStap)
			suite.addTestSuite(SystemTapCommandLineTest.class);
		suite.addTestSuite(SystemTapTabTest.class);
		suite.addTestSuite(LaunchShortcutsTest.class);
		//$JUnit-END$
		return suite;
	}

}
