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
package org.eclipse.linuxtools.valgrind.massif.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.linuxtools.valgrind.massif.MassifToolPage;
import org.eclipse.linuxtools.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.valgrind.massif.birt.ChartEditorInput;
import org.eclipse.linuxtools.valgrind.massif.birt.HeapChart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.valgrind.ui.ValgrindViewPart;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

public class ChartTests extends AbstractMassifTest {
	@Override
	protected void setUp() throws Exception {
		proj = createProject("alloctest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
	}
	
	public void testEditorName() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		config.launch(ILaunchManager.PROFILE_MODE, null, true);
				
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IAction chartAction = getChartAction(view);
		assertNotNull(chartAction);
		chartAction.run();
		
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		assertEquals("Heap Chart - alloctest", part.getTitle()); //$NON-NLS-1$
	}
	
	public void testByteScalingKiB() throws Exception {
		byteScalingHelper(1, 1, 1024 * 10);
	}
	
	public void testByteScalingMiB() throws Exception {
		byteScalingHelper(2, 1, 1024 * 1024 * 10);
	}
	
	public void testByteScalingGiB() throws Exception {
		byteScalingHelper(3, 1024, 1024 * 1024 * 10);
	}

	public void testByteScalingTiB() throws Exception {
		byteScalingHelper(4, 1024 * 1024, 1024 * 1024 *10);
	}
	
	private void byteScalingHelper(int ix, long times, long bytes) throws CModelException, CoreException {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, String.valueOf(bytes) + " " + String.valueOf(times)); //$NON-NLS-1$
		wc.setAttribute(MassifToolPage.ATTR_MASSIF_TIMEUNIT, MassifToolPage.TIME_B);
		config = wc.doSave();
		
		config.launch(ILaunchManager.PROFILE_MODE, null, true);
				
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IAction chartAction = getChartAction(view);
		assertNotNull(chartAction);
		chartAction.run();
		
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part.getEditorInput() instanceof ChartEditorInput) {
			ChartEditorInput input = (ChartEditorInput) part.getEditorInput();
			HeapChart chart = input.getChart();
			assertEquals(HeapChart.getByteUnits()[ix], chart.getXUnits());
		} else {
			fail();
		}
	}
	private IAction getChartAction(IViewPart view) {
		IAction result = null;
		IToolBarManager manager = view.getViewSite().getActionBars().getToolBarManager();
		for (IContributionItem item : manager.getItems()) {
			if (item instanceof ActionContributionItem) {
				ActionContributionItem actionItem = (ActionContributionItem) item;
				if (actionItem.getAction().getId().equals(MassifViewPart.CHART_ACTION)) {
					result = actionItem.getAction();
				}
			}
		}
		return result;
	}
}
