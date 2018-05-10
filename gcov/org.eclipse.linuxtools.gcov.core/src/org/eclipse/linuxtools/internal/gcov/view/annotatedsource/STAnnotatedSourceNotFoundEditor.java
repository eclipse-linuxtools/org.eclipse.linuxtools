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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.binutils.link2source.STCSourceNotFoundEditor;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.ui.IEditorInput;

/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class STAnnotatedSourceNotFoundEditor extends STCSourceNotFoundEditor {

    public static final String ID = "org.eclipse.linuxtools.gcov.view.annotatedsource.STAnnotatedSourceNotFoundEditor"; //$NON-NLS-1$

    @Override
    protected void openSourceFileAtLocation(IProject project, IPath sourceLoc, int lineNumber) {
        IEditorInput input = this.getEditorInput();
        if (input instanceof STAnnotatedSourceNotFoundEditorInput) {
            STAnnotatedSourceNotFoundEditorInput editorInput = (STAnnotatedSourceNotFoundEditorInput) input;
            SourceFile sf = editorInput.getSourceFile();
            OpenSourceFileAction.openAnnotatedSourceFile(project, null, sf, sourceLoc, lineNumber);
        } else {
            super.openSourceFileAtLocation(project, sourceLoc, lineNumber);
        }
    }
}
