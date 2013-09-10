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

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
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

	private FieldEditor rpmtools;

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
		rpmtools = rpmtoolsRadioGroupFieldEditor();
		addField(rpmtools);
		// FIXME: there is validations problem when a FileFieldEditor is used, so
		// as a quick fix, StringFieldEditor is used.
		StringFieldEditor rpmListFieldEditor = new StringFieldEditor(PreferenceConstants.P_RPM_LIST_FILEPATH,
				Messages.RpmProposalsPreferencePage_0, getFieldEditorParent());
		addField(rpmListFieldEditor);
		addField(new BooleanFieldEditor(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD,Messages.RpmProposalsPreferencePage_1, getFieldEditorParent()));
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
		link.setText(Messages.RpmProposalsPreferencePage_2);
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
		list.add(new String[] { Messages.RpmProposalsPreferencePage_3,
								PreferenceConstants.DP_RPMTOOLS_RPM });
		/*
		 * Show only installed tools.
		 * Don't forgot to add sanity check in UiUtils.pluginSanityCheck().
		 */
		if (Utils.fileExist("/usr/bin/yum")) { //$NON-NLS-1$
			list.add(new String[] { Messages.RpmProposalsPreferencePage_4,
					PreferenceConstants.DP_RPMTOOLS_YUM });
		}
		if (Utils.fileExist("/usr/bin/urpmq")) { //$NON-NLS-1$
			list.add(new String[] { Messages.RpmProposalsPreferencePage_5,
					PreferenceConstants.DP_RPMTOOLS_URPM });
		}

		String[][] radioItems = new String[list.size()][2];
		int pos = 0;
		for (String[] item: list) {
			radioItems[pos][0] = item[0];
			radioItems[pos][1] = item[1];
			pos++;

		}

		RadioGroupFieldEditor rpmToolsRadioGroupEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_CURRENT_RPMTOOLS,
				Messages.RpmProposalsPreferencePage_6, 1, radioItems ,
				getFieldEditorParent(), true);
		return rpmToolsRadioGroupEditor;
	}

	private FieldEditor buildTimeListRateFieldEditor() {
		RadioGroupFieldEditor buildListTimeRateRadioGroupEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_RPM_LIST_BUILD_PERIOD,
				Messages.RpmProposalsPreferencePage_7, 1, new String[][] {
						{ Messages.RpmProposalsPreferencePage_8, "1" }, //$NON-NLS-1$
						{ Messages.RpmProposalsPreferencePage_10, "2" }, //$NON-NLS-1$
						{ Messages.RpmProposalsPreferencePage_12, "3" }}, getFieldEditorParent(), true); //$NON-NLS-1$
		return buildListTimeRateRadioGroupEditor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource().equals(rpmtools)) {
			if (!event.getOldValue().equals(event.getNewValue())) {
				String rpmpkgsFile = Activator.getDefault()
						.getPreferenceStore().getString(
								PreferenceConstants.P_RPM_LIST_FILEPATH);
				if (Utils.fileExist(rpmpkgsFile)) {
					new File(rpmpkgsFile).delete();
				}
				Activator.packagesList = null;
			}
		}
	}

}
