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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.perf.PerfCore;
import org.eclipse.linuxtools.perf.PerfPlugin;
import org.eclipse.linuxtools.perf.launch.PerfEventsTab;
import org.eclipse.linuxtools.perf.launch.PerfLaunchConfigDelegate;
import org.eclipse.linuxtools.perf.launch.PerfOptionsTab;
import org.eclipse.linuxtools.perf.model.PMCommand;
import org.eclipse.linuxtools.perf.model.PMDso;
import org.eclipse.linuxtools.perf.model.PMEvent;
import org.eclipse.linuxtools.perf.model.PMFile;
import org.eclipse.linuxtools.perf.model.PMSymbol;
import org.eclipse.linuxtools.perf.model.TreeParent;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;

public class ModelTest extends AbstractTest {
	protected ILaunchConfiguration config;
	protected PerfLaunchConfigDelegate delegate;
	protected ILaunch launch;
	protected ILaunchConfigurationWorkingCopy wc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild(TestPlugin.getDefault().getBundle(), "fibTest"); //$NON-NLS-1$
		config = createConfiguration(proj.getProject());

		delegate = new PerfLaunchConfigDelegate();
		launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);
		wc = config.getWorkingCopy();
		setProfileAttributes(wc);
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		wc.delete();
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
		wc.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, false);
		eventsTab.setDefaults(wc);
		optionsTab.setDefaults(wc);
	}

	public void testDefaultRun () {
		try {
			delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
		} catch (CoreException e) {
			fail();
		}
	}

	public void testClockEventRun () {
		try {
			ArrayList<String> list = new ArrayList<String>();
			list.addAll(Arrays.asList(new String [] {"cpu-clock", "task-clock", "cycles"}));
			wc.setAttribute(PerfPlugin.ATTR_DefaultEvent, false);
			wc.setAttribute(PerfPlugin.ATTR_SelectedEvents, list);
			delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
		} catch (CoreException e) {
			fail();
		}
	}

	public void testcheckModelGenericStructure () {
		PerfCore.Report(config, null, null, null, "resources/perf.data",null);
		TreeParent invisibleRoot = PerfPlugin.getDefault().getModelRoot();

		// model class structure, left element contained in right element
		Class<?> [] klassList = new Class<?> [] {PMSymbol.class, PMFile.class, PMDso.class, PMCommand.class, PMEvent.class};
		Stack<Class<?>> stack = new Stack<Class<?>> ();
		stack.addAll(Arrays.asList(klassList));

		checkChildrenStructure(invisibleRoot, stack);
	}

	public void testPercentages () {
		PerfCore.Report(config, null, null, null, "resources/perf.data",null);
		TreeParent invisibleRoot = PerfPlugin.getDefault().getModelRoot();

		checkChildrenPercentages (invisibleRoot, invisibleRoot.getPercent());
	}

	/**
	 * @param root some element that will serve as the root
	 * @param sum the expected sum of the percentages of this root's
	 * immediate children
	 */
	public void checkChildrenPercentages (TreeParent root, float sum) {
		float actualSum = 0;
		// If a root has no children we're done
		if (root.getChildren().length != 0) {
			for (TreeParent child : root.getChildren()) {
				actualSum += child.getPercent();
				checkChildrenPercentages(child, child.getPercent());
			}
			// some top-level elements have an undefined percentage but
			// their children have defined percentages
			// eg. the invisible root, and PMCommand
			if (actualSum != 100 && sum != -1){
				assertEquals(sum, actualSum);
			}
		}
	}

	/**
	 * @param root some element that will serve as the root
	 * @param stack a stack of classes
	 */
	public void checkChildrenStructure (TreeParent root, Stack<Class<?>> stack){
		if (stack.isEmpty()){
			return;
		}else{
			// children of root must be instances of the top class on the stack
			Class<?> klass = stack.pop();
			assertTrue(root.hasChildren());
			for (TreeParent tp : root.getChildren()){
				// tp.getClass() instanceof klass
				assertTrue(klass.isAssignableFrom(tp.getClass()));
				// each sibling needs its own stack
				Stack<Class<?>> newStack = new Stack<Class<?>>();
				newStack.addAll(Arrays.asList(stack.toArray(new Class<?> [] {})));
				checkChildrenStructure(tp, newStack);
			}
		}
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
		assertTrue(sym.getFunctionName().equals("leftfib"));
		assertTrue(file.getChildren()[1] instanceof PMSymbol);
		sym = ((PMSymbol)file.getChildren()[1]);
		assertTrue(sym.getFunctionName().equals("rightfib"));
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

		assertEquals(2, comm.getChildren().length);
		assertEquals("fib", comm.getChildren()[0].getName());
		assertEquals("[kernel.kallsyms]", comm.getChildren()[1].getName());

		PMDso dso = (PMDso)comm.getChildren()[0];
		
		assertTrue(dso.hasChildren());
		assertEquals(1, dso.getChildren().length);
		assertTrue(dso.getChildren()[0] instanceof PMFile);

		PMFile file = (PMFile)dso.getChildren()[0];
		assertTrue(file.hasChildren());
		assertEquals(2, file.getChildren().length);
		assertTrue(file.getChildren()[0] instanceof PMSymbol);

		PMSymbol sym = ((PMSymbol)file.getChildren()[0]);
		assertTrue(sym.getFunctionName().equals("leftfib"));
		assertTrue(file.getChildren()[1] instanceof PMSymbol);
		sym = ((PMSymbol)file.getChildren()[1]);
		assertTrue(sym.getFunctionName().equals("rightfib"));
	}
	
	public void checkModel(boolean LoadUnresolved) {
		
	}
}
