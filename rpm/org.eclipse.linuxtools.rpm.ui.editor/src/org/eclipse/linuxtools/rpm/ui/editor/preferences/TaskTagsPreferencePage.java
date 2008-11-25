/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.preferences;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TaskTagsPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public TaskTagsPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		return super.createContents(parent);
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		addField(new TasksListEditor(PreferenceConstants.P_TASK_TAGS,
				"Strings indicating tasks in RPM specfile.",
				getFieldEditorParent()));
	}

	class TasksListEditor extends ListEditor {

		public TasksListEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		@Override
		protected String createList(String[] items) {
			StringBuilder itemsString = new StringBuilder();
			for (String item : items) {
				itemsString.append(item + ';');
			}
			return itemsString.toString();
		}

		@Override
		protected String getNewInputObject() {
			String result = null;
			// open an input dialog so that the user can enter a new task tag
			InputDialog inputDialog = new InputDialog(getShell(),
					"New Task Tag...", "Enter new Task Tag:", "", null);
			int returnCode = inputDialog.open();

			if (returnCode == Window.OK) {
				result = inputDialog.getValue();
			}

			return result;

		}

		@Override
		protected String[] parseString(String stringList) {
			String[] items = stringList.split(";");
			return items;
		}

	}

}
