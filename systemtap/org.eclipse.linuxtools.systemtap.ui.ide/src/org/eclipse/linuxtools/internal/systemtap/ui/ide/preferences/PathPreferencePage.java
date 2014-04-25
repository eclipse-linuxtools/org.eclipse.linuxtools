/*******************************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PathPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String REMOTE = "remote"; //$NON-NLS-1$
    public static final String LOCAL = "local"; //$NON-NLS-1$
    private LocalRemoteDirectoryEditor directoryEditor;

    public PathPreferencePage() {
        super(GRID);
        setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        String labels[][] = {{Localization.getString("PathPreferencePage.LocalMachine"), LOCAL}, //$NON-NLS-1$
                             {Localization.getString("PathPreferencePage.RemoteMachine"), REMOTE}}; //$NON-NLS-1$
        RadioGroupFieldEditor radioEditor = new RadioGroupFieldEditor(IDEPreferenceConstants.P_REMOTE_LOCAL_KERNEL_SOURCE,
                Localization.getString("PathPreferencePage.RemoteOrLocalRadio"), 1, labels, parent, true) { //$NON-NLS-1$
            @Override
            protected void fireValueChanged(String property,
                    Object oldValue,
                    Object newValue){
                super.fireValueChanged(property, oldValue, newValue);
                if (!oldValue.equals(newValue) && directoryEditor != null) {
                    if (newValue.equals(REMOTE))
                        directoryEditor.setRemote(true, getFieldEditorParent());
                    else
                        directoryEditor.setRemote(false, getFieldEditorParent());
                }
            }
        };
        addField(radioEditor);

        IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
        String remoteOrLocal = p.getString(IDEPreferenceConstants.P_REMOTE_LOCAL_KERNEL_SOURCE);
        boolean remote;
        if (remoteOrLocal.equals(REMOTE))
            remote = true;
        else
            remote = false;

        directoryEditor = new LocalRemoteDirectoryEditor(IDEPreferenceConstants.P_KERNEL_SOURCE,
                Localization.getString("PathPreferencePage.KernelSourceDirectory"), parent, remote); //$NON-NLS-1$
        addField(directoryEditor);

        addField(new ListEditor(IDEPreferenceConstants.P_EXCLUDED_KERNEL_SOURCE,
                Localization.getString("PathPreferencePage.ExcludedSourceFolders"), Localization.getString("PathPreferencePage.ExcludedDirectory"), "", new DirectoryValidator(), parent)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    }

    @Override
    public void init(IWorkbench workbench) {
    }
}
