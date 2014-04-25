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

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Composite;

public class ListEditor extends org.eclipse.jface.preference.ListEditor {
    public ListEditor(String name, String labelText, String dialogText, String initialVal, IInputValidator validator, Composite parent) {
        init(name, labelText);
        dialogTitle = dialogText;
        this.initialVal = initialVal;
        this.validator = validator;
        createControl(parent);
    }

    /**
     * Creates and populates a StringBuffer with the supplied items.
     *
     * @param items An array of strings to make the StringBuffer with.
     *
     * @return Returns the StringBuffer.
     */
     @Override
    protected String createList(String[] items) {
        StringBuffer path = new StringBuffer();

        for (String item: items) {
            path.append(item);
            path.append(File.pathSeparator);
        }
        return path.toString();
    }

    @Override
    protected String getNewInputObject() {
        InputDialog dialog = new InputDialog(getShell(), dialogTitle, null, initialVal, validator);
        dialog.open();

        return dialog.getValue();
    }

    /**
     * Parses the passed in string into an array of strings.
     *
     * @param stringList The string to pass parse.
     *
     * @return Returns the array of strings.
     */
     @Override
    protected String[] parseString(String stringList) {
        StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r"); //$NON-NLS-1$
        ArrayList<Object> v = new ArrayList<>();
        while (st.hasMoreElements()) {
            v.add(st.nextElement());
        }
        return v.toArray(new String[v.size()]);
    }

    private String dialogTitle;
    private String initialVal;
    private IInputValidator validator;
}
