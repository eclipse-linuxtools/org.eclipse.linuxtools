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
package org.eclipse.linuxtools.valgrind.memcheck.tests;

import java.util.Arrays;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckLaunchConstants;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckPlugin;
import org.eclipse.linuxtools.valgrind.tests.ValgrindTestOptionsTab;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IOConsole;

public class LaunchConfigTabTest extends AbstractMemcheckTest {

	protected ValgrindTestOptionsTab tab;
	protected MemcheckTestToolPage dynamicTab;
	protected ILaunchConfiguration config;
	protected Shell testShell;
	private boolean consoleFinished;

	@Override
	protected void setUp() throws Exception {
		proj = createProject("basicTest"); //$NON-NLS-1$

		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		config = createConfiguration(bin);

		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
		tab = new ValgrindTestOptionsTab(new MemcheckLaunchMockPlugin());
	}

	@Override
	protected void tearDown() throws Exception {
		tab.dispose();
		testShell.dispose();
		deleteProject(proj);
	}

	private ILaunchConfigurationWorkingCopy initConfig() throws CoreException {
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		tab.setDefaults(wc);
		tab.createControl(testShell);
		tab.initializeFrom(config);
		int ix = Arrays.asList(tab.getTools()).indexOf(MemcheckPlugin.TOOL_ID);
		tab.getToolsCombo().select(ix);
		ILaunchConfigurationTab dynamicTab = tab.getDynamicTab();
		if (dynamicTab != null && dynamicTab instanceof MemcheckTestToolPage) {
			this.dynamicTab = (MemcheckTestToolPage) dynamicTab;
		}
		else {
			fail();
		}
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
			assertTrue(cmd.contains("--xml=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("-q")); //$NON-NLS-1$
			assertTrue(cmd.contains("--trace-children=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--child-silent-after-fork=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--demangle=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--num-callers=12")); //$NON-NLS-1$
			assertTrue(cmd.contains("--error-limit=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--show-below-main=no")); //$NON-NLS-1$
			assertFalse(cmd.contains("--suppressions")); //$NON-NLS-1$
			assertTrue(cmd.contains("--max-stackframe=2000000")); //$NON-NLS-1$
			assertTrue(cmd.contains("--alignment=8")); //$NON-NLS-1$
			assertTrue(cmd.contains("--run-libc-freeres=yes")); //$NON-NLS-1$

			assertTrue(cmd.contains("--leak-check=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--show-reachable=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--leak-resolution=low")); //$NON-NLS-1$
			assertTrue(cmd.contains("--freelist-vol=10000000")); //$NON-NLS-1$
			assertTrue(cmd.contains("--workaround-gcc296-bugs=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--partial-loads-ok=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--undef-value-errors=yes")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testWSSuppresions() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		String text = "${workspace_loc:/basicTest/testsuppfile.supp}"; //$NON-NLS-1$
		tab.getSuppFileText().setText(text);
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
		tab.getSuppFileText().setText(suppPath.toOSString());
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

	public void testSuppressionsSpaces() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		IPath suppPath = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path("basicTest/test suppfile.supp")).getLocation(); //$NON-NLS-1$
		tab.getSuppFileText().setText(suppPath.toOSString());
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
		dynamicTab.getAlignmentSpinner().setSelection(512);
		tab.performApply(wc);
		config = wc.doSave();

		assertTrue(tab.isValid(config));

		ILaunch launch = doLaunch(config, "testAlignment"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--alignment=512")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testAlignmentBad() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getAlignmentSpinner().setSelection(63);
		tab.performApply(wc);
		config = wc.doSave();

		assertFalse(tab.isValid(config));
	}

	public void testShowReachable() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getShowReachableButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testShowReachable"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--show-reachable=yes")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testLeakResolutionMed() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		String[] opts = dynamicTab.getLeakResCombo().getItems();
		int ix = Arrays.asList(opts).indexOf(MemcheckLaunchConstants.LEAK_RES_MED);
		dynamicTab.getLeakResCombo().select(ix);
		ILaunch launch = saveAndLaunch(wc, "testLeakResolutionMed"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--leak-resolution=med")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testLeakResolutionHigh() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		String[] opts = dynamicTab.getLeakResCombo().getItems();
		int ix = Arrays.asList(opts).indexOf(MemcheckLaunchConstants.LEAK_RES_HIGH);
		dynamicTab.getLeakResCombo().select(ix);
		ILaunch launch = saveAndLaunch(wc, "testLeakResolutionHigh"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--leak-resolution=high")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testFreeListVol() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getFreelistSpinner().setSelection(2000000);
		ILaunch launch = saveAndLaunch(wc, "testFreeListVol"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--freelist-vol=2000000")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testWorkaroundGCCBugs() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getGccWorkaroundButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testWorkaroundGCCBugs"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--workaround-gcc296-bugs=yes")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testPartialLoads() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getPartialLoadsButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testPartialLoads"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--partial-loads-ok=yes")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testUndefValueErrors() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getUndefValueButton().setSelection(false);
		ILaunch launch = saveAndLaunch(wc, "testUndefValueErrors"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--undef-value-errors=no")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testValgrindError() throws Exception {
		String notExistentFile = "DOES NOT EXIST"; //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy wc = initConfig();
		tab.getSuppFileText().setText(notExistentFile);
		tab.performApply(wc);
		config = wc.doSave();

		assertFalse(tab.isValid(config));

		ILaunch launch = doLaunch(config, "testValgrindError"); //$NON-NLS-1$

		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			assertTrue(p[0].getExitValue() != 0);
			IOConsole console = (IOConsole) DebugUITools.getConsole(p[0]);
			IPropertyChangeListener consoleListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(IConsoleConstants.P_CONSOLE_OUTPUT_COMPLETE)) {
						consoleFinished = true;
					}
				}				
			};

			// must be atomic, otherwise we could wait infinitely
			synchronized (console) {
				// only output should be from Valgrind
				if (console.getDocument().getLength() > 0) {
					consoleFinished = true;
				}
				else {
					consoleFinished = false;					
					console.addPropertyChangeListener(consoleListener);
				}
			}

			// sleep until console done
			Display display = Display.getCurrent();
			while (!consoleFinished) {
				if (display != null) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
				else {
					Thread.sleep(1000);
				}
			}

			String text = console.getDocument().get();
			assertTrue(text.contains(notExistentFile));
		}
		else {
			fail();
		}
	}
}
