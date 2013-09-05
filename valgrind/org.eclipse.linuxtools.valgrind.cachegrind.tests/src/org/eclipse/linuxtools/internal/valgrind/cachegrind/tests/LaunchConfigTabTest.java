/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind.tests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindPlugin;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindToolPage;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LaunchConfigTabTest extends AbstractCachegrindTest {

	protected ILaunchConfiguration config;
	protected Shell testShell;
	protected ValgrindOptionsTab tab;
	protected CachegrindToolPage dynamicTab;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("cpptest"); //$NON-NLS-1$

		config = createConfiguration(proj.getProject());

		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
		tab = new ValgrindOptionsTab();
	}

	@Override
	@After
	public void tearDown() throws Exception {
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
		int ix = Arrays.asList(tab.getTools()).indexOf(CachegrindPlugin.TOOL_ID);
		tab.getToolsCombo().select(ix);
		ILaunchConfigurationTab dynamicTab = tab.getDynamicTab();
		this.dynamicTab = (CachegrindToolPage) dynamicTab;
		return wc;
	}

	private ILaunch saveAndLaunch(ILaunchConfigurationWorkingCopy wc, String testName)
	throws Exception {
		tab.performApply(wc);
		config = wc.doSave();

		ILaunch launch = doLaunch(config, testName);
		return launch;
	}
	@Test
	public void testDefaults() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		ILaunch launch = saveAndLaunch(wc, "testDefaults"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--tool=cachegrind")); //$NON-NLS-1$
		assertFalse(cmd.contains("--xml=yes")); //$NON-NLS-1$
		assertTrue(cmd.contains("-q")); //$NON-NLS-1$
		assertTrue(cmd.contains("--trace-children=no")); //$NON-NLS-1$
		assertTrue(cmd.contains("--child-silent-after-fork=yes")); //$NON-NLS-1$
		assertTrue(cmd.contains("--demangle=yes")); //$NON-NLS-1$
		assertTrue(cmd.contains("--num-callers=12")); //$NON-NLS-1$
		assertTrue(cmd.contains("--error-limit=yes")); //$NON-NLS-1$
		assertTrue(cmd.contains("--show-below-main=no")); //$NON-NLS-1$
		assertFalse(cmd.contains("--suppressions")); //$NON-NLS-1$
		assertTrue(cmd.contains("--max-stackframe=2000000")); //$NON-NLS-1$
		assertTrue(cmd.contains("--run-libc-freeres=yes")); //$NON-NLS-1$

		assertTrue(cmd.contains("--cache-sim=yes")); //$NON-NLS-1$
		assertTrue(cmd.contains("--branch-sim=no")); //$NON-NLS-1$
	}
	@Test
	public void testNoSim() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getCacheButton().setSelection(false);
		tab.performApply(wc);
		wc.doSave();

		assertFalse(tab.isValid(config));
	}
	@Test
	public void testBranchSim() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getBranchButton().setSelection(true);
		tab.performApply(wc);
		wc.doSave();

		ILaunch launch = saveAndLaunch(wc, "testBranchSim"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--branch-sim=yes")); //$NON-NLS-1$
	}
	@Test
	public void testI1Cache() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();

		assertFalse(dynamicTab.getI1SizeSpinner().isEnabled());
		dynamicTab.getI1Button().setSelection(true);
		dynamicTab.getI1Button().notifyListeners(SWT.Selection, null);
		assertTrue(dynamicTab.getI1SizeSpinner().isEnabled());

		dynamicTab.getI1SizeSpinner().setSelection(16384);
		dynamicTab.getI1AssocSpinner().setSelection(1);
		dynamicTab.getI1LineSizeSpinner().setSelection(16);

		tab.performApply(wc);
		wc.doSave();

		ILaunch launch = saveAndLaunch(wc, "testI1Cache"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertTrue(cmd.contains("--I1=16384,1,16")); //$NON-NLS-1$
	}
	@Test
	public void testD1Cache() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();

		assertFalse(dynamicTab.getD1SizeSpinner().isEnabled());
		dynamicTab.getD1Button().setSelection(true);
		dynamicTab.getD1Button().notifyListeners(SWT.Selection, null);
		assertTrue(dynamicTab.getD1SizeSpinner().isEnabled());

		dynamicTab.getD1SizeSpinner().setSelection(16384);
		dynamicTab.getD1AssocSpinner().setSelection(1);
		dynamicTab.getD1LineSizeSpinner().setSelection(16);

		tab.performApply(wc);
		wc.doSave();

		ILaunch launch = saveAndLaunch(wc, "testD1Cache"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertTrue(cmd.contains("--D1=16384,1,16")); //$NON-NLS-1$
	}
	@Test
	public void testL2Cache() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();

		assertFalse(dynamicTab.getL2SizeSpinner().isEnabled());
		dynamicTab.getL2Button().setSelection(true);
		dynamicTab.getL2Button().notifyListeners(SWT.Selection, null);
		assertTrue(dynamicTab.getL2SizeSpinner().isEnabled());

		dynamicTab.getL2SizeSpinner().setSelection(16384);
		dynamicTab.getL2AssocSpinner().setSelection(1);
		dynamicTab.getL2LineSizeSpinner().setSelection(16);

		tab.performApply(wc);
		wc.doSave();

		ILaunch launch = saveAndLaunch(wc, "testL2Cache"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertTrue(cmd.contains("--L2=16384,1,16")); //$NON-NLS-1$
	}

}
