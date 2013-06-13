/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view.annotatedsource;

import java.net.URI;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.binutils.link2source.STLink2SourceSupport;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.actions.AbstractOpenSourceFileAction;
import org.eclipse.linuxtools.internal.gcov.Activator;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenSourceFileAction {

    /**
     * Shared instance of this class
     */
    public static final OpenSourceFileAction sharedInstance = new OpenSourceFileAction();

    private OpenSourceFileAction() {
    }

    // FIXME: move this method in binutils plugin.
    private IFileStore getFileStore(IProject project, IPath path) {
        IEditorInput input = STLink2SourceSupport.sharedInstance.getEditorInput(path, project);
        if (input instanceof IURIEditorInput) {
            IURIEditorInput editorInput = (IURIEditorInput) input;
            URI uri = editorInput.getURI();
            try {
                IFileStore fs = EFS.getStore(uri);
                return fs;
            } catch (CoreException _) {
                return null;
            }
        }
        return null;
    }

    private GcovSourceEditorInput getInput(SourceFile sourceFile, IFileStore fs) {
        GcovSourceEditorInput input = new GcovSourceEditorInput(fs, sourceFile);
        IWorkbenchPage p = CUIPlugin.getActivePage();
        IEditorPart editorPart = p.findEditor(input);
        if (editorPart != null)
            p.closeEditor(editorPart, false);
        return input;
    }

    public void openAnnotatedSourceFile(IProject project, IFile binary, SourceFile sourceFile, int lineNumber) {
        if (sourceFile == null)
            return;
        String pathName = sourceFile.getName();
        if (pathName == null)
            return;
        IPath path = new Path(pathName);
        openAnnotatedSourceFile(project, binary, sourceFile, path, lineNumber);
    }

    public void openAnnotatedSourceFile(IProject project, IFile binary, SourceFile sourceFile, IPath realLocation,
            int lineNumber) {
        IWorkbenchPage page = CUIPlugin.getActivePage();
        if (page != null) {
            IFileStore fs = getFileStore(project, realLocation);
            if (fs == null && !realLocation.isAbsolute() && binary != null) {
                IPath p = binary.getProjectRelativePath().removeLastSegments(1);
                fs = getFileStore(project, p.append(realLocation));
            }
            if (fs == null) {
                try {
                    page.openEditor(new STAnnotatedSourceNotFoundEditorInput(project, sourceFile, realLocation,
                            lineNumber), STAnnotatedSourceNotFoundEditor.ID, true);
                } catch (PartInitException e) {
                    Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                            Messages.OpenSourceFileAction_open_error, e);
                    Activator.getDefault().getLog().log(s);
                }
            } else {
                IEditorInput input = getInput(sourceFile, fs);
                try {
                    IEditorPart editor = page.openEditor(input, AbstractOpenSourceFileAction.EDITOR_ID, true);
                    if (lineNumber > 0 && editor instanceof ITextEditor) {
                        IDocumentProvider provider = ((ITextEditor) editor).getDocumentProvider();
                        IDocument document = provider.getDocument(editor.getEditorInput());
                        try {
                            int start = document.getLineOffset(lineNumber - 1);
                            ((ITextEditor) editor).selectAndReveal(start, 0);
                        } catch (BadLocationException _) {
                            // ignore
                        }
                        IWorkbenchPage p = editor.getSite().getPage();
                        p.activate(editor);
                    }
                } catch (PartInitException e) {
                    Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                            Messages.OpenSourceFileAction_open_error, e);
                    Activator.getDefault().getLog().log(s);
                }
            }
        }
    }

}
