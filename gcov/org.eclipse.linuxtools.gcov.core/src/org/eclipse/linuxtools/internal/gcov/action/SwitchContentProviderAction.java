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
package org.eclipse.linuxtools.internal.gcov.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.gcov.Activator;
import org.eclipse.ui.plugin.AbstractUIPlugin;


    /**
     * This action changes the content provider
     */

    public class SwitchContentProviderAction extends Action {

        private final ColumnViewer viewer;
        private final IContentProvider provider;

        public SwitchContentProviderAction(String name, String iconPath, ColumnViewer viewer, IContentProvider provider) {
            super(name, AS_RADIO_BUTTON);
            this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, iconPath));
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