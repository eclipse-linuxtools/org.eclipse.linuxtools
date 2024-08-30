/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;
import org.eclipse.linuxtools.internal.valgrind.ui.CoreMessagesViewer;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.Test;

public class LinkedResourceDoubleClickTest extends AbstractLinkedResourceMemcheckTest {
    private ValgrindStackFrame frame;
    @Test
    public void testLinkedDoubleClickFile() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testLinkedDoubleClickFile"); //$NON-NLS-1$

        doDoubleClick();

        IEditorPart editor = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        IEditorInput input = editor.getEditorInput();
        assertInstanceOf(IFileEditorInput.class, input, "editor input must be file input");
        IFileEditorInput fileInput = (IFileEditorInput) input;
        IFolder srcFolder = proj.getProject().getFolder("src"); //$NON-NLS-1$
        File expectedFile = new File(srcFolder.getLocation().toOSString(),
                frame.getFile());
        File actualFile = fileInput.getFile().getLocation().toFile();

        assertTrue(fileInput.getFile().isLinked(IResource.CHECK_ANCESTORS));
        assertEquals(expectedFile.getCanonicalPath(),
                actualFile.getCanonicalPath());
    }
    @Test
    public void testLinkedDoubleClickLine() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testLinkedDoubleClickLine"); //$NON-NLS-1$

        doDoubleClick();

        IEditorPart editor = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        assertInstanceOf(ITextEditor.class, editor, "editor must be text editor");
        ITextEditor textEditor = (ITextEditor) editor;

        ISelection selection = textEditor.getSelectionProvider().getSelection();
        assertInstanceOf(ITextSelection.class, selection, "selection must be text one");
        TextSelection textSelection = (TextSelection) selection;
        int line = textSelection.getStartLine() + 1; // zero-indexed

        assertEquals(frame.getLine(), line);
    }

    private void doDoubleClick() {
        ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
        CoreMessagesViewer viewer = view.getMessagesViewer();

        // get first leaf
        IValgrindMessage[] elements = (IValgrindMessage[]) viewer.getTreeViewer().getInput();
        IValgrindMessage element = elements[0];
        TreePath path = new TreePath(new Object[] { element });
        frame = null;
        while (element.getChildren().length > 0) {
            element = element.getChildren()[0];
            path = path.createChildPath(element);
            if (element instanceof ValgrindStackFrame) {
                frame = (ValgrindStackFrame) element;
            }
        }
        assertNotNull(frame);

        viewer.getTreeViewer().expandToLevel(frame, AbstractTreeViewer.ALL_LEVELS);
        TreeSelection selection = new TreeSelection(path);

        // do double click
        IDoubleClickListener listener = viewer.getDoubleClickListener();
        listener.doubleClick(new DoubleClickEvent(viewer.getTreeViewer(), selection));
    }
}
