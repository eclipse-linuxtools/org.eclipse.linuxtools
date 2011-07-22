/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpmstubby.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.rpmstubby.StubbyPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for controlling spec files stub creation.
 *
 */
public class StubbyPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * default constructor
	 */
	public StubbyPreferencePage() {
		super(GRID);
		setPreferenceStore(StubbyPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 */
	@Override
	public void createFieldEditors() {
	}

	/**
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
