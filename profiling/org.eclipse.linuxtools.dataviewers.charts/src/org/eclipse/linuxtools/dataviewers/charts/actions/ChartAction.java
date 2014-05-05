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
package org.eclipse.linuxtools.dataviewers.charts.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.internal.dataviewers.charts.Activator;
import org.eclipse.linuxtools.internal.dataviewers.charts.Messages;
import org.eclipse.linuxtools.internal.dataviewers.charts.dialogs.ChartDialog;
import org.eclipse.linuxtools.internal.dataviewers.charts.view.ChartView;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;

/**
 * An action that open a chart dialog from an <code>AbstractSTViewer</code>.
 *
 * @see AbstractSTViewer
 */
public class ChartAction extends Action {

    /** The dialog */
    private final ChartDialog dialog;

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
        viewer.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setEnabled(!event.getSelection().isEmpty());
            }
        });
    }

    @Override
    public void run() {
        dialog.open();
        Chart chart = dialog.getValue();
        if (chart != null) {
            ChartView.createChartView(chart);

        }
    }
}
