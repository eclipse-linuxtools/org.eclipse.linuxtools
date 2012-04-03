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
		LogManager.logDebug("Start EnvironmentVariablesPreferencePage:", this);
		setPreferenceStore(SystemTapGUIPlugin.getDefault().getPreferenceStore());
		setDescription("Environment Variables.");
		LogManager.logDebug("End EnvironmentVariablesPreferencePage:", this);
	}

	public void init(IWorkbench workbench) {
		LogManager.logDebug("Start init:", this);
		LogManager.logInfo("Initializing", this);
		LogManager.logDebug("End init:", this);
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
		LogManager.logDebug("Start createComponents: parent-" + parent, this);
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
		LogManager.logDebug("End createComponents: returnVal-" + sc, this);
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
		LogManager.logDebug("Start createStringFieldEditor: name-" + name + ", lblText-" + lblText + ", parent-" + parent, this);
		StringFieldEditor sfe = new StringFieldEditor(name, lblText, parent);
		sfe.setPage(this);
		sfe.setPreferenceStore(getPreferenceStore());
		sfe.load();
		
		LogManager.logDebug("End createStringFieldEditor: returnVal-" + sfe, this);
		return sfe;
	}
	
	/**
	 * Loads the default environment variables.
	 */
	protected void performDefaults() {
		LogManager.logDebug("Start performDefaults:", this);
		for(int i=0; i<envVariables.length; i++)
			envVariables[i].loadDefault();

		super.performDefaults();
		LogManager.logDebug("End performDefaults:", this);
	}
	
	/**
	 * Stores the modified environment variables.
	 *
	 * @return True.
	 */
	public boolean performOk() {
		LogManager.logDebug("Start performOk:", this);
		for(int i=0; i<envVariables.length; i++)
			envVariables[i].store();
	
		LogManager.logDebug("End performOk: returnVal-true", this);
		return true;
	}
	
	/**
	 * Returns the currently stored environment variables in the form of a string array.
	 *
	 * @return The string array containing the current environment variables.
	 */
	public static String[] getEnvironmentVariables() {
    	LogManager.logDebug("Start getEnvVars:", EnvironmentVariablesPreferencePage.class);
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
		
    	LogManager.logDebug("End getEnvVars: returnVal-" + envVars, EnvironmentVariablesPreferencePage.class);
		return envVars;
	}
	
	/**
	 * Clears the environment variables string array.
	 */
	public void dispose() {
		LogManager.logDebug("Start dispose:", this);
		LogManager.logInfo("Disposing", this);
		super.dispose();
		
		for(int i=0; i<envVariables.length; i++) {
			envVariables[i].dispose();
			envVariables[i] = null;
		}
		envVariables = null;
		LogManager.logDebug("End dispose:", this);
	}
	
	private static StringFieldEditor[] envVariables;
}