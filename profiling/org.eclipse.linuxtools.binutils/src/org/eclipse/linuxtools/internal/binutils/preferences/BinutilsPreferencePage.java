/*******************************************************************************
 * Copyright (c) 2013 Kalray.eu
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.binutils.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.binutils.Activator;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class BinutilsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PREFKEY_ADDR2LINE_CMD = "PREFKEY_ADDR2LINE_CMD"; //$NON-NLS-1$
    public static final String PREFKEY_ADDR2LINE_ARGS = "PREFKEY_ADDR2LINE_ARGS"; //$NON-NLS-1$

    public static final String PREFKEY_CPPFILT_CMD = "PREFKEY_CPPFILT_CMD"; //$NON-NLS-1$
    public static final String PREFKEY_CPPFILT_ARGS = "PREFKEY_CPPFILT_ARGS"; //$NON-NLS-1$

    public static final String PREFKEY_NM_CMD = "PREFKEY_NM_CMD"; //$NON-NLS-1$
    public static final String PREFKEY_NM_ARGS = "PREFKEY_NM_ARGS"; //$NON-NLS-1$

    private StringFieldEditor faddr2line;
    private StringFieldEditor faddr2lineArgs;
    private StringFieldEditor fcppfilt;
    private StringFieldEditor fcppfiltArgs;
    private StringFieldEditor fnm;
    private StringFieldEditor fnmArgs;

    public BinutilsPreferencePage() {
        super(Messages.BinutilsPreferencePage_title, FieldEditorPreferencePage.GRID);
        this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
        this.setDescription(Messages.BinutilsPreferencePage_description);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        faddr2line = new StringFieldEditor(PREFKEY_ADDR2LINE_CMD, Messages.BinutilsPreferencePage_addr2line, this.getFieldEditorParent());
        this.addField(faddr2line);
        faddr2lineArgs = new StringFieldEditor(PREFKEY_ADDR2LINE_ARGS, Messages.BinutilsPreferencePage_addr2line_flags, this.getFieldEditorParent());
        this.addField(faddr2lineArgs);

        fcppfilt = new StringFieldEditor(PREFKEY_CPPFILT_CMD, Messages.BinutilsPreferencePage_cppfilt, this.getFieldEditorParent());
        this.addField(fcppfilt);
        fcppfiltArgs = new StringFieldEditor(PREFKEY_CPPFILT_ARGS, Messages.BinutilsPreferencePage_cppfilt_flags, this.getFieldEditorParent());
        this.addField(fcppfiltArgs);

        fnm = new StringFieldEditor(PREFKEY_NM_CMD, Messages.BinutilsPreferencePage_nm, this.getFieldEditorParent());
        this.addField(fnm);
        fnmArgs = new StringFieldEditor(PREFKEY_NM_ARGS, Messages.BinutilsPreferencePage_nm_flags, this.getFieldEditorParent());
        this.addField(fnmArgs);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        STSymbolManager.sharedInstance.reset();
        return super.performOk();
    }

}
