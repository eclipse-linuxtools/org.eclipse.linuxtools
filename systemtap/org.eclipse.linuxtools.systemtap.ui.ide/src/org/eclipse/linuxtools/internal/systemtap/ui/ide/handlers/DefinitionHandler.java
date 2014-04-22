/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.CommentRemover;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ISearchableNode;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.ui.editor.handlers.file.OpenFileHandler;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class DefinitionHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        TreeDefinitionNode t = getSelection(event);
        if(t == null) {
            return null;
        }
        String filename = t.getDefinition();
        if (filename == null) {
            return null;
        }
        File file = new File(filename);
        OpenFileHandler open = new OpenFileHandler();
        open.executeOnFile(file);
        if (open.isSuccessful() && t.getData() instanceof ISearchableNode) {
            IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            STPEditor editor = (STPEditor)editorPart;
            editor.jumpToLocation(findDefinitionLine((ISearchableNode) t.getData(), editor) + 1, 0);
        }
        return null;
    }

    private TreeDefinitionNode getSelection(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof StructuredSelection) {
            Object[] selections = ((StructuredSelection) selection).toArray();
            return (selections.length == 1 && selections[0] instanceof TreeDefinitionNode)
                    ? (TreeDefinitionNode) selections[0] : null;
        }
        return null;
    }

    private int findDefinitionLine(ISearchableNode data, STPEditor editor) {
        int locationIndex;
        String contents = CommentRemover.exec(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get());
        if (data.isRegexSearch()) {
            Pattern pattern = Pattern.compile(data.getSearchToken());
            Matcher matcher = pattern.matcher(contents);
            locationIndex = matcher.find() ? matcher.start() : -1;
        } else {
            locationIndex = contents.indexOf(data.getSearchToken());
        }
        if (locationIndex != -1) {
            // Get the line of the match by counting newlines.
            contents = contents.substring(0, locationIndex);
            return contents.length() - contents.replaceAll("\n", "").length(); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return 0;
        }
    }

}
