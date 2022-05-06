/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.dataviewers.charts.view;

import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.dataviewers.charts.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

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

    /**
     * Create and open a new chart view <br/>
     * <br/>
     * <u><b>Note</b></u>: this method uses the UI thread to open the view and then it sets the input chart. The UI
     * thread execution is synchronized on internal Integer SEC_ID which is the secondary id of the chart view. Each new
     * chart view has a secondary id equal to SEC_ID++.
     *
     */
    public static void createChartView() {
        PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
		    try {
		        synchronized (lock) {
		            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		                    .getActivePage().showView(VIEW_ID, String.valueOf(SEC_ID++), IWorkbenchPage.VIEW_ACTIVATE);

		        }
		    } catch (PartInitException e) {
				Activator.getDefault().getLog().log(Status.error(e.getMessage(), e));
		    }
		});

    }

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
    }

    @Override
    public void setFocus() {
        if (parent != null && !parent.isDisposed()) {
            parent.setFocus();
        }
    }

    public Composite getParent() {
        return parent;
    }

    public static int getSecId() {
        return SEC_ID;
    }
}
