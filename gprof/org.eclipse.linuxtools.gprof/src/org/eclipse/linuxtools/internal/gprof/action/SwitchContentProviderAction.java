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
package org.eclipse.linuxtools.internal.gprof.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.gprof.Activator;

/**
 * This action changes the content provider of
 * the {@link org.eclipse.linuxtools.internal.gprof.view.GmonView}
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class SwitchContentProviderAction extends Action {

    private final ColumnViewer viewer;
    private final ITreeContentProvider provider;

    /**
     * Constructor
     * @param name of the action
     * @param iconPath the icon path
     * @param viewer TreeViewer
     * @param provider the new content provider for the given TreeViewer
     */
    public SwitchContentProviderAction(String name, String iconPath, ColumnViewer viewer, ITreeContentProvider provider) {
        super(name, AS_RADIO_BUTTON);
        this.setImageDescriptor(Activator.getImageDescriptor(iconPath));
        this.setToolTipText(name);
        this.viewer = viewer;
        this.provider = provider;
    }

    @Override
    public void run() {
        viewer.getControl().setRedraw(false);
        viewer.setContentProvider(provider);
        ((TreeViewer)viewer).expandToLevel(2);
        viewer.getControl().setRedraw(true);
    }

}
