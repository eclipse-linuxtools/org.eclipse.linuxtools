/*******************************************************************************
 * Copyright (c) 2013, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

    public void setSelectedValue(String newValue) {
        getPreferenceStore().setValue(getPreferenceName(), newValue);
        doLoad();
    }
}
