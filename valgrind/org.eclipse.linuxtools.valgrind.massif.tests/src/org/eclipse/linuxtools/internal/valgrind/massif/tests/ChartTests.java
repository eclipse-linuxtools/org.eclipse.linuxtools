/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifLaunchConstants;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartEditor;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartEditorInput;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartLocationsDialog;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.HeapChart;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.Messages;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

public class ChartTests extends AbstractMassifTest {
	@Before
	public void prep() throws Exception {
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		deleteProject(proj);
		super.tearDown();
	}

	@Test
	public void testEditorName() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testEditorName"); //$NON-NLS-1$

		IAction chartAction = getChartAction();
		assertNotNull(chartAction);
		chartAction.run();

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		assertTrue(part.getTitle().startsWith("Heap Chart - alloctest")); //$NON-NLS-1$
	}

	@Test
	public void testByteScalingKiB() throws Exception {
		byteScalingHelper(1, 1, 1024 * 10, "testByteScalingKiB"); //$NON-NLS-1$
	}

	@Test
	public void testByteScalingMiB() throws Exception {
		byteScalingHelper(2, 1, 1024 * 1024 * 10, "testByteScalingMiB"); //$NON-NLS-1$
	}

	@Test
	public void testByteScalingGiB() throws Exception {
		byteScalingHelper(3, 1024, 1024 * 1024 * 10, "testByteScalingGiB"); //$NON-NLS-1$
	}

	@Test
	public void testByteScalingTiB() throws Exception {
		byteScalingHelper(4, 1024 * 1024, 1024 * 1024 * 10,
				"testByteScalingTiB"); //$NON-NLS-1$
	}

	@Test
	public void testChartCallback() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testChartCallback"); //$NON-NLS-1$

		IAction chartAction = getChartAction();
		assertNotNull(chartAction);
		chartAction.run();

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		assertTrue(part instanceof ChartEditor);
		Chart control = ((ChartEditor) part).getControl();
		ILineSeries lsTotal = (ILineSeries) control.getSeriesSet().getSeries(
				Messages.getString("HeapChart.Total_Heap")); //$NON-NLS-1$
		Point p1 = lsTotal.getPixelCoordinates(4);

		HeapChart heapChart = ((ChartEditorInput) ((ChartEditor) part)
				.getEditorInput()).getChart();
		int x = control.getAxisSet().getXAxis(0)
				.getPixelCoordinate(heapChart.time[4]);
		int y = control.getAxisSet().getYAxis(0)
				.getPixelCoordinate(heapChart.dataTotal[4]);

		assertEquals(x, p1.x);
		assertEquals(y, p1.y);
	}

	@Test
	public void testChartLocationsDialog() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testChartCallback"); //$NON-NLS-1$

		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault()
				.getView().getDynamicView();
		MassifSnapshot snapshot = view.getSnapshots()[7]; // peak
		assertTrue(snapshot.isDetailed());

		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		ChartLocationsDialog dialog = new ChartLocationsDialog(parent);
		dialog.setInput(snapshot);
		dialog.setBlockOnOpen(false);
		dialog.open();

		MassifHeapTreeNode element = snapshot.getRoot().getChildren()[1];
		dialog.getTableViewer().setSelection(new StructuredSelection(element));
		dialog.getOkButton().notifyListeners(SWT.Selection, null);
		dialog.openEditorForResult();

		checkFile(proj.getProject(), element);
		checkLine(element);
	}

	private void byteScalingHelper(int ix, long times, long bytes,
			String testName) throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				String.valueOf(bytes) + " " + String.valueOf(times)); //$NON-NLS-1$
		wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_TIMEUNIT,
				MassifLaunchConstants.TIME_B);
		config = wc.doSave();

		doLaunch(config, testName);

		IAction chartAction = getChartAction();
		assertNotNull(chartAction);
		chartAction.run();

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		assertTrue(part.getEditorInput() instanceof ChartEditorInput);
		ChartEditorInput input = (ChartEditorInput) part.getEditorInput();
		HeapChart chart = input.getChart();
		assertEquals(HeapChart.getByteUnits()[ix], chart.getXUnits());
	}

	private IAction getChartAction() {
		return getToolbarAction(MassifViewPart.CHART_ACTION);
	}

}
