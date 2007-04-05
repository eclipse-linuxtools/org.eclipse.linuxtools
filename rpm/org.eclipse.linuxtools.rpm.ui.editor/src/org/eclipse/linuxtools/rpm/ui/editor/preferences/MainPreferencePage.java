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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
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

	/**
	 * default constructor
	 */
	public MainPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Main preference page for Specfile Plug-in editor");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		final Composite fieldEditorParent = new Composite(parent, SWT.LEFT);
		fieldEditorParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fieldEditorParent.setLayout(new GridLayout());
		Label label = new Label(fieldEditorParent, SWT.NONE);
		createLocalesCombo(fieldEditorParent);
		label.setText("Localize Changelog Formatter:");
		Link link= new Link(fieldEditorParent, SWT.NONE);
		// TODO: don't forgot to update Page1 to fit the new org.eclipse.linuxtools package name
		link.setText("Use the <a href=\"org.eclipse.linuxtools.changelog.core.Page1\">ChangeLog</a> preference to configure your name and e-mail address");
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(fieldEditorParent.getShell() , e.text, null, null); 
			}
		});
		return fieldEditorParent;
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
	}
	
}
