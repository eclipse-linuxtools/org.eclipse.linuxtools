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

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapErrorHandler;

public class SystemTapErrorHandlerTest extends TestCase {

	private SystemTapErrorHandler errHandler;
	private String errorString;

	@Override
	protected void setUp() {
		errHandler = new SystemTapErrorHandler();
	}

	public void testErrorNotRecognized(){

		errorString = "This error will not be caught \n" +
				"Not even this one \n" +
				"Unrecognized \n" +
				"Not found \n" +
				"Error";

		errHandler.handle(new NullProgressMonitor(), errorString);

		assertFalse(errHandler.isErrorRecognized());
	}


	public void testErrorRecognized(){

		errorString = "As long as the word stapusr or stapdev is here, error is recognized";

		errHandler.handle(new NullProgressMonitor(), errorString);

		assertTrue(errHandler.isErrorRecognized());
	}


	public void testUserGroupError(){

		errorString = "ERROR: You are trying to run systemtap as a normal user.\n" +
			"You should either be root, or be part of the group \"stapusr\" and " +
			"possibly one of the groups \"stapsys\" or \"stapdev\".";

		errHandler.handle(new NullProgressMonitor(), errorString);

		assertTrue(errHandler.isErrorRecognized());
		assertTrue(errHandler.getErrorMessage().contains("Please add yourself to the 'stapdev' or 'stapusr' group in order to run stap."));
	}


	public void testDebugInfoError(){

		errorString = "missing [architecture] kernel/module debuginfo under '[kernel-build-tree]'";

		errHandler.handle(new NullProgressMonitor(), errorString);

		assertTrue(errHandler.isErrorRecognized());
		assertTrue(errHandler.getErrorMessage().contains("No debuginfo could be found. Make sure you have yum-utils installed, and run debuginfo-install kernel as root."));
	}


	public void testUprobesError(){

		errorString = "SystemTap's version of uprobes is out of date. As root, or a member of the 'root' group, run \"make -C /usr/local/share/systemtap/runtime/uprobes\".";

		errHandler.handle(new NullProgressMonitor(), errorString);

		assertTrue(errHandler.isErrorRecognized());
		System.out.println(errHandler.getErrorMessage());
		assertTrue(errHandler.getErrorMessage().contains("SystemTap's version of uprobes is out of date."));
		assertTrue(errHandler.getErrorMessage().contains("make -C /usr/local/share/systemtap/runtime/uprobes\"."));
	}
}
