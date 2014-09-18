/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.listeners;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.eclipse.linuxtools.systemtap.graphing.ui.charts.listeners.AbstractChartMouseMoveListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;

/**
 * @since 3.0
 */
public class ToolTipChartMouseMoveListener extends AbstractChartMouseMoveListener {
    private static final int TIP_OFFSET = 20;
    protected final Shell tipShell;
    protected final Text tipText;

    public ToolTipChartMouseMoveListener(Chart chart, Composite parent) {
        super(chart, parent);
        tipShell = new Shell(Display.getCurrent().getActiveShell(), SWT.TOOL | SWT.ON_TOP);
        tipText = new Text(tipShell, SWT.MULTI | SWT.BOLD);
        tipShell.setVisible(false);
    }

    @Override
    public void exit() {
        super.exit();
        tipShell.setVisible(false);
    }

    protected void setTextTip(String message) {
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        tipShell.setLocation(cursorLocation.x + TIP_OFFSET, cursorLocation.y + TIP_OFFSET);
        if (!message.equals(tipText.getText())) {
            tipText.setText(message);
            tipText.pack();
            tipShell.pack();
        }
        if (!tipShell.isVisible()) {
            tipShell.setVisible(true);
        }
    }

    /**
     * @return The contents of the mouse tooltip provided by this listener, if it is visible;
     * a default message otherwise.
     */
    @Override
    public String getMouseMessage() {
        RunnableFuture<String> f = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() {
                return tipText.isVisible() ? tipText.getText() : ""; //$NON-NLS-1$
            }
        });
        tipText.getDisplay().syncExec(f);
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }
}