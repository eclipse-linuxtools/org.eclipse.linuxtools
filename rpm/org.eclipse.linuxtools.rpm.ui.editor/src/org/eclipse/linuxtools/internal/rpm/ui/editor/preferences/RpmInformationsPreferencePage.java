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

package org.eclipse.linuxtools.internal.rpm.ui.editor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RpmInformationsPreferencePage extends FieldEditorPreferencePage implements
IWorkbenchPreferencePage{

		/**
		 * Default constructor
		 */
		public RpmInformationsPreferencePage() {
			super(GRID);
			setDescription(Messages.RpmInformationsPreferencePage_0);
			setPreferenceStore(Activator.getDefault().getPreferenceStore());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
		 */
		@Override
		public void createFieldEditors() {
			addField(maxProposalsIntegerFieldEditor());
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_NAME, RpmTags.NAME, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_VERSION, RpmTags.VERSION, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_RELEASE, RpmTags.RELEASE, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_SUMMARY, RpmTags.SUMMARY, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_LICENSE, RpmTags.LICENSE, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_GROUP, RpmTags.GROUP, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_URL, RpmTags.URL, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_INSTALLTIME, Messages.RpmInformationsPreferencePage_1, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_DESCRIPTION, Messages.RpmInformationsPreferencePage_2, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_PACKAGER, RpmTags.PACKAGER, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_VENDOR, RpmTags.VENDOR, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_SIZE, Messages.RpmInformationsPreferencePage_3, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_BUILDTIME, Messages.RpmInformationsPreferencePage_4, getFieldEditorParent()));
			addField(new BooleanFieldEditor(PreferenceConstants.P_RPMINFO_SOURCERPM, Messages.RpmInformationsPreferencePage_5, getFieldEditorParent()));
		}

		private FieldEditor maxProposalsIntegerFieldEditor() {
			IntegerFieldEditor maxProposalsFieldEditor = new IntegerFieldEditor(
					PreferenceConstants.P_RPM_LIST_MAX_PROPOSALS,
					Messages.RpmInformationsPreferencePage_6, getFieldEditorParent());
			maxProposalsFieldEditor.setValidRange(1, 40);
			maxProposalsFieldEditor.setErrorMessage(Messages.RpmInformationsPreferencePage_7);
			return maxProposalsFieldEditor;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
		 */
		@Override
		public void init(IWorkbench workbench) {
		}

}
