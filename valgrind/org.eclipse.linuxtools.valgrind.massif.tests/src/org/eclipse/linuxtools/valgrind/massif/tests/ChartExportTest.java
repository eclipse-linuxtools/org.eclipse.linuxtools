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
package org.eclipse.linuxtools.valgrind.massif.tests;

import java.io.File;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.massif.birt.ChartEditorInput;
import org.eclipse.linuxtools.valgrind.massif.birt.ChartSVG;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;

public class ChartExportTest extends AbstractMassifTest {
	private IPath svgPath;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
		
		svgPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		assertNotNull(svgPath);
		svgPath = svgPath.append("alloctest").append("chart.svg"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	protected void tearDown() throws Exception {
		File chartFile = svgPath.toFile();
		if (chartFile.exists()) {
			chartFile.delete();
		}
		
		deleteProject(proj);
		super.tearDown();
	}
	
	public void testChartExportSVG() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testDefaults"); //$NON-NLS-1$
		
		IEditorInput input = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (input instanceof ChartEditorInput) {
			Chart chart = ((ChartEditorInput) input).getChart();
			
			ChartSVG svg = new ChartSVG(chart);
			svg.renderSVG(svgPath);
			
			File chartFile = svgPath.toFile();
			assertTrue(chartFile.exists());
			assertTrue(chartFile.length() > 0);
		} else {
			fail();
		}
	}
	
}
