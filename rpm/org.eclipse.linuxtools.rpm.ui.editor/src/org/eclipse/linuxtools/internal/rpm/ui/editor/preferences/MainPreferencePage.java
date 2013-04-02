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

import java.util.Locale;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;


/**
 * Specfile editor main preference page class.
 *
 */
public class MainPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private Combo combo;

	private int defaultItem;

	/**
	 * default constructor
	 */
	public MainPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	private FieldEditor changelogEntryFormatFieldEditor(Composite parent) {
		RadioGroupFieldEditor changelogEntryFormatRadioGroupEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT,
				Messages.MainPreferencePage_2, 1, new String[][] {
						{ Messages.MainPreferencePage_3, PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_VERSIONED },
						{ Messages.MainPreferencePage_4, PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_VERSIONED_WITH_SEPARATOR},
						{ Messages.MainPreferencePage_5, PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_UNVERSIONED }}, parent, true);
		return changelogEntryFormatRadioGroupEditor;
	}

	private void createLocalesCombo(Composite parent) {
		combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY |SWT.BEGINNING);
		// populate the combo with all ISO countries
		int selectedItem = 0;
		String lastLocale = getPreferenceStore().getString(PreferenceConstants.P_CHANGELOG_LOCAL);
		String[] countries = Locale.getISOCountries();
		for (int i = 0; i < countries.length; i++) {
				Locale currentLocale = new Locale(countries[i]);
				combo.add(countries[i] + " - " + currentLocale.getDisplayLanguage()); //$NON-NLS-1$
				// get index of the Locale store in the preferences
				if (countries[i].equals(lastLocale)) {
					selectedItem = i;
				}
				// get the index of the default Locale
				if (countries[i].equals(PreferenceConstants.DP_CHANGELOG_LOCAL)) {
					defaultItem = i;
				}
		}
		combo.select(selectedItem);
		// update preferences
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.getSource();
				getPreferenceStore().setValue(
						PreferenceConstants.P_CHANGELOG_LOCAL,
						combo.getText().split("-")[0].trim()); //$NON-NLS-1$
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		GridData data = new GridData();
		data.horizontalSpan = 2;
		final Composite parent = getFieldEditorParent();
		Link link= new Link(parent, SWT.NONE);
		link.setText(Messages.MainPreferencePage_0);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell() , e.text, null, null);
			}
		});
		addField(changelogEntryFormatFieldEditor(parent));
		Label labelLocal = new Label(parent, SWT.NONE);
		labelLocal.setText(Messages.MainPreferencePage_1);

		labelLocal.setLayoutData(data);
		createLocalesCombo(parent);
		addField(new BooleanFieldEditor(PreferenceConstants.P_SPACES_FOR_TABS,
				Messages.MainPreferencePage_6, parent));
		Composite numEditorComp = new Composite(parent, SWT.NONE);
		IntegerFieldEditor numEditor = new IntegerFieldEditor(
				PreferenceConstants.P_NBR_OF_SPACES_FOR_TAB,
				Messages.MainPreferencePage_7, numEditorComp, 1);
		addField(numEditor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		combo.select(defaultItem);
		getPreferenceStore().setValue(PreferenceConstants.P_CHANGELOG_LOCAL, PreferenceConstants.DP_CHANGELOG_LOCAL);
		getPreferenceStore().setValue(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT, PreferenceConstants.DP_CHANGELOG_ENTRY_FORMAT);
	}

}
