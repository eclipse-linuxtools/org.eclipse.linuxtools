/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Peria de Sene <rpsene@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tools.launch.ui.properties;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Extends the class ComboFieldEditor including the capability to set and get
 * the selected value from a ComboFieldEditor.
 *
 */
public class CustomComboFieldEditor extends ComboFieldEditor {

	public CustomComboFieldEditor(String name, String labelText,
			String[][] entryNamesAndValues, Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	public String getSelectedValue() {
		doStore();
		return getPreferenceStore().getString(getPreferenceName());
	}

	public void setSelectedValue(String newValue) {
		getPreferenceStore().setValue(getPreferenceName(), newValue);
		doLoad();
	}
}