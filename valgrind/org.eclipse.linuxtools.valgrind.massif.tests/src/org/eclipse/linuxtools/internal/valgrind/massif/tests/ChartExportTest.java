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
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.dataviewers.charts.actions.SaveChartAction;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartEditorInput;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChartExportTest extends AbstractMassifTest {
	private String[] pathNames = new String[]{"chart.png", "chart.jpg", "chart.jpeg", "chart.bmp"};
	private ArrayList<IPath> paths = new ArrayList<>();

	@Before
	public void prep() throws Exception {
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$

		IPath basePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		assertNotNull(basePath);
		basePath = basePath.append("alloctest");
		for (String pathName : pathNames) {
			paths.add(basePath.append(pathName));
		}
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		for (IPath path : paths) {
			File chartFile = path.toFile();
			if (chartFile.exists()) {
				chartFile.delete();
			}
		}

		deleteProject(proj);
		super.tearDown();
	}

	@Test
	public void testChartExportPNG() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testDefaults"); //$NON-NLS-1$

		IEditorInput input = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor()
				.getEditorInput();
		assertTrue("input must be ChartEditorInput",
				input instanceof ChartEditorInput);

		Composite control = ((ChartEditorInput) input).getChart().getChartControl();
		if (control.getSize().x == 0 || control.getSize().y == 0) {
			// Manually resize the composite to non-zero width/height so it can be saved
			control.setSize(10, 10);
		}

		SaveChartAction saveChartAction = (SaveChartAction) getToolbarAction(MassifViewPart.SAVE_CHART_ACTION);
		assertNotNull(saveChartAction);

		for (IPath path : paths) {
			saveAsPath(saveChartAction, path);
		}
	}

	private void saveAsPath(SaveChartAction saveChartAction, IPath path) {
		saveChartAction.run(path.toString());
		File chartFile = path.toFile();
		assertTrue(chartFile.exists());
		assertTrue(chartFile.length() > 0);
	}

}
