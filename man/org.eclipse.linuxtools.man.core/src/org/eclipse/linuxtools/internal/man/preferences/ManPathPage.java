/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.FrameworkUtil;

/**
 * Man page preferences.
 */

public class ManPathPage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * Preference page for setting the man path.
	 */
	public ManPathPage() {
		super(GRID);
		IPreferenceStore prefs = new ScopedPreferenceStore(
				InstanceScope.INSTANCE,
				FrameworkUtil.getBundle(this.getClass()).getSymbolicName());
		setPreferenceStore(prefs);
		setDescription(Messages.ManPathPage_0);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceConstants.P_PATH,
				Messages.ManPathPage_1, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing to do
	}

}