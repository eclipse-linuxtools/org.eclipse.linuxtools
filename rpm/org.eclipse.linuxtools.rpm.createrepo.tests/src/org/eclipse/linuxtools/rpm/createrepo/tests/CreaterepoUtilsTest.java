/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.createrepo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.linuxtools.rpm.createrepo.CreaterepoUtils;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for CreaterepoUtils.
 */
public class CreaterepoUtilsTest {

	private static final String CONSOLE_NAME = "CreaterepoConsole"; //$NON-NLS-1$

	private static ConsolePlugin plugin;
	private static IConsoleManager manager;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		plugin = ConsolePlugin.getDefault();
		manager = plugin.getConsoleManager();
	}

	/**
	 * Find any consoles and remove them.
	 *
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (manager != null) {
			manager.removeConsoles(manager.getConsoles());
		}
	}

	/**
	 * Test if findConsole method finds correct console.
	 */
	@Test
	public void testFindConsoleSameObject() {
		MessageConsole createrepoConsole = new MessageConsole(CONSOLE_NAME, null, null, true);
		manager.addConsoles(new IConsole[] {
				new MessageConsole("DummyConsole1", null, null, true), //$NON-NLS-1$
				createrepoConsole,
				new MessageConsole("DummyConsole2", null, null, true) //$NON-NLS-1$
		});
		assertEquals(createrepoConsole, CreaterepoUtils.findConsole(CONSOLE_NAME));
	}

	/**
	 * Test if findConsole finds correct console by name.
	 */
	@Test
	public void testFindConsoleByName() {
		MessageConsole createrepoConsole = new MessageConsole(CONSOLE_NAME, null, null, true);
		manager.addConsoles(new IConsole[] {
				new MessageConsole("DummyConsole1", null, null, true), //$NON-NLS-1$
				new MessageConsole(CONSOLE_NAME, null, null, true),
				new MessageConsole("DummyConsole2", null, null, true) //$NON-NLS-1$
		});
		assertNotEquals(createrepoConsole, CreaterepoUtils.findConsole(CONSOLE_NAME));
		assertEquals(CONSOLE_NAME, CreaterepoUtils.findConsole(CONSOLE_NAME).getName());
	}

	/**
	 * Test if findConsole creates a console with correct name.
	 */
	@Test
	public void testCreateConsoleIfNotFound() {
		MessageConsole console = CreaterepoUtils.findConsole(CONSOLE_NAME);
		assertNotEquals(null, console);
		assertEquals(CONSOLE_NAME, console.getName());
	}

}
