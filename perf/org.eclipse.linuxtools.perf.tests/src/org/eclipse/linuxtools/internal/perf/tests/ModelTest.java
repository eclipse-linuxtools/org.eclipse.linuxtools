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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.linuxtools.internal.perf.ui.PerfDoubleClickAction;
import org.eclipse.linuxtools.internal.perf.ui.PerfProfileView;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public class ModelTest extends AbstractTest {
	private ILaunchConfiguration config;
	private Stack<Class<?>> stack;

	@Before
	public void setUp() throws Exception {
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest"); //$NON-NLS-1$
		config = createConfiguration(proj.getProject());

		Class<?>[] klassList = new Class<?>[] { PMSymbol.class, PMFile.class,
				PMDso.class, PMCommand.class, PMEvent.class };
		stack = new Stack<Class<?>>();
		stack.addAll(Arrays.asList(klassList));
	}

	@After
	public void tearDown() throws Exception {
		deleteProject(proj);
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
	@Test
	public void testModelDefaultGenericStructure() {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		checkChildrenStructure(invisibleRoot, stack);
	}
	@Test
	public void testModelMultiEventGenericStructure() {
		TreeParent invisibleRoot = buildModel(
				"resources/multievent-data/perf.data",
				"resources/multievent-data/perf.data.txt",
				"resources/multievent-data/perf.data.err.log");

		checkChildrenStructure(invisibleRoot, stack);
	}
	@Test
	public void testPercentages() {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		checkChildrenPercentages(invisibleRoot, invisibleRoot.getPercent());
	}
	@Test
	public void testDoubleClickAction () {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		PerfPlugin.getDefault().setModelRoot(invisibleRoot);
		// update the model root for the view
		PerfCore.RefreshView("resources/defaultevent-data/perf.data");

		// number of parents excluding invisibleRoot
		int numOfParents = getNumberOfParents(invisibleRoot) - 1;

		// create a double click action to act on the tree viewer
		try {
			PerfProfileView view = (PerfProfileView) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.showView(PerfPlugin.VIEW_ID);
			TreeViewer tv = view.getTreeViewer();
			PerfDoubleClickAction dblClick = new PerfDoubleClickAction(tv);

			// double click every element
			doubleClickAllChildren(invisibleRoot, tv, dblClick);

			// If all elements are expanded, this is the number of elements
			// in our model that have children.
			assertEquals(numOfParents, tv.getExpandedElements().length);
		} catch (PartInitException e) {
			fail("Failed to open the Profiling View.");
		}
	}
	@Test
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
				assertFalse(event.hasChildren());
			}

		}
	}
	@Test
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
	@Test
	public void testParseEventList() {
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader("resources/simple-perf-event-list"));
		} catch (FileNotFoundException e) {
			fail();
		}

		HashMap<String, ArrayList<String>> eventList = PerfCore.parseEventList(input);
		for(String key : eventList.keySet()){
			if ("Raw hardware event descriptor".equals(key)) {
				assertTrue(eventList.get(key).contains("rNNN"));
				assertTrue(eventList.get(key).contains("cpu/t1=v1"));
			} else if ("Hardware breakpoint".equals(key)) {
				assertTrue(eventList.get(key).contains("mem:<addr>"));
			} else if ("Software event".equals(key)) {
				assertTrue(eventList.get(key).contains("cpu-clock"));
				assertTrue(eventList.get(key).contains("task-clock"));
			} else if ("Hardware cache event".equals(key)) {
				assertTrue(eventList.get(key).contains("L1-dcache-loads"));
				assertTrue(eventList.get(key).contains("L1-dcache-load-misses"));
			} else if ("Tracepoint event".equals(key)) {
				assertTrue(eventList.get(key).contains("mac80211:drv_return_void"));
				assertTrue(eventList.get(key).contains("mac80211:drv_return_int"));
			} else if ("Hardware event".equals(key)) {
				assertTrue(eventList.get(key).contains("cpu-cycles"));
				assertTrue(eventList.get(key).contains("stalled-cycles-frontend"));
			}
		}
	}
	@Test
	public void testParseAnnotation() {
		BufferedReader input = null;

		try {
			input = new BufferedReader(new FileReader(
					"resources/perf-annotation-data"));
		} catch (FileNotFoundException e) {
			fail();
		}

		// Set up arguments for the annotation parser.
		IPath workingDir = Path.fromOSString("/working/directory/");
		PMCommand cmd = new PMCommand("testCommand");
		PMDso dso = new PMDso("testDso", false);
		PMFile tmpFile = new PMFile(PerfPlugin.STRINGS_UnfiledSymbols);
		PMSymbol sym = new PMSymbol("testSym", 0, 0);

		// Set children and respective parents.
		cmd.addChild(dso);
		dso.addChild(tmpFile);
		tmpFile.addChild(sym);

		dso.setParent(cmd);
		tmpFile.setParent(dso);
		sym.setParent(tmpFile);

		PerfCore.parseAnnotation(null, input, workingDir, dso, sym);

		// Expected results data.
		String expectedDsoPath = "/working/directory/fibonacci";
		String expectedFilePath = "/home/user/workspace/fibonacci/Debug/../src/fibonacci.cpp";

		assertTrue(expectedDsoPath.equals(dso.getPath()));
		assertTrue(dso.getChildren().length == 2);

		for (TreeParent dsoChild : dso.getChildren()) {
			String filePath = ((PMFile) dsoChild).getPath();

			if (PerfPlugin.STRINGS_UnfiledSymbols.equals(filePath)) {
				assertFalse(dsoChild.hasChildren());
			} else {
				assertTrue(expectedFilePath.equals(filePath));
				assertTrue(dsoChild.hasChildren());
				assertTrue(dsoChild.getChildren().length == 1);

				TreeParent curSym = dsoChild.getChildren()[0];
				assertTrue(curSym.hasChildren());
				assertTrue(curSym.getChildren().length == 5);

				float percentCount = 0;
				for (TreeParent symChild : curSym.getChildren()) {
					percentCount += symChild.getPercent();
				}

				assertTrue(Math.ceil(percentCount) == 100.0);

			}
		}
	}

	@Test
	public void testAnnotateString() throws CoreException {
		ILaunchConfigurationWorkingCopy tempConfig = config.copy("test-config");
		tempConfig
				.setAttribute(PerfPlugin.ATTR_Kernel_Location, "/boot/kernel");
		tempConfig.setAttribute(PerfPlugin.ATTR_ModuleSymbols, true);

		String[] annotateString = PerfCore.getAnnotateString(tempConfig, "dso",
				"symbol", "resources/defaultevent-data/perf.data", false);

		String[] expectedString = new String[] { PerfPlugin.PERF_COMMAND,
				"annotate", "-d", "dso", "-s", "symbol", "-l", "-P",
				"--vmlinux", "/boot/kernel", "-m", "-i",
				"resources/defaultevent-data/perf.data" };

		assertArrayEquals(expectedString, annotateString);
	}

	@Test
	public void testRecordString() throws CoreException {
		ILaunchConfigurationWorkingCopy tempConfig = config.copy("test-config");
		tempConfig.setAttribute(PerfPlugin.ATTR_Record_Realtime, true);
		tempConfig.setAttribute(PerfPlugin.ATTR_Record_Verbose, true);
		tempConfig.setAttribute(PerfPlugin.ATTR_Multiplex, true);

		ArrayList<String> selectedEvents = new ArrayList<String>();
		selectedEvents.add("cpu-cycles");
		selectedEvents.add("cache-misses");
		selectedEvents.add("cpu-clock");
		tempConfig.setAttribute(PerfPlugin.ATTR_SelectedEvents, selectedEvents);

		tempConfig.setAttribute(PerfPlugin.ATTR_DefaultEvent, false);

		String[] recordString = PerfCore.getRecordString(tempConfig);
		assertNotNull(recordString);

		String[] expectedString = { PerfPlugin.PERF_COMMAND, "record", "-f",
				"-r", "-v", "-M", "-e", "cpu-cycles", "-e", "cache-misses",
				"-e", "cpu-clock" };
		assertArrayEquals(expectedString, recordString);
	}

	@Test
	public void testReportString() throws CoreException {
		ILaunchConfigurationWorkingCopy tempConfig = null;
		tempConfig = config.copy("test-config");
		tempConfig
				.setAttribute(PerfPlugin.ATTR_Kernel_Location, "/boot/kernel");
		tempConfig.setAttribute(PerfPlugin.ATTR_ModuleSymbols, true);

		String[] reportString = PerfCore.getReportString(tempConfig,
				"resources/defaultevent-data/perf.data");
		assertNotNull(reportString);

		String[] expectedString = { PerfPlugin.PERF_COMMAND, "report",
				"--sort", "comm,dso,sym", "-n", "-t", "" + (char) 1,
				"--vmlinux", "/boot/kernel", "-m", "-i",
				"resources/defaultevent-data/perf.data" };
		assertArrayEquals(expectedString, reportString);
	}

	/**
	 * @param root some element that will serve as the root
	 * @param sum the expected sum of the percentages of this root's
	 * immediate children
	 */
	private void checkChildrenPercentages (TreeParent root, float sum) {
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
	private void checkChildrenStructure (TreeParent root, Stack<Class<?>> stack){
		if (!stack.isEmpty()){
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
	 * Performs a Perf double-click action on every element in the
	 * TreeViewer model.
	 *
	 * @param root some element that will serve as the root
	 * @param tv a TreeViewer containing elements from the Perf model
	 * @param dblClick the double-click action to perform on every
	 * element of the TreeViewer.
	 */
	private void doubleClickAllChildren(TreeParent root, TreeViewer tv,
			PerfDoubleClickAction dblClick) {

		for (TreeParent child : root.getChildren()) {
			// see PerfDoubleClickAction for IStructuredSelection
			tv.setSelection(new StructuredSelection(child));
			dblClick.run();
			doubleClickAllChildren(child, tv, dblClick);
		}
	}

	/**
	 * Find the number of ancestors of the given root that have children.
	 * This includes the given root in the computation.
	 *
	 * @param root some element that will serve as the root
	 * @return the number of elements under, and including the
	 * given root, that have children elements.
	 */
	private int getNumberOfParents(TreeParent root) {
		int ret = root.hasChildren() ? 1 : 0;
		for (TreeParent child : root.getChildren()) {
			ret += getNumberOfParents(child);
		}
		return ret;
	}

	/**
	 * Build model based on perf data file report.
	 * @param perfDataLoc location of perf data file
	 * @param perfTextDataLoc location of perf data text file
	 * @param perfErrorDataLoc location of error log file
	 * @return tree model based on perf data report.
	 */
	private TreeParent buildModel(String perfDataLoc, String perfTextDataLoc,
			String perfErrorDataLoc) {
		TreeParent invisibleRoot = new TreeParent("");
		BufferedReader input = null;
		BufferedReader error = null;

		try {
			input = new BufferedReader(new FileReader(perfTextDataLoc));
			error = new BufferedReader(new FileReader(perfErrorDataLoc));
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
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
	private void checkCommadLabels(String[] cmdLabels, TreeParent cmd) {
		List<String> cmdList = new ArrayList<String>(Arrays.asList(cmdLabels));

		for (TreeParent dso : cmd.getChildren()) {
			assertTrue(cmdList.get(0).equals(dso.getName()));
			cmdList.remove(0);
		}
	}
}
