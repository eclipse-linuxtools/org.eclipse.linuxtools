/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.preferences;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * RPM macro proposals and hover preference page class.
 *
 */
public class MacroProposalsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	ScopedPreferenceStore preferences;

	public MacroProposalsPreferencePage() {
		super(FLAT);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
	}

	protected void createFieldEditors() {
		ListEditor macroListEditor = new MacroListEditor(
				PreferenceConstants.P_MACRO_PROPOSALS_FILESPATH,
				"Macro Definitions", getFieldEditorParent());
		addField(macroListEditor);
		RadioGroupFieldEditor macroHoverListEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_MACRO_HOVER_CONTENT, "Mouse hover content",1,
				new String[][] {
					{"Show macro descriptions (e.g '%{prefix}/share')", 
					 PreferenceConstants.P_MACRO_HOVER_CONTENT_VIEWDESCRIPTION
					},
					{"Show macro contents (e.g '/usr/share')", 
					 PreferenceConstants.P_MACRO_HOVER_CONTENT_VIEWCONTENT
					}
				},
				getFieldEditorParent(),
				true);
		addField(macroHoverListEditor);
	}

	class MacroListEditor extends ListEditor {

		public MacroListEditor(String name, String labelText, Composite parent) {
			init(name, labelText);
			createControl(parent);
		}

		protected String createList(String[] items) {
			StringBuffer path = new StringBuffer("");
			for (int i = 0; i < items.length; i++) {
				path.append(items[i]);
				path.append(";");
			}
			return path.toString();
		}

		protected String getNewInputObject() {
			FileDialog dialog = new FileDialog(getShell());
			return dialog.open();
		}

		protected String[] parseString(String stringList) {
			StringTokenizer st = new StringTokenizer(stringList, ";\n\r");
			ArrayList v = new ArrayList();
			while (st.hasMoreElements()) {
				v.add(st.nextElement());
			}
			return (String[]) v.toArray(new String[v.size()]);
		}

	}

}
