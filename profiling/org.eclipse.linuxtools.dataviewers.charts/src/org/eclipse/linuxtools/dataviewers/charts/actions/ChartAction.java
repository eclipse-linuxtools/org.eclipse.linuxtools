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
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.internal.dataviewers.charts.Activator;
import org.eclipse.linuxtools.internal.dataviewers.charts.Messages;
import org.eclipse.linuxtools.internal.dataviewers.charts.dialogs.ChartDialog;
import org.eclipse.linuxtools.internal.dataviewers.charts.dialogs.ChartDialog2;
import org.eclipse.linuxtools.internal.dataviewers.charts.view.ChartView;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;

/**
 * An action that open a chart dialog from an <code>AbstractSTViewer</code>.
 *
 * @see AbstractSTViewer
 * @deprecated Functionality to be moved to Eclipse.org SWTChart project.
 */
@Deprecated
public class ChartAction extends Action {

    /** The dialog */
    private final Dialog dialog;
    private final boolean useEclipseSwtChart;

    /**
     * The constructor.
     *
     * @param shell
     *            the shell used by the dialog
     * @param viewer
     *            the viewer inputed to the disalog
     */
    public ChartAction(Shell shell, AbstractSTViewer viewer) {
        super(Messages.ChartConstants_CREATE_CHART, Activator.getImageDescriptor("icons/chart_icon.png")); //$NON-NLS-1$
        dialog = new ChartDialog(shell, viewer);
        setEnabled(!viewer.getViewer().getSelection().isEmpty());
        viewer.getViewer().addSelectionChangedListener(event -> setEnabled(!event.getSelection().isEmpty()));
        this.useEclipseSwtChart=false;
    }
    
    /**
     * The constructor.
     *
     * @param shell
     *            the shell used by the dialog
     * @param viewer
     *            the viewer inputed to the disalog
     * @param useEclipseSwtchart Whether Eclipse.org SWTChart to be used or the old Sourceforge library
     * @since 6.1
     */
    public ChartAction(Shell shell, AbstractSTViewer viewer, boolean useEclipseSwtchart) {
        super(Messages.ChartConstants_CREATE_CHART, Activator.getImageDescriptor("icons/chart_icon.png")); //$NON-NLS-1$
        dialog = new ChartDialog2(shell, viewer);
        setEnabled(!viewer.getViewer().getSelection().isEmpty());
        viewer.getViewer().addSelectionChangedListener(event -> setEnabled(!event.getSelection().isEmpty()));
        this.useEclipseSwtChart = useEclipseSwtchart;
    }

    @Override
    public void run() {
        dialog.open();
        if (!useEclipseSwtChart) {
        	Chart chart = ((ChartDialog)dialog).getValue();
        	ChartView.createChartView(chart);
        } else {
        	ChartView.createChartView();
        }
    }
}
