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
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import java.util.Arrays;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.linuxtools.internal.valgrind.memcheck.MemcheckLaunchConstants;
import org.eclipse.linuxtools.internal.valgrind.memcheck.MemcheckPlugin;
import org.eclipse.linuxtools.internal.valgrind.memcheck.MemcheckToolPage;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Version;

public class LaunchConfigTabTest extends AbstractMemcheckTest {

	protected ValgrindOptionsTab tab;
	protected MemcheckToolPage dynamicTab;
	protected ILaunchConfiguration config;
	protected Shell testShell;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$

		config = createConfiguration(proj.getProject());

		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
		tab = new ValgrindOptionsTab();
	}

	@Override
	protected void tearDown() throws Exception {
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
		int ix = Arrays.asList(tab.getTools()).indexOf(MemcheckPlugin.TOOL_ID);
		tab.getToolsCombo().select(ix);
		ILaunchConfigurationTab dynamicTab = tab.getDynamicTab();
		this.dynamicTab = (MemcheckToolPage) dynamicTab;
		return wc;
	}

	private ILaunch saveAndLaunch(ILaunchConfigurationWorkingCopy wc, String testName)
	throws Exception {
		tab.performApply(wc);
		config = wc.doSave();

		ILaunch launch = doLaunch(config, testName);
		return launch;
	}

	public void testDefaults() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		ILaunch launch = saveAndLaunch(wc, "testDefaults"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--tool=memcheck")); //$NON-NLS-1$
			assertTrue(cmd.contains("-q")); //$NON-NLS-1$
			assertTrue(cmd.contains("--trace-children=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--child-silent-after-fork=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--demangle=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--num-callers=12")); //$NON-NLS-1$
			assertTrue(cmd.contains("--error-limit=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--show-below-main=no")); //$NON-NLS-1$
			assertFalse(cmd.contains("--suppressions=")); //$NON-NLS-1$
			assertTrue(cmd.contains("--max-stackframe=2000000")); //$NON-NLS-1$
			assertFalse(cmd.contains("--alignment=")); //$NON-NLS-1$
			assertTrue(cmd.contains("--run-libc-freeres=yes")); //$NON-NLS-1$

			assertTrue(cmd.contains("--leak-check=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--show-reachable=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--leak-resolution=high")); //$NON-NLS-1$
			assertTrue(cmd.contains("--freelist-vol=10000000")); //$NON-NLS-1$
			assertTrue(cmd.contains("--workaround-gcc296-bugs=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--partial-loads-ok=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--undef-value-errors=yes")); //$NON-NLS-1$

			// 3.4.0 specific
			IProject project = CDebugUtils.verifyCProject(wc).getProject();
			Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion(project);
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				assertFalse(cmd.contains("--track-origins")); //$NON-NLS-1$
			}
			assertFalse(cmd.contains("--main-stacksize")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testWSSuppresions() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		String text = "${workspace_loc:/basicTest/testsuppfile.supp}"; //$NON-NLS-1$
		tab.getSuppFileList().add(text);
		ILaunch launch = saveAndLaunch(wc, "testWSSuppresions"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			IPath suppPath = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path("basicTest/testsuppfile.supp")).getLocation(); //$NON-NLS-1$
			assertTrue(cmd.contains("--suppressions=" + suppPath.toOSString())); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testSuppressions() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		IPath suppPath = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path("basicTest/testsuppfile.supp")).getLocation(); //$NON-NLS-1$
		tab.getSuppFileList().add(suppPath.toOSString());
		ILaunch launch = saveAndLaunch(wc, "testSuppressions"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--suppressions=" + suppPath.toOSString())); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testSuppressionsMultiple() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		IPath suppPath = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path("basicTest/testsuppfile.supp")).getLocation(); //$NON-NLS-1$
		IPath suppPath2 = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path("basicTest/testsuppfile2.supp")).getLocation(); //$NON-NLS-1$
		tab.getSuppFileList().add(suppPath.toOSString());
		tab.getSuppFileList().add(suppPath2.toOSString());
		ILaunch launch = saveAndLaunch(wc, "testSuppressionsMultiple"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--suppressions=" + suppPath.toOSString())); //$NON-NLS-1$
			assertTrue(cmd.contains("--suppressions=" + suppPath2.toOSString())); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testSuppressionsSpaces() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		IPath suppPath = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path("basicTest/test suppfile.supp")).getLocation(); //$NON-NLS-1$
		tab.getSuppFileList().add(suppPath.toOSString());
		ILaunch launch = saveAndLaunch(wc, "testSuppressionsSpaces"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--suppressions=" + suppPath.toOSString())); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testTraceChildren() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getTraceChildrenButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testTraceChildren"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--trace-children=yes")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testDemangle() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getDemangleButton().setSelection(false);
		ILaunch launch = saveAndLaunch(wc, "testDemangle"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--demangle=no")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testNumCallers() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getNumCallersSpinner().setSelection(24);
		ILaunch launch = saveAndLaunch(wc, "testNumCallers"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--num-callers=24")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testErrorLimit() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getErrorLimitButton().setSelection(false);
		ILaunch launch = saveAndLaunch(wc, "testErrorLimit"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--error-limit=no")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testShowBelowMain() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getShowBelowMainButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testShowBelowMain"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--show-below-main=yes")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testMaxStackframe() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getMaxStackFrameSpinner().setSelection(50000000);
		ILaunch launch = saveAndLaunch(wc, "testMaxStackframe"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--max-stackframe=50000000")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testRunFreeRes() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getRunFreeresButton().setSelection(false);
		ILaunch launch = saveAndLaunch(wc, "testRunFreeRes"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--run-libc-freeres=no")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testAlignment() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();

		assertFalse(dynamicTab.getAlignmentSpinner().getEnabled());
		dynamicTab.getAlignmentButton().setSelection(true);
		dynamicTab.getAlignmentButton().notifyListeners(SWT.Selection, null);
		assertTrue(dynamicTab.getAlignmentSpinner().getEnabled());

		dynamicTab.getAlignmentSpinner().setSelection(512);
		tab.performApply(wc);
		config = wc.doSave();

		assertTrue(tab.isValid(config));

		ILaunch launch = doLaunch(config, "testAlignment"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--alignment=512")); //$NON-NLS-1$
	}

	public void testAlignmentBad() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();

		assertFalse(dynamicTab.getAlignmentSpinner().getEnabled());
		dynamicTab.getAlignmentButton().setSelection(true);
		dynamicTab.getAlignmentButton().notifyListeners(SWT.Selection, null);
		assertTrue(dynamicTab.getAlignmentSpinner().getEnabled());

		dynamicTab.getAlignmentSpinner().setSelection(63);
		tab.performApply(wc);
		config = wc.doSave();

		assertFalse(tab.isValid(config));
	}

	public void testNoLeakCheck() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getLeakCheckButton().setSelection(false);
		ILaunch launch = saveAndLaunch(wc, "testNoLeakCheck"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--leak-check=no")); //$NON-NLS-1$
	}

	public void testShowReachable() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getShowReachableButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testShowReachable"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--show-reachable=yes")); //$NON-NLS-1$
	}

	public void testLeakResolutionMed() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		String[] opts = dynamicTab.getLeakResCombo().getItems();
		int ix = Arrays.asList(opts).indexOf(
				MemcheckLaunchConstants.LEAK_RES_MED);
		dynamicTab.getLeakResCombo().select(ix);
		ILaunch launch = saveAndLaunch(wc, "testLeakResolutionMed"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--leak-resolution=med")); //$NON-NLS-1$
	}

	public void testLeakResolutionHigh() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		String[] opts = dynamicTab.getLeakResCombo().getItems();
		int ix = Arrays.asList(opts).indexOf(
				MemcheckLaunchConstants.LEAK_RES_HIGH);
		dynamicTab.getLeakResCombo().select(ix);
		ILaunch launch = saveAndLaunch(wc, "testLeakResolutionHigh"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--leak-resolution=high")); //$NON-NLS-1$
	}

	public void testFreeListVol() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getFreelistSpinner().setSelection(2000000);
		ILaunch launch = saveAndLaunch(wc, "testFreeListVol"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--freelist-vol=2000000")); //$NON-NLS-1$
	}

	public void testWorkaroundGCCBugs() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getGccWorkaroundButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testWorkaroundGCCBugs"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--workaround-gcc296-bugs=yes")); //$NON-NLS-1$
	}

	public void testPartialLoads() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getPartialLoadsButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testPartialLoads"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--partial-loads-ok=yes")); //$NON-NLS-1$
	}

	public void testUndefValueErrors() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getUndefValueButton().setSelection(false);
		ILaunch launch = saveAndLaunch(wc, "testUndefValueErrors"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		assertTrue("process array should not be empty", p.length > 0);
		String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
		assertEquals(0, p[0].getExitValue());
		assertTrue(cmd.contains("--undef-value-errors=no")); //$NON-NLS-1$
	}

	public void testMainStackSize() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		IProject project = CDebugUtils.verifyCProject(wc).getProject();
		Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion(
				project);
		if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
			assertFalse(tab.getMainStackSizeSpinner().isEnabled());
			tab.getMainStackSizeButton().setSelection(true);
			tab.getMainStackSizeButton().notifyListeners(SWT.Selection, null);
			assertTrue(tab.getMainStackSizeSpinner().isEnabled());
			tab.getMainStackSizeSpinner().setSelection(2048);
			ILaunch launch = saveAndLaunch(wc, "testMainStackFrame"); //$NON-NLS-1$
			IProcess[] p = launch.getProcesses();
			assertTrue("process array should not be empty", p.length > 0);
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--main-stacksize=2048")); //$NON-NLS-1$
		} else {
			assertNull(tab.getMainStackSizeButton());
			assertNull(tab.getMainStackSizeSpinner());
		}
	}

	public void testTrackOrigins() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		IProject project = CDebugUtils.verifyCProject(config).getProject();
		Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion(
				project);
		if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
			dynamicTab.getTrackOriginsButton().setSelection(true);
			ILaunch launch = saveAndLaunch(wc, "testTrackOrigins"); //$NON-NLS-1$
			IProcess[] p = launch.getProcesses();
			assertTrue("process array should not be empty", p.length > 0);
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--track-origins=yes")); //$NON-NLS-1$
		} else {
			assertNull(dynamicTab.getTrackOriginsButton());
		}
	}

	public void testTrackOriginsValidity() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		IProject project = CDebugUtils.verifyCProject(config).getProject();
		Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion(project);
		if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
			dynamicTab.getTrackOriginsButton().setSelection(true);
			tab.performApply(wc);
			assertTrue(tab.isValid(wc));
			dynamicTab.getUndefValueButton().setSelection(false);
			tab.performApply(wc);
			assertFalse(tab.isValid(wc));
		}
	}

	public void testValgrindError() throws Exception {
		String notExistentFile = "DOES NOT EXIST"; //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getSuppFileList().add(notExistentFile);
		tab.performApply(wc);
		config = wc.doSave();

		assertFalse(tab.isValid(config));

		doLaunch(config, "testValgrindError"); //$NON-NLS-1$

		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IValgrindMessage[] messages = view.getMessages();
		assertTrue(messages.length > 0);

		String text = messages[0].getText();
		assertTrue(text.contains(notExistentFile));
	}
}
