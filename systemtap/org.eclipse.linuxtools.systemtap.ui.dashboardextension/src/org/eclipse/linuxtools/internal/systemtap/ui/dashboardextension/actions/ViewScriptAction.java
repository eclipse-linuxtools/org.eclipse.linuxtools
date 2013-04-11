/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.actions;

import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModule;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ModuleTreeNode;
import org.eclipse.linuxtools.systemtap.ui.ide.IDEPerspective;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.TempFileAction;

/**
 * A class that handles extracting the original script from the dashboard module.
 * It will then create a new active editor in the IDE perspective and display the
 * module in a temp file.  Users can then modify this script and use the modified
 * version as well as the original.
 * @author Ryan Morse
 */
public class ViewScriptAction extends Action implements IViewActionDelegate {
	/**
	 * This method sets what item the user has selected to view.
	 */
	@Override
	public void init(IViewPart view) {
		selectedItem = null;
	}

	/**
	 * This method will retreive the script from the selected module for the user
	 * to see.  It will then create a new active editor in the IDE perspective and
	 * display the module in a temp file.  Users can then modify this script and
	 * use the modified version as well as the original.
	 * @param act An action representing the click event used to start this method.
	 */
	@Override
	public void run(IAction act) {
		DashboardModule data = (DashboardModule)selectedItem.getData();

		try {
			IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(IDEPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());

			TempFileAction tfa = new TempFileAction();
			tfa.run();

			IEditorPart edit = p.getActiveEditor();

			if (edit instanceof SimpleEditor) {
				SimpleEditor editor = (SimpleEditor) edit;

				// Copy the file just to ensure the user has to save their own
				// copy if they want it
				FileInputStream fin;
				try {
					fin = new FileInputStream(data.script);
					StringBuilder sb = new StringBuilder();

					int c;
					while ((c = fin.read()) != -1) {
						sb.append((char) c);
					}

					fin.close();
					editor.insertText(sb.toString());
				} catch (IOException e) {
				}
			}
		} catch(WorkbenchException we) {}
	}

	/**
	 * This method will update the selected item when a new item is selected.
	 * @param action The action that fired this method.
	 * @param selection The newly selected item.
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection selected = (IStructuredSelection)selection;
			Object o = selected.getFirstElement();
			if(o instanceof ModuleTreeNode) {
				selectedItem = (ModuleTreeNode)o;
				o = selectedItem.getData();
				if(null != o) {
					setEnabled(true);
					return;
				}
			}
		}
		setEnabled(false);
	}

	private ModuleTreeNode selectedItem;
}
