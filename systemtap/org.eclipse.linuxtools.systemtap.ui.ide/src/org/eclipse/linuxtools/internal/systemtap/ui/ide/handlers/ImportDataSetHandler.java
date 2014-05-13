/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPerspective;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.FilteredRowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.table.FilteredTableDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.graphing.ui.views.GraphSelectorEditor;
import org.eclipse.linuxtools.systemtap.graphing.ui.views.GraphSelectorEditorInput;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * This <code>Action</code> imports data from an external file to populate
 * a {@link GraphSelectorEditor}.
 * into an external file, which can be imported back in later.
 */
public class ImportDataSetHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[]{Messages.DataSetFileExtension});
        dialog.setText(Messages.ImportDataSetAction_DialogTitle);
        String path = dialog.open();
        if (path == null) {
            return null;
        }

        IFilteredDataSet dataset = null;
        File file = new File(path);
        try (InputStreamReader fr = new InputStreamReader(new FileInputStream(file), Charset.defaultCharset());
                BufferedReader br = new BufferedReader(fr)) {
            String id = br.readLine();
            String[] titles = br.readLine().split(", "); //$NON-NLS-1$

            if (id == null && titles == null) {
                throw new IOException();
            } else if (id.equals(RowDataSet.ID)) {
                dataset = new FilteredRowDataSet(titles);
            } else if (id.equals(TableDataSet.ID)) {
                dataset = new FilteredTableDataSet(titles);
            } else {
                throw new IOException();
            }
            dataset.readFromFile(file);

            String title = path.substring(path.lastIndexOf('/')+1);
            IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(IDEPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            GraphSelectorEditor ivp = (GraphSelectorEditor)p.openEditor(new GraphSelectorEditorInput(title), GraphSelectorEditor.ID);
            ivp.createScriptSets(path, Arrays.asList(title), Arrays.asList(dataset));
        } catch (FileNotFoundException fnfe) {
            ExceptionErrorDialog.openError(Messages.ImportDataSetAction_FileNotFound, fnfe);
        } catch (IOException ioe) {
            ExceptionErrorDialog.openError(Messages.ImportDataSetAction_FileInvalid, ioe);
        } catch (WorkbenchException we) {
            ExceptionErrorDialog.openError(Messages.RunScriptChartHandler_couldNotSwitchToGraphicPerspective, we);
        }

        return null;
    }

}
