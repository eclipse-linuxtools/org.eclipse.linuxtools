/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The Valgrind Preference Page can be found by going to Windows -> Preferences
 * from the Eclipse top menu bar. This can hold all non-launch specific
 * configuration settings or user preferences.
 */
public class ValgrindPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * Boolean to allow user to disable Valgrind integration
	 * @since 0.8
	 */
	public static final String VALGRIND_ENABLE = "VALGRIND_ENABLE"; //$NON-NLS-1$
	public static final String VALGRIND_PATH = "VALGRIND_PATH"; //$NON-NLS-1$
	private Text binText;
	private Button browseButton;
	private IPreferenceStore store;
	private Button enableButton;

	@Override
	protected Control createContents(Composite parent) {
		Composite enableTop = new Composite(parent, SWT.NONE);
		enableTop.setLayout(new GridLayout());
		GridData enableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		enableTop.setLayoutData(enableData);
		
		enableButton = new Button(enableTop, SWT.CHECK);
		enableButton.setText(Messages.getString("ValgrindPreferencePage.Button_Enable_Valgrind")); //$NON-NLS-1$
		enableButton.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				checkValgrindEnablement();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Composite locationTop = new Composite(enableTop, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginTop = 0;
		locationTop.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = SWT.FILL;
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		locationTop.setLayoutData(data);

		// Path Label
		Label pathLabel = new Label(locationTop, SWT.NONE);
		pathLabel.setText(Messages
				.getString("ValgrindPreferencePage.Binary_path")); //$NON-NLS-1$

		// Path Text Field
		binText = new Text(locationTop, SWT.SINGLE | SWT.BORDER);
		GridData binTextData = new GridData();
		binTextData.horizontalAlignment = SWT.FILL;
		binTextData.grabExcessHorizontalSpace = true;
		binText.setLayoutData(binTextData);

		// Button
		browseButton = new Button(locationTop, SWT.PUSH);
		browseButton.setText(Messages
				.getString("ValgrindPreferencePage.Browse_button")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			public void widgetSelected(SelectionEvent e) {
				Shell shell = new Shell();
				FileDialog dialog = new FileDialog(shell);
				String path = dialog.open();
				if (path != null) {
					binText.setText(path);
				}
			}
		});

		loadPreferences();
		return parent;
	}

	private void checkValgrindEnablement() {
		boolean enabled = enableButton.getSelection();
		binText.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	// Loading preferences into controls
	private void loadPreferences() {
		enableButton.setSelection(store.getBoolean(VALGRIND_ENABLE));
		binText.setText(store.getString(VALGRIND_PATH));
		checkValgrindEnablement();
	}

	// Get the PreferenceStore for this Plugin
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return ValgrindPlugin.getDefault().getPreferenceStore();
	}

	// Initialization (Before Creating Widgets)
	public void init(IWorkbench workbench) {
		store = getPreferenceStore();
	}

	@Override
	protected void performDefaults() {
		store.setValue(VALGRIND_ENABLE, store.getDefaultBoolean(VALGRIND_ENABLE));
		enableButton.setSelection(store.getDefaultBoolean(VALGRIND_ENABLE));
		
		store.setValue(VALGRIND_PATH, store.getDefaultString(VALGRIND_PATH));
		binText.setText(store.getDefaultString(VALGRIND_PATH));
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if (isValid()) {
			store.setValue(VALGRIND_ENABLE, enableButton.getSelection());
			store.setValue(VALGRIND_PATH, binText.getText());
			ValgrindPlugin.getDefault().savePluginPreferences();
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public boolean isValid() {
		setErrorMessage(null);
		// Check the Binary Path is valid
		String path = binText.getText();
		File file = new File(path);
		// Can be more strict if necessary
		if (file.exists() && !file.isDirectory()) {
			return true;
		} else {
			setErrorMessage(Messages.getString("ValgrindPreferencePage.Error_invalid_message")); //$NON-NLS-1$
			return false;
		}
	}

}