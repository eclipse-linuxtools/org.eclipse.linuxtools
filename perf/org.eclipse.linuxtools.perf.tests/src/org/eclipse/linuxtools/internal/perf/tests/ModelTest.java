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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.launch.PerfEventsTab;
import org.eclipse.linuxtools.internal.perf.launch.PerfOptionsTab;
import org.eclipse.linuxtools.internal.perf.model.PMCommand;
import org.eclipse.linuxtools.internal.perf.model.PMDso;
import org.eclipse.linuxtools.internal.perf.model.PMEvent;
import org.eclipse.linuxtools.internal.perf.model.PMFile;
import org.eclipse.linuxtools.internal.perf.model.PMSymbol;
import org.eclipse.linuxtools.internal.perf.model.TreeParent;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.osgi.framework.FrameworkUtil;

public class ModelTest extends AbstractTest {
	protected ILaunchConfiguration config;
	protected Stack<Class<?>> stack;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest"); //$NON-NLS-1$
		config = createConfiguration(proj.getProject());

		Class<?>[] klassList = new Class<?>[] { PMSymbol.class, PMFile.class,
				PMDso.class, PMCommand.class, PMEvent.class };
		stack = new Stack<Class<?>>();
		stack.addAll(Arrays.asList(klassList));
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(PerfPlugin.LAUNCHCONF_ID);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		PerfEventsTab eventsTab = new PerfEventsTab();
		PerfOptionsTab optionsTab = new PerfOptionsTab();
		wc.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, false);
		eventsTab.setDefaults(wc);
		optionsTab.setDefaults(wc);
	}

	public void testModelDefaultGenericStructure() {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		checkChildrenStructure(invisibleRoot, stack);
	}

	public void testModelMultiEventGenericStructure() {
		TreeParent invisibleRoot = buildModel(
				"resources/multievent-data/perf.data",
				"resources/multievent-data/perf.data.txt",
				"resources/multievent-data/perf.data.err.log");

		checkChildrenStructure(invisibleRoot, stack);
	}

	public void testPercentages() {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		checkChildrenPercentages(invisibleRoot, invisibleRoot.getPercent());
	}

	public void testParserMultiEvent() {
		TreeParent invisibleRoot = buildModel(
				"resources/multievent-data/perf.data",
				"resources/multievent-data/perf.data.txt",
				"resources/multievent-data/perf.data.err.log");

		assertEquals(invisibleRoot.getChildren().length, 5);

		String cur = null;

		for (TreeParent event : invisibleRoot.getChildren()) {

			cur = event.getName();

			// Assert specific properties extracted by the parser.
			if ("cpu-clock".equals(cur)) {
				assertTrue(event.hasChildren());
				assertEquals(event.getChildren().length, 1);

				TreeParent cmd = event.getChildren()[0];
				assertEquals(cmd.getChildren().length, 1);

				String[] cmdLabels = { "hellotest" };
				checkCommadLabels(cmdLabels, cmd);
			} else if ("task-clock".equals(cur)) {
				assertTrue(event.hasChildren());
				assertEquals(event.getChildren().length, 1);

				TreeParent cmd = event.getChildren()[0];
				assertEquals(cmd.getChildren().length, 1);

				String[] cmdLabels = { "hellotest" };
				checkCommadLabels(cmdLabels, cmd);
			} else if ("page-faults".equals(cur)) {
				assertTrue(event.hasChildren());
				assertEquals(event.getChildren().length, 1);

				TreeParent cmd = event.getChildren()[0];
				assertEquals(cmd.getChildren().length, 3);

				String[] cmdLabels = { "ld-2.14.90.so", "[kernel.kallsyms]",
						"libc-2.14.90.so" };
				checkCommadLabels(cmdLabels, cmd);
			} else if ("minor-faults".equals(cur)) {
				assertTrue(event.hasChildren());
				assertEquals(event.getChildren().length, 1);

				TreeParent cmd = event.getChildren()[0];
				assertEquals(cmd.getChildren().length, 3);

				String[] cmdLabels = { "ld-2.14.90.so", "[kernel.kallsyms]",
						"libc-2.14.90.so" };
				checkCommadLabels(cmdLabels, cmd);
			} else if ("major-faults".equals(cur)) {
				assertTrue(!event.hasChildren());
			}

		}
	}

	public void testParserDefaultEvent() {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		// Assert specific properties extracted by the parser.
		assertEquals(invisibleRoot.getChildren().length, 1);

		TreeParent event = invisibleRoot.getChildren()[0];
		assertEquals(event.getName(), "cycles");
		assertTrue(event.hasChildren());
		assertEquals(event.getChildren().length, 1);

		TreeParent cmd = event.getChildren()[0];
		assertTrue(cmd.hasChildren());
		assertEquals(cmd.getChildren().length, 4);

		String[] cmdLabels = { "hellotest", "[kernel.kallsyms]",
				"ld-2.14.90.so", "perf" };
		checkCommadLabels(cmdLabels, cmd);
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
				assertTrue(actualSum/sum <= 1.0 && actualSum/sum >= 0.99);
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

	/**
	 * Build model based on perf data file report.
	 * @param perfDataLoc location of perf data file
	 * @param perfTextDataLoc location of perf data text file
	 * @param perfErrorDataLoc location of error log file
	 * @return tree model based on perf data report.
	 */
	public TreeParent buildModel(String perfDataLoc, String perfTextDataLoc,
			String perfErrorDataLoc) {
		TreeParent invisibleRoot = new TreeParent("");
		BufferedReader input = null;
		BufferedReader error = null;

		try {
			input = new BufferedReader(new FileReader(perfTextDataLoc));
			error = new BufferedReader(new FileReader(perfErrorDataLoc));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		PerfCore.parseReport(config, null, null, perfDataLoc, null,
				invisibleRoot, false, input, error);
		return invisibleRoot;
	}

	/**
	 * Check whether the command labels in model rooted at cmd exist in
	 * list of labels cmdLabels.
	 * @param cmdLabels list of command labels
	 * @param cmd root of tree model
	 */
	public void checkCommadLabels(String[] cmdLabels, TreeParent cmd) {
		List<String> cmdList = new ArrayList<String>(Arrays.asList(cmdLabels));

		for (TreeParent dso : cmd.getChildren()) {
			assertTrue(cmdList.get(0).equals(dso.getName()));
			cmdList.remove(0);
		}
	}
}
