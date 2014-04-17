/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.binutils.link2source;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.Activator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Editor Input used for source not found
 */
public class STCSourceNotFoundEditorInput implements IEditorInput {

    private final IProject project;
    private final IPath sourcePath;
    private final int lineNumber;

    /**
     * Constructor
     *
     * @param project The project.
     * @param sourcePath The path.
     * @param lineNumber The line number.
     */
    public STCSourceNotFoundEditorInput(IProject project, IPath sourcePath, int lineNumber) {
        this.project = project;
        this.sourcePath = sourcePath;
        this.lineNumber = lineNumber;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/c_file_obj.gif"); //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return sourcePath.lastSegment() + ":" + lineNumber; //$NON-NLS-1$
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return Messages.STCSourceNotFoundEditorInput_source_not_found;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        return null;
    }

    /**
     * @return the project
     */
    public IProject getProject() {
        return project;
    }

    /**
     * @return the sourcePath
     */
    public IPath getSourcePath() {
        return sourcePath;
    }

    /**
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        result = prime * result + ((sourcePath == null) ? 0 : sourcePath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final STCSourceNotFoundEditorInput other = (STCSourceNotFoundEditorInput) obj;
        if (project == null) {
            if (other.project != null) {
                return false;
            }
        } else if (!project.equals(other.project)) {
            return false;
        }
        if (sourcePath == null) {
            if (other.sourcePath != null) {
                return false;
            }
        } else if (!sourcePath.equals(other.sourcePath)) {
            return false;
        }
        return true;
    }

}
