/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.tests.helpers;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class EditorHelper {

    /**
     * Open file associated with <code>diskresource</code> in current
     * workspace.
     *
     * @param diskresource The file to be opened in the current workspace.
     * @return The IEditorPart associated with the opened file in the current workspace
     *         or null if opening fails.
     */
    public static IEditorPart openEditor(IFile diskresource) {
        IWorkbench ws = PlatformUI.getWorkbench();
        try {
            return org.eclipse.ui.ide.IDE.openEditor(ws
                    .getActiveWorkbenchWindow().getActivePage(), diskresource,
                    true);
        } catch (PartInitException e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Close editor if it is active.
     */
    public static void closeEditor(final IEditorPart editor) {
        if (editor.getSite().getWorkbenchWindow().getActivePage() != null) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    // close editor
                    editor.getSite().getWorkbenchWindow().getActivePage()
                            .closeEditor(editor, false);
                }
            });
        }
    }

    /**
     * Return the content of the given IEditorPart as String.
     * @param editorPart
     * @return Content of editorPart.
     */
    public static String getContent(IEditorPart editorPart) {
        AbstractTextEditor castEditor = (AbstractTextEditor) editorPart;
        IDocumentProvider provider = castEditor.getDocumentProvider();
        IDocument document = provider.getDocument(castEditor.getEditorInput());
        return document.get();
    }
}
