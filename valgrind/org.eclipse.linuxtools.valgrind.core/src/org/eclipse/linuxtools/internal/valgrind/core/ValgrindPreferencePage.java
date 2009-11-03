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
 * The Valgrind Preference Page can be found by going to Windows ->
 * Preferences from the Eclipse top menu bar. This can hold all
 * non-launch specific configuration settings or user preferences.
 */
public class ValgrindPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage{
	
	public static final String VALGRIND_PATH = "VALGRIND_PATH"; //$NON-NLS-1$
	private Text binText;
	private Button button;
	private IPreferenceStore store;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = SWT.FILL;
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		
		//Path Label
		Label pathLabel = new Label(composite,SWT.NONE);
		pathLabel.setText(Messages.getString("ValgrindPreferencePage.Binary_path")); //$NON-NLS-1$
		
		//Path Text Field
		binText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData binTextData = new GridData();
		binTextData.horizontalAlignment = SWT.FILL;
		binTextData.grabExcessHorizontalSpace = true;
		binText.setLayoutData(binTextData);
		
		//Button
		button = new Button(composite, SWT.PUSH);
		button.setText(Messages.getString("ValgrindPreferencePage.Browse_button")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			public void widgetSelected(SelectionEvent e) {
				Shell shell = new Shell();
				FileDialog dialog = new FileDialog(shell);
				String path = dialog.open();
				if (path != null){
					binText.setText(path);
				}
			}});
		
		loadPreferences();
		return parent;
	}
	
	//Loading preferences into controls
	private void loadPreferences() {
		binText.setText(store.getString(VALGRIND_PATH));
	}
	
	//Get the PreferenceStore for this Plugin
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return ValgrindPlugin.getDefault().getPreferenceStore();
	}

	//Initialization (Before Creating Widgets)
	public void init(IWorkbench workbench) {
		store = getPreferenceStore();
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		store.setValue(VALGRIND_PATH, store.getDefaultString(VALGRIND_PATH));
		binText.setText(store.getDefaultString(VALGRIND_PATH));
	}

	@Override
	public boolean performOk(){
		if (passesValidityChecks()){
			ValgrindPlugin.getDefault().savePluginPreferences();
			return true;
		}else{
			return false;
		}
	}

	private boolean passesValidityChecks() {
		
		//Check the Binary Path is valid
		File file = new File(binText.getText());
		//Can be more strict if necessary
		if (file.exists() && !file.isDirectory()){
			store.setValue(VALGRIND_PATH, binText.getText());
		}else{
			performDefaults();
			Shell shell = new Shell();
			MessageDialog
					.openError(shell, Messages.getString("ValgrindPreferencePage.Error_invalid_title"), //$NON-NLS-1$
							Messages.getString("ValgrindPreferencePage.Error_invalid_message")); //$NON-NLS-1$
			return false;
		}
		
		return true;
	}

}