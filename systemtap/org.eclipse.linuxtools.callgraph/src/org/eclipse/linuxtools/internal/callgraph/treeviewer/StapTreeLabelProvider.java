/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.treeviewer;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.internal.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.internal.callgraph.StapData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class StapTreeLabelProvider extends LabelProvider {

    @Override
    public Image getImage(Object element) {
        Image im = null;
        if ( ((StapData) element).isMarked()) {
            im = AbstractUIPlugin.imageDescriptorFromPlugin(CallGraphConstants.PLUGIN_ID, "/icons/public_co.gif").createImage(); //$NON-NLS-1$
        } else {
            im = AbstractUIPlugin.imageDescriptorFromPlugin(CallGraphConstants.PLUGIN_ID, "/icons/compare_method.gif").createImage(); //$NON-NLS-1$
        }
        return im;
    }

    @Override
    public String getText(Object element) {
        return ((StapData) element).timesCalled + ": " + ((StapData) element).name; //$NON-NLS-1$
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }
}
