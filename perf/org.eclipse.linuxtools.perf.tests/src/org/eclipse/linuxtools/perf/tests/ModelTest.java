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
package org.eclipse.linuxtools.perf.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.perf.PerfCore;
import org.eclipse.linuxtools.perf.PerfPlugin;
import org.eclipse.linuxtools.perf.launch.PerfEventsTab;
import org.eclipse.linuxtools.perf.launch.PerfOptionsTab;
import org.eclipse.linuxtools.perf.model.PMCommand;
import org.eclipse.linuxtools.perf.model.PMDso;
import org.eclipse.linuxtools.perf.model.PMEvent;
import org.eclipse.linuxtools.perf.model.PMFile;
import org.eclipse.linuxtools.perf.model.PMSymbol;
import org.eclipse.linuxtools.perf.model.TreeParent;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ModelTest extends AbstractTest {
	protected ILaunchConfiguration config;
	protected Shell testShell;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild(TestPlugin.getDefault().getBundle(), "fibTest"); //$NON-NLS-1$
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
		wc.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, false);
	}

	public void testModel_ResolvedOnly() throws CoreException {
		PerfCore.Report(config, null, null, null, "resources/perf.data",null);
		
		TreeParent invisibleRoot = PerfPlugin.getDefault().getModelRoot();
		assertTrue(invisibleRoot.hasChildren());
		assertEquals(1, invisibleRoot.getChildren().length);
		assertTrue(invisibleRoot.getChildren()[0] instanceof PMEvent);
		PMEvent ev = (PMEvent)invisibleRoot.getChildren()[0];
		assertEquals("Default Event", ev.getName());
		assertTrue(ev.hasChildren());
		assertEquals(1, ev.getChildren().length);
		assertTrue(ev.getChildren()[0] instanceof PMCommand);
		PMCommand comm = (PMCommand)ev.getChildren()[0];
		assertTrue(comm.hasChildren());
		for (TreeParent x : comm.getChildren()) {
			assertTrue(x instanceof PMDso);
		}
		
		assertEquals(2, comm.getChildren().length);
		PMDso dso = (PMDso)comm.getChildren()[0];
		
		assertTrue(dso.hasChildren());
		assertEquals(1, dso.getChildren().length);
		assertTrue(dso.getChildren()[0] instanceof PMFile);
		PMFile file = (PMFile)dso.getChildren()[0];
		assertTrue(file.hasChildren());
		assertEquals(2, file.getChildren().length);
		assertTrue(file.getChildren()[0] instanceof PMSymbol);
		PMSymbol sym = ((PMSymbol)file.getChildren()[0]);
		assertTrue(sym.getFunctionName().equals("rightfib"));
		assertTrue(file.getChildren()[1] instanceof PMSymbol);
		sym = ((PMSymbol)file.getChildren()[1]);
		assertTrue(sym.getFunctionName().equals("leftfib"));
	}
	
	public void testModel_Unresolved() throws CoreException {
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, false);
		config = wc.doSave();
		
		PerfCore.Report(config, null, null, null, "resources/perf.data",null);
		
		TreeParent invisibleRoot = PerfPlugin.getDefault().getModelRoot();
		assertTrue(invisibleRoot.hasChildren());
		assertEquals(1, invisibleRoot.getChildren().length);
		assertTrue(invisibleRoot.getChildren()[0] instanceof PMEvent);
		PMEvent ev = (PMEvent)invisibleRoot.getChildren()[0];
		assertEquals("Default Event", ev.getName());
		assertTrue(ev.hasChildren());
		assertEquals(1, ev.getChildren().length);
		assertTrue(ev.getChildren()[0] instanceof PMCommand);
		PMCommand comm = (PMCommand)ev.getChildren()[0];
		assertTrue(comm.hasChildren());	
		for (TreeParent x : comm.getChildren()) {
			assertTrue(x instanceof PMDso);
		}
		
		assertEquals(4, comm.getChildren().length);
		assertTrue(comm.getChildren()[3].getName().equals("ld-2.10.1.so"));
		assertTrue(comm.getChildren()[0].getName().equals("80487c2"));
		PMDso dso = (PMDso)comm.getChildren()[1];
		
		assertTrue(dso.hasChildren());
		assertEquals(1, dso.getChildren().length);
		assertTrue(dso.getChildren()[0] instanceof PMFile);
		PMFile file = (PMFile)dso.getChildren()[0];
		assertTrue(file.hasChildren());
		assertEquals(2, file.getChildren().length);
		assertTrue(file.getChildren()[0] instanceof PMSymbol);
		PMSymbol sym = ((PMSymbol)file.getChildren()[1]);
		assertTrue(sym.getFunctionName().equals("leftfib"));
		assertTrue(file.getChildren()[1] instanceof PMSymbol);
		sym = ((PMSymbol)file.getChildren()[0]);
		assertTrue(sym.getFunctionName().equals("rightfib"));
	}
	
	public void checkModel(boolean LoadUnresolved) {
		
	}
}
