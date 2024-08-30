/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc.
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
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot.SnapshotType;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;


public abstract class AbstractMassifTest extends AbstractValgrindTest {

    @Override
    protected String getToolID() {
        return MassifPlugin.TOOL_ID;
    }

    protected void checkFile(IProject proj, MassifHeapTreeNode node) {
        IEditorPart editor = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        IEditorInput input = editor.getEditorInput();
        assertInstanceOf(IFileEditorInput.class, input);
        IFileEditorInput fileInput = (IFileEditorInput) input;
        IResource expectedResource = proj.findMember(node.getFilename());
        assertNotNull(expectedResource);
        File expectedFile = expectedResource.getLocation().toFile();
        File actualFile = fileInput.getFile().getLocation().toFile();
        assertEquals(expectedFile, actualFile);
    }

    protected void checkLine(MassifHeapTreeNode node) {
        IEditorPart editor = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        assertTrue(editor instanceof ITextEditor);
        ITextEditor textEditor = (ITextEditor) editor;

        ISelection selection = textEditor.getSelectionProvider().getSelection();
        assertTrue(selection instanceof TextSelection);
        TextSelection textSelection = (TextSelection) selection;
        int line = textSelection.getStartLine() + 1; // zero-indexed

        assertEquals(node.getLine(), line);
    }

    /**
     * Check snapshots contain the expected used bytes.
     *
     * @param snapshots MassifSnapshots to check
     * @param usefulBytesDelta scale of useful bytes
     * @param extraBytesDelta scale of extra bytes
     */
    protected void checkSnapshots(MassifSnapshot[] snapshots, int usefulBytesDelta, int extraBytesDelta){
        long expectedHeapBytes = 0;
        long expectedHeapExtraBytes = 0;
        boolean pastPeakUsage = false;

        for (MassifSnapshot snapshot : snapshots) {
            if (snapshot.getTime() == 0) {
                // no need to update expected values
            } else if (snapshot.getType().compareTo(SnapshotType.PEAK) == 0) {
                pastPeakUsage = true;
                // no need to update expected values
            } else if (!pastPeakUsage) {
                expectedHeapBytes += usefulBytesDelta;
                expectedHeapExtraBytes += extraBytesDelta;
            } else {
                // past the peak , heap bytes used begin to decrease
                expectedHeapBytes -= usefulBytesDelta;
                expectedHeapExtraBytes -= extraBytesDelta;
            }

            assertEquals(expectedHeapBytes, snapshot.getHeapBytes());
            assertEquals(expectedHeapExtraBytes, snapshot.getHeapExtra());
            assertEquals(expectedHeapBytes + expectedHeapExtraBytes, snapshot.getTotal());
        }
    }

    protected IAction getToolbarAction(String actionId) {
        IAction result = null;
        ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
        IToolBarManager manager = view.getViewSite().getActionBars()
                .getToolBarManager();
        for (IContributionItem item : manager.getItems()) {
            if (item instanceof ActionContributionItem actionItem) {
                if (actionItem.getAction().getId()
                        .equals(actionId)) {
                    result = actionItem.getAction();
                }
            }
        }
        return result;
    }

}
