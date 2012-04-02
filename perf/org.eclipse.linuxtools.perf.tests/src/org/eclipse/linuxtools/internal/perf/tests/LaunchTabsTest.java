/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.launch.PerfEventsTab;
import org.eclipse.linuxtools.internal.perf.launch.PerfOptionsTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.osgi.framework.FrameworkUtil;

public class LaunchTabsTest extends AbstractTest {
	protected ILaunchConfiguration config;
	protected Shell testShell;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest"); //$NON-NLS-1$
		config = createConfiguration(proj.getProject());
		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
	}

	@Override
	protected void tearDown() throws Exception {
		testShell.dispose();
		deleteProject(proj);
		super.tearDown();
	}
	
	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(PerfPlugin.LAUNCHCONF_ID);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc)
			throws CoreException {
		PerfEventsTab eventsTab = new PerfEventsTab();
		PerfOptionsTab optionsTab = new PerfOptionsTab();
		eventsTab.setDefaults(wc);
		optionsTab.setDefaults(wc);
	}
	
	
	//getter functions for otherwise unaccessible member variables 
	private class TestOptionsTab extends PerfOptionsTab {
		protected Text get_txtKernel_Location() { return _txtKernel_Location; }
		protected Button get_chkRecord_Realtime() { return _chkRecord_Realtime; }
		protected Button get_chkRecord_Verbose() { return _chkRecord_Verbose; }
		protected Button get_chkSourceLineNumbers() { return _chkSourceLineNumbers; }
		protected Button get_chkKernel_SourceLineNumbers() { return _chkKernel_SourceLineNumbers; }
		protected Button get_chkMultiplexEvents() { return _chkMultiplexEvents; }
		protected Button get_chkModuleSymbols() { return _chkModuleSymbols; }
		protected Button get_chkHideUnresolvedSymbols() { return _chkHideUnresolvedSymbols; }
	}
	
	public void testOptionsTab() throws CoreException {
		TestOptionsTab tab = new TestOptionsTab();
		tab.createControl(new Shell());
		assertNotNull(tab.getImage());
		assertNotNull(tab.getName());
		
		//default config
		tab.setDefaults(config.getWorkingCopy());
		tab.initializeFrom(config);
		assertTrue(tab.isValid(config));
		
		Button rrCheck = tab.get_chkRecord_Realtime();
		rrCheck.setSelection(true);
		rrCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(true, config.getAttribute(PerfPlugin.ATTR_Record_Realtime, false));
		rrCheck.setSelection(false);
		rrCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(false, config.getAttribute(PerfPlugin.ATTR_Record_Realtime, true));
		
		Button rvCheck = tab.get_chkRecord_Verbose();
		rvCheck.setSelection(true);
		rvCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(true, config.getAttribute(PerfPlugin.ATTR_Record_Verbose, false));
		rvCheck.setSelection(false);
		rvCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(false, config.getAttribute(PerfPlugin.ATTR_Record_Verbose, true));
		
		Button slcCheck = tab.get_chkSourceLineNumbers();
		slcCheck.setSelection(true);
		slcCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(true, config.getAttribute(PerfPlugin.ATTR_SourceLineNumbers, false));
		slcCheck.setSelection(false);
		slcCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(false, config.getAttribute(PerfPlugin.ATTR_SourceLineNumbers, true));
		
		Button kslcCheck = tab.get_chkKernel_SourceLineNumbers();
		kslcCheck.setSelection(true);
		kslcCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(true, config.getAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, false));
		kslcCheck.setSelection(false);
		kslcCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(false, config.getAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, true));
		
		Button meCheck = tab.get_chkMultiplexEvents();
		meCheck.setSelection(true);
		meCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(true, config.getAttribute(PerfPlugin.ATTR_Multiplex, false));
		meCheck.setSelection(false);
		meCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(false, config.getAttribute(PerfPlugin.ATTR_Multiplex, true));
		
		Button msCheck = tab.get_chkModuleSymbols();
		msCheck.setSelection(true);
		msCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(true, config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, false));
		msCheck.setSelection(false);
		msCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(false, config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, true));
		
		Button husCheck = tab.get_chkHideUnresolvedSymbols();
		husCheck.setSelection(true);
		husCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(true, config.getAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, false));
		husCheck.setSelection(false);
		husCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertEquals(false, config.getAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, true));
		
		rrCheck.setSelection(true);
		rrCheck.notifyListeners(SWT.Selection, null);
		rvCheck.setSelection(true);
		rvCheck.notifyListeners(SWT.Selection, null);
		slcCheck.setSelection(true);
		slcCheck.notifyListeners(SWT.Selection, null);
		kslcCheck.setSelection(true);
		kslcCheck.notifyListeners(SWT.Selection, null);
		meCheck.setSelection(true);
		meCheck.notifyListeners(SWT.Selection, null);
		msCheck.setSelection(true);
		msCheck.notifyListeners(SWT.Selection, null);
		husCheck.setSelection(true);
		husCheck.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		tab.initializeFrom(config);
		assertTrue(rrCheck.getSelection());
		assertTrue(rvCheck.getSelection());
		assertTrue(slcCheck.getSelection());
		assertTrue(kslcCheck.getSelection());
		assertTrue(meCheck.getSelection());
		assertTrue(msCheck.getSelection());
		assertTrue(husCheck.getSelection());
		
		Text klocText = tab.get_txtKernel_Location();
		klocText.setText("doesntexist"); //$NON-NLS-1$
		klocText.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertFalse(tab.isValid(config));

		klocText.setText(""); //$NON-NLS-1$
		klocText.notifyListeners(SWT.Selection, null);
		tab.performApply(config.getWorkingCopy());
		assertTrue(tab.isValid(config));
	}
	
	//getter functions for otherwise unaccessible member variables 
	private class TestEventsTab extends PerfEventsTab {
		public Button get_chkDefaultEvent() { return _chkDefaultEvent; }
	}
	
	public void testEventsTab() throws CoreException {
		TestEventsTab tab = new TestEventsTab();
		tab.createControl(new Shell());
		assertNotNull(tab.getImage());
		assertNotNull(tab.getName());
		
		//default config
		tab.setDefaults(config.getWorkingCopy());
		tab.initializeFrom(config);
		assertTrue(tab.isValid(config));
		
		assertTrue(tab.get_chkDefaultEvent().getSelection());
		
		tab.get_chkDefaultEvent().notifyListeners(SWT.Selection, null);		
		tab.get_chkDefaultEvent().setSelection(false);
		tab.get_chkDefaultEvent().notifyListeners(SWT.Selection, null);
		assertFalse(tab.get_chkDefaultEvent().getSelection());
	}

}
