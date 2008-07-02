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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.Utils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * RPM package proposals preference page class.
 *
 */
public class RpmProposalsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/*
	 * default constructor
	 */
	public RpmProposalsPreferencePage() {
		super(FLAT);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		addField(rpmtoolsRadioGroupFieldEditor());	
		// FIXME: there is validations problem when a FileFieldEditor is used, so 
		// as a quick fix, StringFieldEditor is used.
		StringFieldEditor rpmListFieldEditor = new StringFieldEditor(PreferenceConstants.P_RPM_LIST_FILEPATH,
				"Path to packages list file:", getFieldEditorParent());
		addField(rpmListFieldEditor);
		addField(new BooleanFieldEditor(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD,"Automatically build the RPM packages proposals list", getFieldEditorParent()));
		addField(buildTimeListRateFieldEditor());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		Link link= new Link(parent, SWT.NONE);
		link.setText("<a href=\"org.eclipse.linuxtools.rpm.ui.editor.preferences.RpmInformationsPreferencePage\">Package Information</a> page helps to configure proposal descriptions");
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell() , e.text, null, null); 
			}
		});
		Composite fieldEditorComposite = (Composite) super
				.createContents(parent);
		return fieldEditorComposite;
	}
	
	private FieldEditor rpmtoolsRadioGroupFieldEditor() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		list.add(new String[] { "RPM (Red Hat Package Manager)",
								PreferenceConstants.DP_RPMTOOLS_RPM });
		/*
		 * Show only installed tools.
		 * Don't forgot to add sanity check in Utils.pluginSanityCheck().
		 */ 
		if (Utils.fileExist("/usr/bin/yum")) 
			list.add(new String[] { "YUM (Yellowdog Updater, Modified)",
					PreferenceConstants.DP_RPMTOOLS_YUM });
		if (Utils.fileExist("/usr/bin/urpmq")) 
			list.add(new String[] { "URPM (User RPM)",
					PreferenceConstants.DP_RPMTOOLS_URPM });

		String[][] radioItems = new String[list.size()][2];
		int pos = 0;
		for (String[] item: list) {
			radioItems[pos][0] = item[0];
			radioItems[pos][1] = item[1];
			pos++;
			
		}
	
		RadioGroupFieldEditor rpmToolsRadioGroupEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_CURRENT_RPMTOOLS,
				"RPM tools used to build the package list", 1, radioItems ,
				getFieldEditorParent(), true);
		return rpmToolsRadioGroupEditor;
	}
	
	private FieldEditor buildTimeListRateFieldEditor() { 
		RadioGroupFieldEditor buildListTimeRateRadioGroupEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_RPM_LIST_BUILD_PERIOD,
				"Proposals RPM list build rate", 1, new String[][] {
						{ "Each time that the workbench is open", "1" },
						{ "Once a week", "2" },
						{ "Once a month", "3" }}, getFieldEditorParent(), true);
		return buildListTimeRateRadioGroupEditor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}

}
