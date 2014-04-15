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
package org.eclipse.linuxtools.internal.valgrind.massif.charting;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.Range;

public class ChartEditor extends EditorPart {
	private Chart control;

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
		final ChartEditorInput input = (ChartEditorInput) getEditorInput();
		final HeapChart heapChart = input.getChart();
		control = new Chart(parent, SWT.FILL);
		heapChart.setChartControl(control);

		final Color LIGHTYELLOW = new Color(Display.getDefault(), 255, 255, 225);
		final Color WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		final Color BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		final Color RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		final Color ORANGE = new Color(Display.getDefault(), 255, 165, 0);
		final Color GREEN = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
		final Color DARK_BLUE = new Color(Display.getDefault(), 64, 128, 128);
		final int TICK_GAP = 40;

		control.setBackground(WHITE);
		control.setBackgroundInPlotArea(LIGHTYELLOW);

		FontData fd = JFaceResources.getDialogFont().getFontData()[0];
		fd.setStyle(SWT.BOLD);

		Font font = new Font(Display.getDefault(), fd);
		fd.setHeight(fd.getHeight() + 2);
		Font titleFont = new Font(Display.getDefault(), fd);

		ITitle title = control.getTitle();
		title.setFont(titleFont);
		title.setForeground(BLACK);
		title.setText(heapChart.title);

		IAxis xAxis = control.getAxisSet().getXAxis(0);
		xAxis.getGrid().setStyle(LineStyle.NONE);
		xAxis.getTick().setForeground(BLACK);
		ITitle xTitle = xAxis.getTitle();
		xTitle.setFont(font);
		xTitle.setForeground(BLACK);
		xTitle.setText(heapChart.xUnits);

		IAxis yAxis = control.getAxisSet().getYAxis(0);
		yAxis.getGrid().setStyle(LineStyle.SOLID);
		yAxis.getTick().setForeground(BLACK);
		yAxis.getTick().setTickMarkStepHint(TICK_GAP);
		ITitle yTitle = yAxis.getTitle();
		yTitle.setFont(font);
		yTitle.setText(heapChart.yUnits);
		yTitle.setForeground(BLACK);

		control.getLegend().setPosition(SWT.BOTTOM);

		// data
		final ILineSeries lsUseful = (ILineSeries) control.getSeriesSet().
				createSeries(SeriesType.LINE, Messages.getString("HeapChart.Useful_Heap")); //$NON-NLS-1$;
		lsUseful.setXSeries(heapChart.time);
		lsUseful.setYSeries(heapChart.dataUseful);
		lsUseful.setSymbolType(PlotSymbolType.DIAMOND);
		lsUseful.setSymbolColor(RED);
		lsUseful.setLineColor(RED);

		final ILineSeries lsExtra = (ILineSeries) control.getSeriesSet().
				createSeries(SeriesType.LINE, Messages.getString("HeapChart.Extra_Heap")); //$NON-NLS-1$;
		lsExtra.setXSeries(heapChart.time);
		lsExtra.setYSeries(heapChart.dataExtra);
		lsExtra.setSymbolType(PlotSymbolType.DIAMOND);
		lsExtra.setSymbolColor(ORANGE);
		lsExtra.setLineColor(ORANGE);

		if (heapChart.dataStacks != null){
			final ILineSeries lsStack = (ILineSeries) control.getSeriesSet().
					createSeries(SeriesType.LINE, Messages.getString("HeapChart.Stacks")); //$NON-NLS-1$;
			lsStack.setXSeries(heapChart.time);
			lsStack.setYSeries(heapChart.dataStacks);
			lsStack.setSymbolType(PlotSymbolType.DIAMOND);
			lsStack.setSymbolColor(DARK_BLUE);
			lsStack.setLineColor(DARK_BLUE);
		}

		final ILineSeries lsTotal = (ILineSeries) control.getSeriesSet().
				createSeries(SeriesType.LINE, Messages.getString("HeapChart.Total_Heap")); //$NON-NLS-1$;
		lsTotal.setXSeries(heapChart.time);
		lsTotal.setYSeries(heapChart.dataTotal);
		lsTotal.setSymbolType(PlotSymbolType.DIAMOND);
		lsTotal.setSymbolColor(GREEN);
		lsTotal.setLineColor(GREEN);

		// adjust axes
		control.getAxisSet().adjustRange();

		IAxisSet axisSet = control.getAxisSet();
		Range xRange = axisSet.getXAxis(0).getRange();
		Range yRange = axisSet.getYAxis(0).getRange();

		double xExtra = 0.05 * (xRange.upper - xRange.lower);
		double yExtra = 0.05 * (yRange.upper - yRange.lower);

		axisSet.getXAxis(0).setRange(new Range(xRange.lower, xRange.upper + xExtra));
		axisSet.getYAxis(0).setRange(new Range(yRange.lower, yRange.upper + yExtra));

		// listeners
		control.getPlotArea().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				showView();
				TableViewer viewer = input.getView().getTableViewer();
				input.getView().setTopControl(viewer.getControl());

				Point p = new Point(e.x, e.y);

				int closest = 0;
				double d1, d2, d3, currMin;
				double globalMin = Double.MAX_VALUE;
				for (int i = 0; i < heapChart.time.length; i++){
					// get distance from click event to data points for the given index
					d1 = distance(lsUseful.getPixelCoordinates(i), p);
					d2 = distance(lsExtra.getPixelCoordinates(i), p);
					d3 = distance(lsTotal.getPixelCoordinates(i), p);
					// find the closest data point to the click event
					currMin = Math.min(Math.min(d1, d2), d3);
					if (currMin < globalMin){
						closest = i;
						globalMin = currMin;
					}
				}

				MassifSnapshot snapshot = (MassifSnapshot) viewer.getElementAt(closest);
				viewer.setSelection(new StructuredSelection(snapshot), true);

				if (e.count == 2 && snapshot.isDetailed()) {
					ChartLocationsDialog dialog = new ChartLocationsDialog(Display.getCurrent().getActiveShell());
					dialog.setInput(snapshot);

					if (dialog.open() == Window.OK) {
						dialog.openEditorForResult();
					}
				}

			}
		});

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


    /**
     * Shows the Valgrind view in the active page and gives it focus.
     */
    private void showView() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    IWorkbenchPage activePage = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getActivePage();
                    activePage.showView(IValgrindToolView.VIEW_ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Calculate Euclidean distance between two points (R2).
     */
    private double distance (Point p1, Point p2) {
    	return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

}
