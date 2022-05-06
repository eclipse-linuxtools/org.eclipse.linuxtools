/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.binutils.link2source.STLink2SourceSupport;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.FrameworkUtil;

public final class OpenSourceFileAction {

    /**
     * Shared instance of this class
     */
    private OpenSourceFileAction() {
    }

    // FIXME: move this method in binutils plugin.
    private static IFileStore getFileStore(IProject project, IPath path) {
        IEditorInput input = STLink2SourceSupport.getEditorInput(path, project);
        if (input instanceof IURIEditorInput) {
            IURIEditorInput editorInput = (IURIEditorInput) input;
            URI uri = editorInput.getURI();
            try {
                return EFS.getStore(uri);
            } catch (CoreException e) {
                return null;
            }
        } else if (input instanceof IFileEditorInput) {
            IFile f = ((IFileEditorInput) input).getFile();
            try {
                return EFS.getStore(f.getLocationURI());
            } catch (CoreException e) {
                return null;
            }
        }
        return null;
    }

    public static void openAnnotatedSourceFile(IProject project, IFile binary, SourceFile sourceFile, int lineNumber) {
        if (sourceFile == null) {
            return;
        }
        String pathName = sourceFile.getName();
        if (pathName == null) {
            return;
        }
        IPath path = new Path(pathName);
        openAnnotatedSourceFile(project, binary, sourceFile, path, lineNumber);
    }

    public static void openAnnotatedSourceFile(IProject project, IFile binary, SourceFile sourceFile, IPath realLocation,
            int lineNumber) {
    	PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
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
						IStatus s = Status.error(Messages.OpenSourceFileAction_open_error, e);
						Platform.getLog(FrameworkUtil.getBundle(OpenSourceFileAction.class)).log(s);
					}
            } else {
                try {
                    IEditorPart editor = IDE.openEditorOnFileStore(page, fs);
                    if (lineNumber > 0 && editor instanceof ITextEditor) {
                        IDocumentProvider provider = ((ITextEditor) editor).getDocumentProvider();
                        IDocument document = provider.getDocument(editor.getEditorInput());
                        try {
                            int start = document.getLineOffset(lineNumber - 1);
                            ((ITextEditor) editor).selectAndReveal(start, 0);
                        } catch (BadLocationException e) {
                            // ignore
                        }
                        IWorkbenchPage p = editor.getSite().getPage();
                        p.activate(editor);
                    }
					} catch (PartInitException e) {
						IStatus s = Status.error(Messages.OpenSourceFileAction_open_error, e);
						Platform.getLog(FrameworkUtil.getBundle(OpenSourceFileAction.class)).log(s);
					}
            }

        }
    	});
    }

}
