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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartEditorInput;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartPNG;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.HeapChart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChartExportTest extends AbstractMassifTest {
	private IPath pngPath;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$

		pngPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		assertNotNull(pngPath);
		pngPath = pngPath.append("alloctest").append("chart.png"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		File chartFile = pngPath.toFile();
		if (chartFile.exists()) {
			chartFile.delete();
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
		HeapChart chart = ((ChartEditorInput) input).getChart();

		ChartPNG png = new ChartPNG(chart);
		png.renderPNG(pngPath);

		File chartFile = pngPath.toFile();
		assertTrue(chartFile.exists());
		assertTrue(chartFile.length() > 0);
	}

}
