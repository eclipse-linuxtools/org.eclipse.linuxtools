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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RpmInformationsPreferencePage extends FieldEditorPreferencePage implements
IWorkbenchPreferencePage{

		/**
		 * Default constructor
		 */
		public RpmInformationsPreferencePage() {
			super(GRID);
			setDescription("RPM tags used as proposal descriptions");
			setPreferenceStore(Activator.getDefault().getPreferenceStore());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
		 */
		public void createFieldEditors() {
			addField(maxProposalsIntegerFieldEditor());
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_NAME, "Name", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_VERSION, "Version", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_RELEASE, "Release", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_SUMMARY, "Summary", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_LICENSE, "License", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_GROUP, "Group", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_URL, "URL", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_INSTALLTIME, "Installation Date", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_DESCRIPTION, "Description", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_PACKAGER, "Packager", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_VENDOR, "Vendor", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_SIZE, "Size", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_BUILDTIME, "Build Date", getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_SOURCERPM, "Source Rpm", getFieldEditorParent()));
		}
		
		private FieldEditor maxProposalsIntegerFieldEditor() {
			IntegerFieldEditor maxProposalsFieldEditor = new IntegerFieldEditor(
					PreferenceConstants.P_RPM_LIST_MAX_PROPOSALS,
					"This information is only shown when the number of proposals is less than", getFieldEditorParent());
			maxProposalsFieldEditor.setValidRange(1, 40);
			maxProposalsFieldEditor.setErrorMessage("Proposal limit for RPM information must be an integer between 1 and 40");
			return maxProposalsFieldEditor;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
		 */
		public void init(IWorkbench workbench) {
		}
		
}
