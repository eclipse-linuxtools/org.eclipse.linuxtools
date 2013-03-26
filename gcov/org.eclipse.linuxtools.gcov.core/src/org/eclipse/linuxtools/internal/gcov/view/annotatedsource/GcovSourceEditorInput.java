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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.IAnnotationProvider;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.IEditorInputWithAnnotations;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.ui.ide.FileStoreEditorInput;

public class GcovSourceEditorInput extends FileStoreEditorInput implements IEditorInputWithAnnotations {

    private final SourceFile sourceFile;

    public GcovSourceEditorInput(IFileStore fileStore, SourceFile sourceFile) {
        super(fileStore);
        this.sourceFile = sourceFile;
    }

    @Override
    public IAnnotationProvider createAnnotationProvider() {
        return new GcovAnnotationProvider(sourceFile);
    }

}
