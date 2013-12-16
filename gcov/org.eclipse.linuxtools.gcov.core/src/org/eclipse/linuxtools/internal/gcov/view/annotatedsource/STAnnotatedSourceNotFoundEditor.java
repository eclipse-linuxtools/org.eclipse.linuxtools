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

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.linuxtools.binutils.link2source.STCSourceNotFoundEditor#openSourceFileAtLocation(org.eclipse.core
     * .resources.IProject, org.eclipse.core.runtime.IPath, int)
     */
    @Override
    protected void openSourceFileAtLocation(IProject project, IPath sourceLoc, int lineNumber) {
        IEditorInput input = this.getEditorInput();
        if (input instanceof STAnnotatedSourceNotFoundEditorInput) {
            STAnnotatedSourceNotFoundEditorInput editorInput = (STAnnotatedSourceNotFoundEditorInput) input;
            SourceFile sf = editorInput.getSourceFile();
            OpenSourceFileAction.sharedInstance.openAnnotatedSourceFile(project, null, sf, sourceLoc, lineNumber);
        } else {
            super.openSourceFileAtLocation(project, sourceLoc, lineNumber);
        }
    }
}
