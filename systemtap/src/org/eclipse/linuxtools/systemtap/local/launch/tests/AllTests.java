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
package org.eclipse.linuxtools.systemtap.local.launch.tests;

//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.swt.widgets.Shell;

import org.eclipse.linuxtools.systemtap.local.core.SystemTapUIErrorMessages;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
//		Shell sh = new Shell();
//		MessageDialog.openInformation(sh, "Launching JUnit tests", 
//				"Commencing JUnit tests of the SystemTap Eclipse plugin. You should\n" +
//				"see several error dialogs pop up - these are a normal part of the\n" +
//				"test. The testing framework must 'trick' the plugin into launching\n" +
//				"some of its internal classes, and the plugin is just letting you\n" +
//				"know that it is being tricked. \nJust click OK on all dialogs.");
		
		SystemTapUIErrorMessages.setActive(false);
		
//		Shell eclipseShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
//		int width = Display.getCurrent().getBounds().width;
//		int height = Display.getCurrent().getBounds().height;
//		eclipseShell.setBounds(0, 0, width, height);
		
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.systemtap.local.launch.tests");
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
