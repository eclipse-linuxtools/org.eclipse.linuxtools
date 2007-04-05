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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
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
	public void createFieldEditors() {
		addField(rpmtoolsRadioGroupFieldEditor());	
		// FIXME: there is validations problem when a FileFieldEditor is used, so 
		// as a quick fix, StringFieldEditor is used.
		StringFieldEditor rpmListFieldEditor = new StringFieldEditor(PreferenceConstants.P_RPM_LIST_FILEPATH,
				"Path to packages list file:", getFieldEditorParent());
		addField(rpmListFieldEditor);
		addField(new BooleanFieldEditor(PreferenceConstants.P_RPM_LIST_HIDE_PROPOSALS_WARNING,"Hide warning about RPM proposals", getFieldEditorParent()));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(final Composite parent) {
		Link link= new Link(parent, SWT.NONE);
		link.setText("<a href=\"org.eclipse.linuxtools.rpm.ui.editor.preferences.RpmInformationsPreferencePage\">Package Information</a> page helps to configure proposal descriptions");
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell() , e.text, null, null); 
			}
		});
		Composite fieldEditorComposite = (Composite) super
				.createContents(parent);
		createBuildListButton(fieldEditorComposite);
		return fieldEditorComposite;
	}
	
	private FieldEditor rpmtoolsRadioGroupFieldEditor() {
		RadioGroupFieldEditor rpmToolsRadioGroupEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_CURRENT_RPMTOOLS,
				"RPM tools used to build the package list", 1, new String[][] {
						{ "RPM (Red Hat Package Manager)",
								PreferenceConstants.DP_RPMTOOLS_RPM },
						{ "YUM (Yellowdog Updater, Modified)",
								PreferenceConstants.DP_RPMTOOLS_YUM } },
				getFieldEditorParent(), true);
		return rpmToolsRadioGroupEditor;
	}
	
	public void createBuildListButton(Composite parent) {
		Button builRpmProposalsButton = new Button(parent,
				SWT.PUSH);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.END;
		data.verticalIndent = 10;
		data.grabExcessHorizontalSpace = true;
		builRpmProposalsButton.setLayoutData(data);
		builRpmProposalsButton.setText("Build proposals now ...");
		builRpmProposalsButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						performApply();
						String rpmListCmd = getPreferenceStore().getString(PreferenceConstants.P_CURRENT_RPMTOOLS);
						String rpmListFilepath = getPreferenceStore().getString(PreferenceConstants.P_RPM_LIST_FILEPATH);
						try {
							String[] cmd = new String[] {"/bin/sh", "-c", rpmListCmd};
							monitor.beginTask("Get RPM proposals list...", IProgressMonitor.UNKNOWN);
							Process child = Runtime.getRuntime().exec(cmd);
							InputStream in = child.getInputStream();
							BufferedWriter out = new BufferedWriter(new FileWriter(rpmListFilepath, false));
							BufferedReader reader = new BufferedReader(new InputStreamReader(in));
							monitor.setTaskName("Write RPM proposals list into "
							+ rpmListFilepath + " file ...");
							String line;
							while ((line = reader.readLine()) != null) {
								monitor.subTask("Add package: " + line);
								out.write(line + "\n");
					        }
							in.close();
							out.close();
							// validate the page and hide the error message.
							setValid(true);
							setErrorMessage(null);
						} catch (IOException e) {
							setErrorMessage("Error when building the RPM packages list:\n" + e.getMessage());
						} finally {
							monitor.done();
						}
					}
				};
				try {
					ProgressMonitorDialog progressMonitor = new ProgressMonitorDialog(getShell());
					//FIXME: If we will use a non forked thread, we may implement something to access this 
					// pref. page outside the main thread. Seems to be easy for Editors but not for Preference pages.
					progressMonitor.run(false, true, runnable);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}

}
