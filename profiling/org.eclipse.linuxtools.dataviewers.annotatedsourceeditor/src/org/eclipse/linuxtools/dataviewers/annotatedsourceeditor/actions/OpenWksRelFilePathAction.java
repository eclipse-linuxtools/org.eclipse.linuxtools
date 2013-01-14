/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.actions;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.AbstractSTAnnotatedSourceEditorInput;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.STAnnotatedSourceEditorActivator;

public abstract class OpenWksRelFilePathAction extends AbstractOpenSourceFileAction {
    private String filepath;

    public OpenWksRelFilePathAction(String filepath, long ts) {
        super(filepath, ts);

        this.filepath = filepath;
    }

    @Override
    public abstract AbstractSTAnnotatedSourceEditorInput getInput(IFileStore fs);

    @Override
    public IFileStore getFileStore() {
        File file = new File(filepath);
        IFile f = null;
        if (!file.isAbsolute()) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            if (workspace != null) {
                IWorkspaceRoot wrkRoot = workspace.getRoot();
                f = wrkRoot.getFile(new Path(filepath));
                IPath p = f.getLocation();
                if (p != null)
                    file = p.toFile();
            }
            try {
                IFileStore fs = EFS.getStore(file.toURI());
                return fs;
            } catch (CoreException e) {
                STAnnotatedSourceEditorActivator.getDefault().getLog().log(e.getStatus());
            }
        }
        return null;
    }

}
