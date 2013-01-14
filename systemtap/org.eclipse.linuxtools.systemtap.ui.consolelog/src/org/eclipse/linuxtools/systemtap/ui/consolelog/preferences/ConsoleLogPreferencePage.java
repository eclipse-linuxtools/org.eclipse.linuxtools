/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

public class ConsoleLogPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public ConsoleLogPreferencePage() {
		super(GRID);
		setPreferenceStore(ConsoleLogPlugin.getDefault().getPreferenceStore());
		setDescription("Preferences when accessing a remote server");
	}
	
	@Override
	public void createFieldEditors() {
		
		addField(new StringFieldEditor(ConsoleLogPreferenceConstants.HOST_NAME,
				"Host Name: ", getFieldEditorParent()));
		
		addField(new IntegerFieldEditor(ConsoleLogPreferenceConstants.PORT_NUMBER,
				"Port: ", getFieldEditorParent()));
		
		addField(new StringFieldEditor(ConsoleLogPreferenceConstants.SCP_USER,
				"User Name: ", getFieldEditorParent()));
		
		StringFieldEditor passwordField = new StringFieldEditor(
				ConsoleLogPreferenceConstants.SCP_PASSWORD, "Password: ",
                getFieldEditorParent());
        passwordField.getTextControl(getFieldEditorParent()).setEchoChar('*');
        addField(passwordField);

		addField(new BooleanFieldEditor(ConsoleLogPreferenceConstants.REMEMBER_SERVER,
				"Always connect to this host.", getFieldEditorParent()));
		
		addField(new IntegerFieldEditor(ConsoleLogPreferenceConstants.SAVE_LENGTH,
				"Seconds to Save Data: ", getFieldEditorParent()));

	}

	public void init(IWorkbench workbench) {}
}
