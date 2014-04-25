/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.forms;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class SpecfileFormEditor extends FormEditor {

    FormPage mainPackage;
    SpecfileEditor editor;
    private Specfile specfile;

    public SpecfileFormEditor() {
        super();
        editor = new SpecfileEditor();
    }

    @Override
    protected FormToolkit createToolkit(Display display) {
        // Create a toolkit that shares colors between editors.
        return new FormToolkit(Display.getCurrent());
    }

    @Override
    protected void addPages() {
        try {
            int index = addPage(editor, getEditorInput());
            setPageText(index, Messages.SpecfileFormEditor_0);
            specfile = editor.getSpecfile();
            mainPackage = new MainPackagePage(this, specfile);
            addPage(0, mainPackage);
            addPage(1, new RpmSectionPage(this, specfile, RpmSections.PREP_SECTION));
            addPage(2, new RpmSectionPage(this, specfile, RpmSections.BUILD_SECTION));
            addPage(3, new RpmSectionPage(this, specfile, RpmSections.INSTALL_SECTION));
        } catch (PartInitException e) {
            //
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        editor.doSave(monitor);
    }

    @Override
    public void doSaveAs() {
        //noop
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return editor.isDirty();
    }
}
