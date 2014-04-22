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

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.systemtap.ui.editor.handlers.file.NewFileHandler;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * A simple class that contains information about the current session of the IDE, such as
 * the path to the tapset libraries, and the active SystemTap Script Editor.
 * @author Ryan Morse
 */
public class IDESessionSettings {
    /**
     * Use {@link IDESessionSettings#setActiveSTPEditor(STPEditor)} and
     * {@link IDESessionSettings#getActiveSTPEditor()}
     */
    private static STPEditor activeSTPEditor = null;

    /**
     * Returns the most recent active {@link STPEditor} script editor if one was
     * set. If one was not set and there is only one {@link STPEditor} script editor
     * open then that one is returned. Otherwise returns null.
     * @return The most recent active {@link STPEditor}
     * @since 1.2
     */
    public static STPEditor getActiveSTPEditor() {
        if (activeSTPEditor == null) {
            // If no active editor is et and there is only one
            // opened stap script editor, set it to be the active editor.
            IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
            STPEditor stpEditor = null;
            for (IEditorReference editor: editors) {
                if (editor.getId().equals(STPEditor.ID)) {
                    if (stpEditor == null) {
                        stpEditor = (STPEditor) editor.getEditor(true);
                    } else {
                        return null;
                    }
                }
            }
            activeSTPEditor = stpEditor;
        }
        return activeSTPEditor;
    }

    /**
     * Sets the current active editor.
     * @param editor the active editor.
     * @since 1.2
     */
    public static void setActiveSTPEditor(STPEditor editor) {
        activeSTPEditor = editor;
    }

    /**
     * Restore and return an STPEditor based on the user's choice.
     * @param checkCurrent Set to <code>true</code> if the currently active editor may be returned
     * if it is an {@link STPEditor}.
     * @return
     */
    public static STPEditor getOrAskForActiveSTPEditor(boolean checkCurrent) {
        STPEditor stpEditor;
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        if (checkCurrent) {
            IEditorPart editor = page.getActiveEditor();
            stpEditor = editor instanceof STPEditor ? (STPEditor) editor : askForSTPEditor(window);
        } else {
            stpEditor = askForSTPEditor(window);
        }
        if (stpEditor != null) {
            page.activate(stpEditor);
        }
        return stpEditor;
    }

    private static STPEditor askForSTPEditor(IWorkbenchWindow window) {
        final List<STPEditor> allEditors = new LinkedList<>();
        for (IEditorReference editor : window.getActivePage().getEditorReferences()) {
            if (editor.getId().equals(STPEditor.ID)) {
                allEditors.add((STPEditor) editor.getEditor(true));
            }
        }

        switch (allEditors.size()) {
        // If only one file is found, open it. Give user the option to open another file.
        case 1:
            MessageDialog messageDialog = new MessageDialog(window.getShell(),
                    Localization.getString("GetEditorAction.DialogTitle"), null, //$NON-NLS-1$
                    MessageFormat.format(Localization.getString("GetEditorAction.AskBeforeOpenMessage"), //$NON-NLS-1$
                            allEditors.get(0).getEditorInput().getName() ),
                            MessageDialog.QUESTION,
                            new String[]{Localization.getString("GetEditorAction.AskBeforeOpenCancel"), //$NON-NLS-1$
                Localization.getString("GetEditorAction.AskBeforeOpenAnother"), //$NON-NLS-1$
                Localization.getString("GetEditorAction.AskBeforeOpenYes")}, 2); //$NON-NLS-1$

            switch (messageDialog.open()) {
            case 2:
                return allEditors.get(0);

            case 1:
                return openNewFile(window);

            default:
                return null;
            }

        // If no files found, prompt user to open a new file
        case 0:
            return openNewFile(window);

        // If multiple files found, prompt user to select one of them
        default:
            return openNewFileFromMultiple(window, allEditors);
        }
    }

    private static STPEditor openNewFileFromMultiple(IWorkbenchWindow window, final List<STPEditor> allEditors) {
        ListDialog listDialog = new ListDialog(window.getShell());
        listDialog.setTitle(Localization.getString("GetEditorAction.DialogTitle")); //$NON-NLS-1$
        listDialog.setContentProvider(new ArrayContentProvider());

        listDialog.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                int i = (Integer) element;
                return i != -1 ? allEditors.get(i).getEditorInput().getName()
                        : Localization.getString("GetEditorAction.OtherFile"); //$NON-NLS-1$
            }
        });

        Integer[] editorIndexes = new Integer[allEditors.size() + 1];
        for (int i = 0; i < editorIndexes.length - 1; i++) {
            editorIndexes[i] = i;
        }
        editorIndexes[editorIndexes.length - 1] = -1;
        listDialog.setInput(editorIndexes);
        listDialog.setMessage(Localization.getString("GetEditorAction.SelectEditor")); //$NON-NLS-1$
        if (listDialog.open() == Window.OK) {
            int result = (Integer) listDialog.getResult()[0];
            return result != -1 ? allEditors.get(result) : openNewFile(window);
        }
        // Abort if user cancels
        return null;
    }

    private static STPEditor openNewFile(IWorkbenchWindow window) {
        NewFileHandler action = new NewFileHandler();
        action.execute(null);
        if (action.isSuccessful()) {
            return (STPEditor) window.getActivePage().getActiveEditor();
        }
        return null;
    }
}
