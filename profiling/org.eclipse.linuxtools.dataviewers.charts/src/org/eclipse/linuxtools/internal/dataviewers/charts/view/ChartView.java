/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.dataviewers.charts.view;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.linuxtools.internal.dataviewers.charts.Activator;
import org.eclipse.linuxtools.internal.dataviewers.charts.actions.SaveChartAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.swtchart.Chart;

/**
 * The chart view.
 * 
 * <br/>
 * This view is multiple and all the created chart will be displayed in an instance of this view. Each one will have a
 * primary id equals to ChartView.VIEW_ID and an integer (increased by 1 at each new view) for the secondary id.
 * 
 * @author Marzia Maugeri <marzia.maugeri@st.com>
 * 
 */
public class ChartView extends ViewPart {

    /** The primary id of this view */
    public static final String VIEW_ID = "org.eclipse.linuxtools.dataviewers.charts.view"; //$NON-NLS-1$

    /** The current secondary id for these views */
    private static int SEC_ID = 0;
    private static final Object lock = new Object();

    private Composite parent;

    private SaveChartAction saveChartAction;

    /**
     * Create and open a new chart view <br/>
     * <br/>
     * <u><b>Note</b></u>: this method uses the UI thread to open the view and then it sets the input chart. The UI
     * thread execution is synchronized on internal Integer SEC_ID which is the secondary id of the chart view. Each new
     * chart view has a secondary id equal to SEC_ID++.
     * 
     * @param chart
     */
    public static void createChartView(final Chart chart) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        ChartView view = (ChartView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                .getActivePage().showView(VIEW_ID, String.valueOf(SEC_ID++), IWorkbenchPage.VIEW_ACTIVATE);
                        view.setChart(chart);

                    }
                } catch (PartInitException e) {
                    Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
                    Activator.getDefault().getLog().log(s);
                }
            }
        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        createActions(parent);
        IActionBars actionBars = getViewSite().getActionBars();
        initToolBar(actionBars.getToolBarManager());
    }

    protected void createActions(Composite parent) {
        saveChartAction = new SaveChartAction(getViewSite().getShell(), this);
    }

    protected void initToolBar(IToolBarManager manager) {
        manager.add(saveChartAction);
        manager.update(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        if (parent != null && !parent.isDisposed()) {
            parent.setFocus();
        }
    }

    /**
     * Set the chart in this view
     * 
     * @param chart
     */
    public void setChart(Chart chart) {
        saveChartAction.setChart(chart);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public Composite getParent() {
        return parent;
    }

    public static int getSecId() {
        return SEC_ID;
    }
}
