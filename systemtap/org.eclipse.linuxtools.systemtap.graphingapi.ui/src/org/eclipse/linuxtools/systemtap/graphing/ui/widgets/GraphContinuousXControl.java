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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Slider;

/**
 * @since 3.0
 */
public class GraphContinuousXControl extends Composite {

	private static final int CLICK_INCREMENT = 10;
	private static final double TOLERANCE = 0.01;

	private AbstractChartBuilder builder;
	private Scale zoomScale;
	private Slider scrollBar;

	public GraphContinuousXControl(GraphComposite comp, int style) {
		super(comp, style);
		this.builder = comp.getCanvas();
		this.setLayout(new FormLayout());
		Font font = new Font(comp.getDisplay(), "Arial", 10, SWT.BOLD); //$NON-NLS-1$

		FormData thisData = new FormData();
		thisData.bottom = new FormAttachment(100, 0);
		thisData.left = new FormAttachment(builder, 0, SWT.LEFT);
		thisData.right = new FormAttachment(builder, 0, SWT.RIGHT);
		this.setLayoutData(thisData);

		Button zoomOutButton = new Button(this, SWT.CENTER);
		zoomOutButton.setText(Messages.GraphContinuousControl_ZoomOutLabel);
		zoomOutButton.setToolTipText(Messages.GraphContinuousXControl_ZoomOutTooltip);
		zoomOutButton.setFont(font);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		zoomOutButton.setLayoutData(data);
		zoomOutButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoomScale.setSelection(zoomScale.getSelection() - CLICK_INCREMENT);
				updateScale();
			}
		});

		Button zoomInButton = new Button(this, SWT.CENTER);
		zoomInButton.setText(Messages.GraphContinuousControl_ZoomInLabel);
		zoomInButton.setToolTipText(Messages.GraphContinuousXControl_ZoomInTooltip);
		zoomInButton.setFont(font);
		data = new FormData();
		data.right = new FormAttachment(100, 0);
		data.bottom = ((FormData) zoomOutButton.getLayoutData()).bottom;
		zoomInButton.setLayoutData(data);
		zoomInButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoomScale.setSelection(zoomScale.getSelection() + CLICK_INCREMENT);
				updateScale();
			}
		});

		zoomScale = new Scale(this,SWT.HORIZONTAL);
		zoomScale.setMinimum(0);
		zoomScale.setMaximum(99);
		zoomScale.setIncrement(1);
		zoomScale.setPageIncrement(CLICK_INCREMENT);
		zoomScale.setSelection(0); // Inverted: high on left, low on right
		zoomScale.setToolTipText(Messages.GraphContinuousXControl_ScaleMessage);
		data = new FormData();
		data.left = new FormAttachment(zoomOutButton, 2);
		data.bottom = ((FormData) zoomInButton.getLayoutData()).bottom;
		data.right = new FormAttachment(zoomInButton, -2);
		zoomScale.setLayoutData(data);

		scrollBar = new Slider(this,SWT.HORIZONTAL);
		scrollBar.setMinimum(0);
		scrollBar.setMaximum(101);
		scrollBar.setThumb(100);
		scrollBar.setIncrement(1);
		scrollBar.setPageIncrement(1);
		scrollBar.setSelection(100); // High on right, low on left
		scrollBar.setToolTipText(Messages.GraphContinuousXControl_ScrollMessage);
		data = new FormData();
		data.left = new FormAttachment(zoomOutButton, 0);
		data.bottom = new FormAttachment(zoomScale, 0);
		data.right = new FormAttachment(zoomInButton, 0);
		scrollBar.setLayoutData(data);

		zoomScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateScale();
			}
		});
		scrollBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateScroll();
			}
		});

		updateScale();
		updateScroll();
	}

	private void updateScale() {
		double newscale = 1.0 - zoomScale.getSelection() / 100.0;
		if (Math.abs(builder.getScale() - newscale) >= TOLERANCE) {
			builder.setScale(newscale);
			scrollBar.setThumb((int) (newscale * 100));
			scrollBar.setSelection((int) (builder.getScroll() * (101 - scrollBar.getThumb())));
		}
	}

	private void updateScroll() {
		double newscroll = scrollBar.getSelection() / (101.0 - scrollBar.getThumb());
		if (Math.abs(builder.getScroll() - newscroll) >= TOLERANCE) {
			builder.setScroll(newscroll);
		}
	}

}
