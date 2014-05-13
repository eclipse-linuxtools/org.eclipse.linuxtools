/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and Action implementation
 *     Red Hat - conversion to Handler implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.dataviewers.charts.actions.SaveChartAction;
import org.eclipse.linuxtools.systemtap.graphing.ui.GraphDisplaySet;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.graphing.ui.views.GraphSelectorEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;

/**
 * This action is designed to allow for saving of the graph in the active window.
 * It will let the user select the location to save the image, and then save it as
 * a jpg image.
 * @author Ryan Morse
 * @since 3.0 Migrated from .ui.graphing package.
 */
public class SaveGraphImageHandler extends AbstractHandler {

    private SaveChartAction saveChartAction = new SaveChartAction();

    /**
     * This is the main method of the action.  It handles getting the active graph,
     * prompting the user for a location to save the image to, and then actually doing
     * the save.
     */
    @Override
    public Object execute(ExecutionEvent event) {
        saveChartAction.setChart(getActiveChart());
        saveChartAction.run();
        return null;
    }

    private GraphSelectorEditor getActiveGraphEditor() {
        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().
                getActivePage().getActiveEditor();
        return editor instanceof GraphSelectorEditor ? (GraphSelectorEditor) editor : null;
    }

    private AbstractChartBuilder getActiveGraph() {
        GraphSelectorEditor graphEditor = getActiveGraphEditor();
        if (graphEditor == null) {
            return null;
        }
        GraphDisplaySet gds = graphEditor.getActiveDisplaySet();
        return gds == null ? null : gds.getActiveGraph();
    }

    private Chart getActiveChart() {
        AbstractChartBuilder abs = getActiveGraph();
        return abs == null ? null : abs.getChart();
    }

}
