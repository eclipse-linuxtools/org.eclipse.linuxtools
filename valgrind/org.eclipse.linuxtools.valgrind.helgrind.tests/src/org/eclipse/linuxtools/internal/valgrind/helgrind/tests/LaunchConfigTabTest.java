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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.helgrind.HelgrindPlugin;
import org.eclipse.linuxtools.internal.valgrind.helgrind.HelgrindToolPage;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LaunchConfigTabTest extends AbstractHelgrindTest {

	private ILaunchConfiguration config;
	private Shell testShell;
	private ValgrindOptionsTab tab;
	private HelgrindToolPage dynamicTab;

	@Before
	public void prep() throws Exception {
		proj = createProjectAndBuild("cpptest"); //$NON-NLS-1$

		config = createConfiguration(proj.getProject());

		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
		tab = new ValgrindOptionsTab();
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		tab.dispose();
		testShell.dispose();
		deleteProject(proj);
		super.tearDown();
	}

	private ILaunchConfigurationWorkingCopy initConfig() throws CoreException {
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		tab.setDefaults(wc);
		tab.createControl(testShell);
		tab.initializeFrom(config);
		int ix = Arrays.asList(tab.getTools()).indexOf(HelgrindPlugin.TOOL_ID);
		tab.getToolsCombo().select(ix);
		ILaunchConfigurationTab dynamicTab = tab.getDynamicTab();
		this.dynamicTab = (HelgrindToolPage) dynamicTab;
		return wc;
	}

	private ILaunch saveAndLaunch(ILaunchConfigurationWorkingCopy wc,
			String testName) throws CoreException, URISyntaxException, IOException  {
		tab.performApply(wc);
		config = wc.doSave();

		ILaunch launch = doLaunch(config, testName);
		return launch;
	}

	@Test
	public void testDefaults() throws CoreException, URISyntaxException, IOException   {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		ILaunch launch = saveAndLaunch(wc, "testHelgrindGeneric"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--tool=helgrind")); //$NON-NLS-1$
		assertFalse(cmd.contains("--xml=yes")); //$NON-NLS-1$
		assertTrue(cmd.contains("-q")); //$NON-NLS-1$
		assertTrue(cmd.contains("--track-lockorders=yes")); //$NON-NLS-1$
		assertTrue(cmd.contains("--history-level=full")); //$NON-NLS-1$
		assertTrue(cmd.contains("--conflict-cache-size=1000000")); //$NON-NLS-1$
	}

	@Test
	public void testTrackLockorders() throws CoreException, URISyntaxException, IOException  {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getLockordersButton().setSelection(false);
		tab.performApply(wc);
		wc.doSave();

		ILaunch launch = saveAndLaunch(wc, "testHelgrindGeneric"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--track-lockorders=no")); //$NON-NLS-1$
	}

	@Test
	public void testHistoryNone() throws CoreException, URISyntaxException, IOException {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getHistoryCombo().setText("none");
		tab.performApply(wc);
		wc.doSave();

		ILaunch launch = saveAndLaunch(wc, "testHelgrindGeneric"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--history-level=none")); //$NON-NLS-1$
	}

	@Test
	public void testHistoryApprox() throws CoreException, URISyntaxException, IOException {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getHistoryCombo().setText("approx");
		tab.performApply(wc);
		wc.doSave();

		ILaunch launch = saveAndLaunch(wc, "testHelgrindGeneric"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--history-level=approx")); //$NON-NLS-1$
	}

	@Test
	public void testConflictCacheSize() throws CoreException, URISyntaxException, IOException  {
		ILaunchConfigurationWorkingCopy wc = initConfig();

		dynamicTab.getCacheSizeSpinner().setSelection(123456);
		tab.performApply(wc);
		wc.doSave();

		ILaunch launch = saveAndLaunch(wc, "testHelgrindGeneric"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--conflict-cache-size=123456")); //$NON-NLS-1$
	}
}
