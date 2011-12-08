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
package org.eclipse.linuxtools.internal.valgrind.massif.birt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

public class ChartEditor extends EditorPart {
	protected Chart control;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (input instanceof ChartEditorInput) {
			setInput(input);
			setSite(site);
			setPartName(NLS.bind(Messages.getString("ChartEditor.Heap_Chart"), input.getName())); //$NON-NLS-1$
		}
		else {
			throw new PartInitException(NLS.bind(Messages.getString("ChartEditor.Editor_input_must_be"), ChartEditorInput.class.getName())); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		ChartEditorInput input = (ChartEditorInput) getEditorInput();
		control = new Chart(parent, SWT.FILL);

		Color color = new Color(Display.getDefault(), 255, 128, 128);

		control.setBackground(color);
		control.setBackgroundInPlotArea(color);

		// titles
		control.getTitle().setText("Valgrind Title");
		control.getAxisSet().getXAxis(0).getTitle().setText(input.getChart().xUnits);
		control.getAxisSet().getYAxis(0).getTitle().setText(input.getChart().yUnits);

		// series
		ILineSeries lsUseful = (ILineSeries) control.getSeriesSet().createSeries(SeriesType.LINE, "line 1");
		lsUseful.setYSeries(input.getChart().dataUseful);
		lsUseful.setSymbolType(PlotSymbolType.DIAMOND);

		ILineSeries lsExtra = (ILineSeries) control.getSeriesSet().createSeries(SeriesType.LINE, "line 2");
		lsExtra.setYSeries(input.getChart().dataExtra);
		lsExtra.setSymbolType(PlotSymbolType.DIAMOND);

		if (input.getChart().dataStacks != null){
			ILineSeries lsStack = (ILineSeries) control.getSeriesSet().createSeries(SeriesType.LINE, "line 3");
			lsStack.setYSeries(input.getChart().dataStacks);
			lsStack.setSymbolType(PlotSymbolType.DIAMOND);
		}

		ILineSeries lsTotal = (ILineSeries) control.getSeriesSet().createSeries(SeriesType.LINE, "line 4");
		lsTotal.setYSeries(input.getChart().dataTotal);
		lsTotal.setSymbolType(PlotSymbolType.DIAMOND);

		// adjust axes
		control.getAxisSet().adjustRange();

		/*Display dsp = Display.getCurrent();
		GC gc = new GC(control);
		Image img = new Image(dsp, 640, 480);
		gc.copyArea(img, 0, 0);
		gc.dispose();
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {img.getImageData()};
		imageLoader.save("/home/rgrunber/Desktop/test.jpg",SWT.IMAGE_JPEG);*/

	}

	public Chart getControl() {
		return control;
	}
	
	@Override
	public void setFocus() {
		if (control != null) {
			control.setFocus();
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		control.dispose();
	}

}
