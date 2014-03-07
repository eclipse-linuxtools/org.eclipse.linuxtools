/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.ui.widgets;

import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.Range;

/**
 * @since 3.0
 */
public class GraphDiscreteXControl extends Composite implements IUpdateListener {

	private final static double ZOOM_AMOUNT = 2.0;

	private AbstractChartBuilder builder;
	private Button zoomInButton;
	private Button zoomOutButton;
	private Button allButton;
	private Button leftButton;
	private Button rightButton;
	private Button firstButton;
	private Button lastButton;

	public GraphDiscreteXControl(GraphComposite comp, int style) {
		super(comp, style);
		this.builder = comp.getCanvas();
		this.setLayout(new RowLayout());
		Font font = new Font(comp.getDisplay(), "Arial", 10, SWT.BOLD); //$NON-NLS-1$

		FormData thisData = new FormData();
		thisData.bottom = new FormAttachment(100, 0);
		thisData.left = new FormAttachment(builder, 0, SWT.LEFT);
		this.setLayoutData(thisData);

		firstButton = new Button(this, SWT.CENTER);
		firstButton.setText(Messages.GraphDiscreteXControl_First);
		firstButton.setFont(font);
		firstButton.setEnabled(false);
		firstButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				builder.setScroll(0);
				firstButton.setEnabled(false);
				leftButton.setEnabled(false);
				rightButton.setEnabled(true);
				lastButton.setEnabled(true);
			}
		});

		leftButton = new Button(this, SWT.CENTER);
		leftButton.setText(Messages.GraphDiscreteXControl_Left);
		leftButton.setFont(font);
		leftButton.setEnabled(false);
		leftButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stepScroll(-1);
			}
		});

		zoomInButton = new Button(this, SWT.CENTER);
		zoomInButton.setText(Messages.GraphDiscreteXControl_ZoomIn);
		zoomInButton.setFont(font);
		zoomInButton.setEnabled(false);
		zoomInButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				builder.setScale(builder.getScale() / ZOOM_AMOUNT);
			}
		});

		zoomOutButton = new Button(this, SWT.CENTER);
		zoomOutButton.setText(Messages.GraphDiscreteXControl_ZoomOut);
		zoomOutButton.setFont(font);
		zoomOutButton.setEnabled(false);
		zoomOutButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				builder.setScale(builder.getScale() * ZOOM_AMOUNT);
			}
		});

		allButton = new Button(this, SWT.CENTER);
		allButton.setText(Messages.GraphDiscreteXControl_All);
		allButton.setFont(font);
		allButton.setEnabled(false);
		allButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				builder.setScale(1.0);
			}
		});

		rightButton = new Button(this, SWT.CENTER);
		rightButton.setText(Messages.GraphDiscreteXControl_Right);
		rightButton.setFont(font);
		rightButton.setEnabled(false);
		rightButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stepScroll(1);
			}
		});

		lastButton = new Button(this, SWT.CENTER);
		lastButton.setText(Messages.GraphDiscreteXControl_Last);
		lastButton.setFont(font);
		lastButton.setEnabled(false);
		lastButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				builder.setScroll(1);
				firstButton.setEnabled(true);
				leftButton.setEnabled(true);
				rightButton.setEnabled(false);
				lastButton.setEnabled(false);
			}
		});
	}

	private int getNumItems() {
		ISeries[] series = builder.getChart().getSeriesSet().getSeries();
		return series.length > 0 ? series[0].getXSeries().length : 0;
	}

	private void stepScroll(int step) {
		// Note: scroll buttons are disabled when scale is 100%.
		builder.setScroll(builder.getScroll() + step / (double) (getNumItems()) / (1.0 - builder.getScale()));
	}

	@Override
	public void handleUpdateEvent() {
		IAxis xAxis = builder.getChart().getAxisSet().getXAxis(0);
		Range range = xAxis.getRange();
		zoomInButton.setEnabled(range.upper - range.lower > 0);

		boolean showingAll = builder.getScale() == 1;
		zoomOutButton.setEnabled(!showingAll);
		allButton.setEnabled(!showingAll);

		boolean hitLeft = showingAll || range.lower == 0;
		boolean hitRight = showingAll || range.upper == getNumItems() - 1;
		leftButton.setEnabled(!hitLeft);
		rightButton.setEnabled(!hitRight);
		firstButton.setEnabled(!hitLeft);
		lastButton.setEnabled(!hitRight);
	}

}
