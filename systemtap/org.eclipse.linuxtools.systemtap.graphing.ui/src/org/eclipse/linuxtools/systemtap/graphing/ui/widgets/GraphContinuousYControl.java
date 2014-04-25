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
public class GraphContinuousYControl extends Composite {

    private static final int CLICK_INCREMENT = 10;
    private static final double TOLERANCE = 0.01;

    private AbstractChartBuilder builder;
    private Scale zoomScale;
    private Slider scrollBar;

    public GraphContinuousYControl(GraphComposite comp, int style) {
        super(comp, style);
        this.builder = comp.getCanvas();
        this.setLayout(new FormLayout());
        Font font = new Font(comp.getDisplay(), "Arial", 10, SWT.BOLD); //$NON-NLS-1$

        FormData thisData = new FormData();
        thisData.left = new FormAttachment(0, 0);
        thisData.top = new FormAttachment(builder, 0, SWT.TOP);
        thisData.bottom = new FormAttachment(builder, 0, SWT.BOTTOM);
        this.setLayoutData(thisData);

        Button zoomOutButton = new Button(this, SWT.CENTER);
        zoomOutButton.setText(Messages.GraphContinuousControl_ZoomOutLabel);
        zoomOutButton.setToolTipText(Messages.GraphContinuousYControl_ZoomOutTooltip);
        zoomOutButton.setFont(font);
        FormData data = new FormData();
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        zoomOutButton.setLayoutData(data);
        zoomOutButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                zoomScale.setSelection(zoomScale.getSelection() + CLICK_INCREMENT);
                updateScaleY();
            }
        });

        Button zoomInButton = new Button(this, SWT.CENTER);
        zoomInButton.setText(Messages.GraphContinuousControl_ZoomInLabel);
        zoomInButton.setToolTipText(Messages.GraphContinuousYControl_ZoomInTooltip);
        zoomInButton.setFont(font);
        data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = ((FormData) zoomOutButton.getLayoutData()).left;
        zoomInButton.setLayoutData(data);
        zoomInButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                zoomScale.setSelection(zoomScale.getSelection() - CLICK_INCREMENT);
                updateScaleY();
            }
        });

        zoomScale = new Scale(this,SWT.VERTICAL);
        zoomScale.setMinimum(1);
        zoomScale.setMaximum(100);
        zoomScale.setIncrement(1);
        zoomScale.setPageIncrement(CLICK_INCREMENT);
        zoomScale.setSelection(100); // Low on top, high on bottom
        zoomScale.setToolTipText(Messages.GraphContinuousYControl_ScaleMessage);
        data = new FormData();
        data.top = new FormAttachment(zoomInButton, 2);
        data.left = ((FormData) zoomInButton.getLayoutData()).left;
        data.bottom = new FormAttachment(zoomOutButton, -2);
        zoomScale.setLayoutData(data);

        scrollBar = new Slider(this,SWT.VERTICAL);
        scrollBar.setMinimum(0);
        scrollBar.setMaximum(101);
        scrollBar.setThumb(100);
        scrollBar.setIncrement(1);
        scrollBar.setPageIncrement(1);
        scrollBar.setSelection(0); // Inverted: high on top, low on bottom
        scrollBar.setToolTipText(Messages.GraphContinuousYControl_ScrollMessage);
        data = new FormData();
        data.top = new FormAttachment(zoomInButton, 0);
        data.left = new FormAttachment(zoomScale, 0);
        data.bottom = new FormAttachment(zoomOutButton, 0);
        scrollBar.setLayoutData(data);

        zoomScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateScaleY();
            }
        });
        scrollBar.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateScrollY();
            }
        });

        updateScaleY();
        updateScrollY();
    }

    private void updateScaleY() {
        double newscale = zoomScale.getSelection() / 100.0;
        if (Math.abs(builder.getScaleY() - newscale) >= TOLERANCE) {
            builder.setScaleY(newscale);
            scrollBar.setThumb((int) (newscale * 100));
            scrollBar.setSelection((int) ((1 - builder.getScrollY()) * (101 - scrollBar.getThumb())));
        }
    }

    private void updateScrollY() {
        double newscroll = 1.0 - scrollBar.getSelection() / (101.0 - scrollBar.getThumb());
        if (Math.abs(builder.getScrollY() - newscroll) >= TOLERANCE) {
            builder.setScrollY(newscroll);
        }
    }

}
