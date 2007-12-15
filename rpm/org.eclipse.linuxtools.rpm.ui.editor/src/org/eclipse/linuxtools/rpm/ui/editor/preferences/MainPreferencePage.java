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

import java.util.Locale;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	
	private Composite fieldEditorParent;

	/**
	 * default constructor
	 */
	public MainPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		//setDescription("Main preference page for Specfile Plug-in editor");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		fieldEditorParent = new Composite(parent, SWT.LEFT);
		fieldEditorParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fieldEditorParent.setLayout(new GridLayout());
        
        Link link= new Link(fieldEditorParent, SWT.NONE);
		link.setText("Use the <a href=\"org.eclipse.linuxtools.changelog.core.Page1\">ChangeLog</a> preferences to configure your name and e-mail address.");
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(fieldEditorParent.getShell() , e.text, null, null); 
			}
		});
		
		createFieldEditors();
        
		Label labelLocal = new Label(fieldEditorParent, SWT.NONE);
		labelLocal.setText("Changelog entries Locale:");
		createLocalesCombo(fieldEditorParent);

        initialize();
        checkState();
		
		return fieldEditorParent;
	}
	
	private FieldEditor changelogEntryFormatFieldEditor(Composite parent) { 
		RadioGroupFieldEditor changelogEntryFormatRadioGroupEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT,
				"Changelog entries format:", 1, new String[][] {
						{ "Versioned entry (e.g. * Date Name <Mail> 1.1-1)", PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_VERSIONED },
						{ "Versioned entry with separator (e.g. * Date Name <Mail> - 1.1-1)", PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_VERSIONED_WITH_SEPARATOR},
						{ "Unversioned entry (e.g. * Date Name <Mail>)", PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_UNVERSIONED }}, parent, true);
		return changelogEntryFormatRadioGroupEditor;
	}		
	
	private void createLocalesCombo(Composite parent) {
		combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		// populate the combo with all ISO countries
		int selectedItem = 0;
		String lastLocale = getPreferenceStore().getString(PreferenceConstants.P_CHANGELOG_LOCAL);
		String[] countries = Locale.getISOCountries();
		for (int i = 0; i < countries.length; i++) {
				Locale currentLocale = new Locale(countries[i]);
				combo.add(countries[i] + " - " + currentLocale.getDisplayLanguage());
				// get index of the Locale store in the preferences
				if (countries[i].equals(lastLocale))
					selectedItem = i;
				// get the index of the default Locale
				if (countries[i].equals(PreferenceConstants.DP_CHANGELOG_LOCAL))
					defaultItem = i;
		}
		combo.select(selectedItem);
		// update preferences
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.getSource();
				getPreferenceStore().setValue(
						PreferenceConstants.P_CHANGELOG_LOCAL,
						combo.getText().split("-")[0].trim());
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	public void createFieldEditors() {
		addField(changelogEntryFormatFieldEditor(fieldEditorParent));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		combo.select(defaultItem);
		getPreferenceStore().setValue(PreferenceConstants.P_CHANGELOG_LOCAL, PreferenceConstants.DP_CHANGELOG_LOCAL);
		getPreferenceStore().setValue(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT, PreferenceConstants.DP_CHANGELOG_ENTRY_FORMAT);
	}
	
}
