/*******************************************************************************
 * Copyright (c) 2006, 2010 Phil Muldoon <pkmuldoon@picobot.org> and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *    Kyu Lee <klee@redhat.com>          - editor support
 *    Alexander Kurtakov (Red Hat)       - remove parts notneeded
 *    Red Hat Inc.                       - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author pmuldoon (Phil Muldoon)
 */

/**
 * This class implements a sample preference page that is added to the
 * preference dialog based on the registration.
 */
public class ChangeLogPreferencesPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Text emailField;

	private Text nameField;

	private List formatterList;

	private List editorList;

	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);

		// GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);

		// GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	private Text createTextField(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData(data);
		return text;
	}

	private List createListBox(Composite parent, int sizeHint) {
		List list = new List(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		data.heightHint = list.getItemHeight() * sizeHint;
		list.setLayoutData(data);

		return list;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return ChangelogPlugin.getDefault().getPreferenceStore();
	}

	private void initializeDefaults() {
		IPreferenceStore store = getPreferenceStore();
		emailField.setText(store
				.getDefaultString("IChangeLogConstants.AUTHOR_EMAIL")); //$NON-NLS-1$
		nameField.setText(store
				.getDefaultString("IChangeLogConstants.AUTHOR_NAME")); //$NON-NLS-1$
		setDefaultFormatter(store);
		setDefaultEditor(store);
		storeValues();
	}

	private void setDefaultFormatter(IPreferenceStore store) {

		String defaultFormatter = store
				.getDefaultString("IChangeLogConstants.DEFAULT_FORMATTER"); //$NON-NLS-1$
		for (int i = 0; i < formatterList.getItemCount(); i++) {
			if (formatterList.getItem(i).equals(defaultFormatter)) {
				formatterList.setSelection(i);
				return;
			}
		}
	}

	private void setDefaultEditor(IPreferenceStore store) {

		String defaultEditor = store
				.getDefaultString("IChangeLogConstants.DEFAULT_EDITOR"); //$NON-NLS-1$
		for (int i = 0; i < editorList.getItemCount(); i++) {
			if (editorList.getItem(i).equals(defaultEditor)) {
				editorList.setSelection(i);
				return;
			}
		}
	}

	private void populateFormatList(IPreferenceStore store) {
		IExtensionPoint parserExtensions = Platform
				.getExtensionRegistry()
				.getExtensionPoint(
						"org.eclipse.linuxtools.changelog.core", "formatterContribution"); //$NON-NLS-1$ //$NON-NLS-2$
		if (parserExtensions != null) {
			IConfigurationElement[] elements = parserExtensions
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals("formatter")) {//$NON-NLS-1$

					String fname = element.getAttribute("name"); //$NON-NLS-1$\
					// only add formatters for external files, not in-file formatters.
					if (element.getAttribute("inFile").equalsIgnoreCase("false")) {
						formatterList.add(fname);
					}

					if (fname
							.equals(store
									.getString("IChangeLogConstants.DEFAULT_FORMATTER"))) { //$NON-NLS-1$
						formatterList
								.setSelection(formatterList.getItemCount() - 1);
					}
				}
			}

		}
	}

	private void populateEditorList(IPreferenceStore store) {
		IExtensionPoint editorExtensions = Platform
				.getExtensionRegistry()
				.getExtensionPoint(
						"org.eclipse.linuxtools.changelog.core", "editorContribution"); //$NON-NLS-1$ //$NON-NLS-2$
		if (editorExtensions != null) {
			IConfigurationElement[] elements = editorExtensions
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals("editor")) {//$NON-NLS-1$
					String fname = element.getAttribute("name"); //$NON-NLS-1$
					editorList.add(fname);
					if (fname.equals(store
							.getString("IChangeLogConstants.DEFAULT_EDITOR"))) {//$NON-NLS-1$
						editorList.setSelection(editorList.getItemCount() - 1);
					}
				}
			}

		}
	}

	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();

		emailField.setText(store.getString("IChangeLogConstants.AUTHOR_EMAIL")); //$NON-NLS-1$
		nameField.setText(store.getString("IChangeLogConstants.AUTHOR_NAME")); //$NON-NLS-1$
		populateFormatList(store);
		populateEditorList(store);
	}

	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();

		store.setValue("IChangeLogConstants.AUTHOR_NAME", nameField.getText()); //$NON-NLS-1$
		store
				.setValue(
						"IChangeLogConstants.AUTHOR_EMAIL", emailField.getText()); //$NON-NLS-1$
		String[] selection = formatterList.getSelection();
		if (selection != null && selection.length > 0) {
			store.setValue("IChangeLogConstants.DEFAULT_FORMATTER", selection[0]); //$NON-NLS-1$
		}
		String[] selection2 = editorList.getSelection();
		if (selection2 != null && selection2.length > 0) {
			store.setValue("IChangeLogConstants.DEFAULT_EDITOR", selection2[0]); //$NON-NLS-1$
		}

	}

	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();

	}

	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean performOk() {
		storeValues();
		ChangelogPlugin.getDefault().savePluginPreferences();
		return true;
	}

	@Override
	protected Control createContents(Composite parent) {
		// composite_textField << parent
		Composite composite_textField = createComposite(parent, 2);
		createLabel(composite_textField, Messages
				.getString("ChangeLogPreferencesPage.AuthorName")); //$NON-NLS-1$
		nameField = createTextField(composite_textField);

		// composite_textField << parent

		createLabel(composite_textField, Messages
				.getString("ChangeLogPreferencesPage.AuthorEmail")); //$NON-NLS-1$
		emailField = createTextField(composite_textField);

		createLabel(composite_textField, Messages
				.getString("ChangeLogPreferencesPage.Formatters")); //$NON-NLS-1$
		formatterList = createListBox(composite_textField, 3);

		createLabel(composite_textField, Messages
				.getString("ChangeLogPreferencesPage.Editors")); //$NON-NLS-1$
		editorList = createListBox(composite_textField, 3);

		initializeValues();

		return new Composite(parent, SWT.NULL);
	}
}