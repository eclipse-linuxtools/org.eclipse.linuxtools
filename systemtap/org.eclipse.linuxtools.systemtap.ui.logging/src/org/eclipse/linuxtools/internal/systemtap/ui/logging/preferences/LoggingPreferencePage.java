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

package org.eclipse.linuxtools.internal.systemtap.ui.logging.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.logging.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.logging.LoggingPlugin;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class LoggingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public LoggingPreferencePage() {
		super(GRID);
		setPreferenceStore(LoggingPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("LoggingPreferencePage.LoggingDescription"));
	}

	@Override
	public void createFieldEditors() {
		BooleanFieldEditor logging =
			new BooleanFieldEditor(PreferenceConstants.P_LOG_ENABLED, Localization.getString("LoggingPreferencePage.EnableLogging"), getFieldEditorParent());
		String[] debugLevel = {Localization.getString("LoggingPreferencePage.Debug"), "" + LogManager.DEBUG};
		String[] infoLevel = {Localization.getString("LoggingPreferencePage.Info"),"" + LogManager.INFO};
		String[] criticalLevel = {Localization.getString("LoggingPreferencePage.Critical"), "" + LogManager.CRITICAL};
		String[] fatalLevel = {Localization.getString("LoggingPreferencePage.Fatal"), "" + LogManager.FATAL};
		String[][] levels = {debugLevel,infoLevel,criticalLevel,fatalLevel};

		ComboFieldEditor level =
			new ComboFieldEditor(PreferenceConstants.P_LOG_LEVEL, Localization.getString("LoggingPreferencePage.LoggingLevel"), levels, getFieldEditorParent());
		RadioGroupFieldEditor loggingType =
			new RadioGroupFieldEditor(PreferenceConstants.P_LOG_TYPE, Localization.getString("LoggingPreferencePage.LogTo"), 1,
					new String[][] {{ Localization.getString("LoggingPreferencePage.Console"), "" + LogManager.CONSOLE},
									{Localization.getString("LoggingPreferencePage.File"), "" + LogManager.FILE} }
					, getFieldEditorParent());
		StringFieldEditor file = new StringFieldEditor(PreferenceConstants.P_LOG_FILE, Localization.getString("LoggingPreferencePage.File"), getFieldEditorParent());
		file.setEmptyStringAllowed(true);

		this.addField(logging);
		this.addField(level);
		this.addField(loggingType);
		this.addField(file);
	}

	public void init(IWorkbench workbench) {
	}

}