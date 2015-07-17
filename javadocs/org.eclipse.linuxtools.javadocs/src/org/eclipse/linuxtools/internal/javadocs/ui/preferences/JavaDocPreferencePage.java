/*******************************************************************************
 * Copyright (c) 2012-2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 * Eric Williams <ericwill@redhat.com> - modification for Javadocs
 *******************************************************************************/

package org.eclipse.linuxtools.internal.javadocs.ui.preferences;

import java.io.File;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.internal.javadocs.ui.JavaDocPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * The preference page class controls the initialization and running of
 * preference page under Window -> Preferences -> Javadoc documents
 */
public class JavaDocPreferencePage extends FieldEditorPreferencePage implements
	IWorkbenchPreferencePage {

	private final static String JAVADOCS_DIR = "Java.Doc.Directory"; //$NON-NLS-1$
    private final static String TITLE = "Java.Doc.Preference.title"; //$NON-NLS-1$



    /**
     * Initialize the preferences page and specify the preferences store to
     * be used to store preference values.
     */
    public JavaDocPreferencePage() {
        super(GRID);
        setPreferenceStore(JavaDocPlugin.getDefault().getPreferenceStore());

    }

    /**
     * The field editor in the preferences windows. This will allow the user
     * to set the javadoc directory they wish to read help documents from.
     */
    private static class JavadocStringFieldEditor extends DirectoryFieldEditor {


    	public JavadocStringFieldEditor(String name, String labelText,
                Composite parent) {
            super(name, labelText, parent);
            // Load default path and display it in the text box
            setFilterPath(new File(JavaDocPlugin.getDefault().getPreferenceStore().getString(name)));
        }

    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    @Override
    public void createFieldEditors() {
        addField(
                new LabelFieldEditor(
                        getFieldEditorParent(),
                        JavaDocMessages.getString(TITLE)));
        addField(
                new JavadocStringFieldEditor(
                        PreferenceConstants.JAVADOCS_DIRECTORY,
                        JavaDocMessages.getString(JAVADOCS_DIR),
                        getFieldEditorParent()));


    }

    @Override
    public void init(IWorkbench workbench) {

    }

}




