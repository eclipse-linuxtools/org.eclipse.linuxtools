/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

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
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorEditor;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * This <code>Action</code> imports data from an external file to populate
 * a {@link GraphSelectorEditor}.
 * into an external file, which can be imported back in later.
 * @author Andrew Ferrazzutti
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

		IDataSet dataset = null;
		File file = new File(path);
		try (InputStreamReader fr = new InputStreamReader(new FileInputStream(file), Charset.defaultCharset());
				BufferedReader br = new BufferedReader(fr)) {
			String id = br.readLine();
			String[] titles = br.readLine().split(", "); //$NON-NLS-1$

			if (id == null && titles == null) {
				throw new IOException();
			} else if (id.equals(RowDataSet.ID)) {
				dataset = new RowDataSet(titles);
			} else if (id.equals(TableDataSet.ID)) {
				dataset = new TableDataSet(titles);
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
			ExceptionErrorDialog.openError(Messages.RunScriptChartAction_couldNotSwitchToGraphicPerspective, we);
		}

		return null;
	}

	@Override
	public boolean isEnabled() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
				getPerspective().getId().equals(IDEPerspective.ID);
	}
}
