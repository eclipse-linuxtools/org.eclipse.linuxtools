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

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class EnvironmentVariablesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private static StringFieldEditor[] envVariables;

    /**
     * Set the description of the page.
     */
    public EnvironmentVariablesPreferencePage() {
        super();
        setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.EnvironmentVariablesPreferencePage_Title);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    /**
     * Creates a ScrolledComposite, sets options on oit, opens string field editors for the
     * preferences.
     *
     * @param parent The parent of the ScrolledComposite object.
     *
     * @return The ScrolledComposite object that is created configured.
     */
    @Override
    protected Control createContents(Composite parent) {
        ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL );
        Composite c = new Composite(sc, SWT.NONE);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        sc.setContent(c);

        envVariables = new StringFieldEditor[PreferenceConstants.P_ENV.values().length];
        int i = 0;
        for (PreferenceConstants.P_ENV env : PreferenceConstants.P_ENV.values()) {
            envVariables[i] = createStringFieldEditor(env.toPrefKey(), env.toEnvKey(), c);
            i++;
        }
        return sc;
    }

    /**
     * Creates and returns a StringFieldEditor object with preferences set to it.
     *
     * @param name Name of the field.
     * @param lblText Label text of the field.
     * @param parent Composite object parent of the object.
     *
     * @return The created and configued StringFieldEditor ojbect.
     */
    private StringFieldEditor createStringFieldEditor(String name, String lblText, Composite parent) {
        StringFieldEditor sfe = new StringFieldEditor(name, lblText, parent);
        sfe.setPage(this);
        sfe.setPreferenceStore(getPreferenceStore());
        sfe.load();

        return sfe;
    }

    /**
     * Loads the default environment variables.
     */
    @Override
    protected void performDefaults() {
        for (StringFieldEditor envVariable : envVariables) {
            envVariable.loadDefault();
        }

        super.performDefaults();
    }

    /**
     * Stores the modified environment variables.
     *
     * @return True.
     */
    @Override
    public boolean performOk() {
        for (StringFieldEditor envVariable : envVariables) {
            envVariable.store();
        }

        return true;
    }

    /**
     * Returns the currently stored environment variables in the form of a string array.
     *
     * @return The string array containing the current environment variables.
     */
    public static String[] getEnvironmentVariables() {
        ArrayList<String> vars = new ArrayList<>();
        String[] envVars = null;

        int i;
        if (IDEPlugin.getDefault() == null || IDEPlugin.getDefault().getPreferenceStore() == null) {
            return null;
        }
        IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
        for (PreferenceConstants.P_ENV env : PreferenceConstants.P_ENV.values()) {
            String val = p.getString(env.toPrefKey()).trim();
            if (!val.isEmpty()) {
                vars.add(env.createKeyValString(val));
            }
        }

        if (vars.size() > 0) {
            envVars = new String[vars.size()];
            for (i = 0; i < vars.size(); i++) {
                envVars[i] = vars.get(i);
            }
        }

        return envVars;
    }

    /**
     * Clears the environment variables string array.
     */
    @Override
    public void dispose() {
        super.dispose();

        for (int i = 0; i < envVariables.length; i++) {
            envVariables[i].dispose();
            envVariables[i] = null;
        }
        envVariables = null;
    }
}
