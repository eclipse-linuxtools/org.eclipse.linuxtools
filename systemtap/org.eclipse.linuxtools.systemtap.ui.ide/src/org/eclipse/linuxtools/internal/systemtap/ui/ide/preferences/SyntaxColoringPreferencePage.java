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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class SyntaxColoringPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    public SyntaxColoringPreferencePage() {
        super();
        setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
        setDescription(Localization.getString("SyntaxColoringPreferencePage.SyntaxColoringOptions")); //$NON-NLS-1$
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {
        //STP Editor
        Composite comp = new Composite(parent, SWT.NULL);
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        stpDC = createColorFieldEditor(
                IDEPreferenceConstants.P_STP_DEFAULT_COLOR,
                Localization.getString("SyntaxColoringPreferencePage.DefaultColor"), comp); //$NON-NLS-1$
        stpKC = createColorFieldEditor(
                IDEPreferenceConstants.P_STP_KEYWORD_COLOR,
                Localization.getString("SyntaxColoringPreferencePage.KeywordColor"), comp); //$NON-NLS-1$
        stpEC = createColorFieldEditor(
                IDEPreferenceConstants.P_STP_EMBEDDED_C_COLOR,
                Localization.getString("SyntaxColoringPreferencePage.EmbeddedCColor"), comp); //$NON-NLS-1$
        stpEE = createColorFieldEditor(
                IDEPreferenceConstants.P_STP_EMBEDDED_COLOR,
                Localization.getString("SyntaxColoringPreferencePage.EmbeddedColor"), comp); //$NON-NLS-1$
        stpCC = createColorFieldEditor(
                IDEPreferenceConstants.P_STP_COMMENT_COLOR,
                Localization.getString("SyntaxColoringPreferencePage.CommentColor"), comp); //$NON-NLS-1$
        stpTC = createColorFieldEditor(
                IDEPreferenceConstants.P_STP_TYPE_COLOR,
                Localization.getString("SyntaxColoringPreferencePage.TypeColor"), comp); //$NON-NLS-1$
        stpSC = createColorFieldEditor(
                IDEPreferenceConstants.P_STP_STRING_COLOR,
                Localization.getString("SyntaxColoringPreferencePage.StringColor"), comp); //$NON-NLS-1$

        return comp;
    }

    private ColorFieldEditor createColorFieldEditor(String name, String lblText, Composite parent) {
        ColorFieldEditor cfe = new ColorFieldEditor(name, lblText, parent);
        cfe.setPage(this);
        cfe.setPreferenceStore(getPreferenceStore());
        cfe.load();

        return cfe;
    }

    @Override
    protected void performDefaults() {
        stpDC.loadDefault();
        stpKC.loadDefault();
        stpEC.loadDefault();
        stpEE.loadDefault();
        stpCC.loadDefault();
        stpTC.loadDefault();
        stpSC.loadDefault();

        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        stpDC.store();
        stpKC.store();
        stpEC.store();
        stpEE.store();
        stpCC.store();
        stpTC.store();
        stpSC.store();

        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        stpDC.dispose();
        stpKC.dispose();
        stpEC.dispose();
        stpEE.dispose();
        stpCC.dispose();
        stpTC.dispose();
        stpSC.dispose();
        stpDC = null;
        stpKC = null;
        stpEC = null;
        stpEE = null;
        stpCC = null;
        stpTC = null;
        stpSC = null;
    }

    private ColorFieldEditor stpDC, stpKC, stpEC, stpEE, stpCC, stpTC, stpSC;
}
