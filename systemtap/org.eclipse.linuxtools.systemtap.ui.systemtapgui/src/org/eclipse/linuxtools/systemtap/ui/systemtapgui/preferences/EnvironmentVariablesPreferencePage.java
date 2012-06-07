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

package org.eclipse.linuxtools.systemtap.ui.systemtapgui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.preference.*;
import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.SystemTapGUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;



public class EnvironmentVariablesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Set the description of the page.
	 */
	public EnvironmentVariablesPreferencePage() {
		super();
		LogManager.logDebug("Start EnvironmentVariablesPreferencePage:", this); //$NON-NLS-1$
		setPreferenceStore(SystemTapGUIPlugin.getDefault().getPreferenceStore());
		setDescription("Environment Variables.");
		LogManager.logDebug("End EnvironmentVariablesPreferencePage:", this); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
		LogManager.logDebug("Start init:", this); //$NON-NLS-1$
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
		LogManager.logDebug("End init:", this); //$NON-NLS-1$
	}
	
	/**
	 * Creates a ScrolledComposite, sets options on oit, opens string field editors for the 
	 * preferences.
	 * 
	 * @param The parent of the ScrolledComposite object.
	 *
	 * @return The ScrolledComposite object that is created configured.
	 */
	protected Control createContents(Composite parent) {
		LogManager.logDebug("Start createComponents: parent-" + parent, this); //$NON-NLS-1$
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Composite c = new Composite(sc, SWT.NONE);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setContent(c);
		sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		envVariables = new StringFieldEditor[PreferenceConstants.P_ENV.length];
		for(int i=0; i<envVariables.length; i++) {
			envVariables[i] = createStringFieldEditor(PreferenceConstants.P_ENV[i][0], 
					PreferenceConstants.P_ENV[i][1], c);
		}
		LogManager.logDebug("End createComponents: returnVal-" + sc, this); //$NON-NLS-1$
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
		LogManager.logDebug("Start createStringFieldEditor: name-" + name + ", lblText-" + lblText + ", parent-" + parent, this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		StringFieldEditor sfe = new StringFieldEditor(name, lblText, parent);
		sfe.setPage(this);
		sfe.setPreferenceStore(getPreferenceStore());
		sfe.load();
		
		LogManager.logDebug("End createStringFieldEditor: returnVal-" + sfe, this); //$NON-NLS-1$
		return sfe;
	}
	
	/**
	 * Loads the default environment variables.
	 */
	protected void performDefaults() {
		LogManager.logDebug("Start performDefaults:", this); //$NON-NLS-1$
		for(int i=0; i<envVariables.length; i++)
			envVariables[i].loadDefault();

		super.performDefaults();
		LogManager.logDebug("End performDefaults:", this); //$NON-NLS-1$
	}
	
	/**
	 * Stores the modified environment variables.
	 *
	 * @return True.
	 */
	public boolean performOk() {
		LogManager.logDebug("Start performOk:", this); //$NON-NLS-1$
		for(int i=0; i<envVariables.length; i++)
			envVariables[i].store();
	
		LogManager.logDebug("End performOk: returnVal-true", this); //$NON-NLS-1$
		return true;
	}
	
	/**
	 * Returns the currently stored environment variables in the form of a string array.
	 *
	 * @return The string array containing the current environment variables.
	 */
	public static String[] getEnvironmentVariables() {
    	LogManager.logDebug("Start getEnvVars:", EnvironmentVariablesPreferencePage.class); //$NON-NLS-1$
		ArrayList vars = new ArrayList();
		String[] envVars = null;
		String var;

		int i;
		if(null == SystemTapGUIPlugin.getDefault() || null == SystemTapGUIPlugin.getDefault().getPreferenceStore())
			return null;
		IPreferenceStore p = SystemTapGUIPlugin.getDefault().getPreferenceStore();
		for(i=0; i<PreferenceConstants.P_ENV.length; i++) {
			var = p.getString(PreferenceConstants.P_ENV[i][0]).trim();
			if(!var.equals("")) {
				vars.add(PreferenceConstants.P_ENV[i][0] + "=" + var);
			}
		}
		
		if(vars.size() > 0) {
			envVars = new String[vars.size()];
			for(i = 0; i<vars.size(); i++)
				envVars[i] = (String)vars.get(i);
		}
		
    	LogManager.logDebug("End getEnvVars: returnVal-" + envVars, EnvironmentVariablesPreferencePage.class); //$NON-NLS-1$
		return envVars;
	}
	
	/**
	 * Clears the environment variables string array.
	 */
	public void dispose() {
		LogManager.logDebug("Start dispose:", this); //$NON-NLS-1$
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		super.dispose();
		
		for(int i=0; i<envVariables.length; i++) {
			envVariables[i].dispose();
			envVariables[i] = null;
		}
		envVariables = null;
		LogManager.logDebug("End dispose:", this); //$NON-NLS-1$
	}
	
	private static StringFieldEditor[] envVariables;
}
