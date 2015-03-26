/*******************************************************************************
 * Copyright (c) 2012, 2015 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.graphing.ui.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.views.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @since 3.0 Migrated from .ui.graphing package.
 */
public class GraphSelectorEditorInput implements IEditorInput {

    private String title = null;

    public GraphSelectorEditorInput() {
        title = Messages.GraphSelectorEditor_graphsEditorDefaultTitle;
    }

    /**
     * Creates the editor input with the given title.
     * @param scriptTitle The title.
     * @since 2.2
     */
    public GraphSelectorEditorInput(String scriptTitle) {
        title = NLS.bind(Messages.GraphSelectorEditor_graphsEditorTitle, scriptTitle);
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return title;
    }

}
