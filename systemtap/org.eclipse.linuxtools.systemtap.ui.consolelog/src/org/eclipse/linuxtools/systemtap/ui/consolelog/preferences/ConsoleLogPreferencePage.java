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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.preferences.Messages;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ConsoleLogPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public ConsoleLogPreferencePage() {
        super(GRID);
        setPreferenceStore(ConsoleLogPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.ConsoleLogPreferencePage_PreferencesTitle);
    }

    @Override
    public void createFieldEditors() {

        addField(new StringFieldEditor(ConsoleLogPreferenceConstants.HOST_NAME,
                Messages.ConsoleLogPreferencePage_Host, getFieldEditorParent()));

        addField(new IntegerFieldEditor(ConsoleLogPreferenceConstants.PORT_NUMBER,
                Messages.ConsoleLogPreferencePage_Port, getFieldEditorParent()));

        addField(new StringFieldEditor(ConsoleLogPreferenceConstants.SCP_USER,
                Messages.ConsoleLogPreferencePage_User, getFieldEditorParent()));

        StringFieldEditor passwordField = new StringFieldEditor(
                ConsoleLogPreferenceConstants.SCP_PASSWORD, Messages.ConsoleLogPreferencePage_Password,
                getFieldEditorParent());
        passwordField.getTextControl(getFieldEditorParent()).setEchoChar('*');
        addField(passwordField);
    }

    @Override
    public void init(IWorkbench workbench) {}
}
