/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class MassifTreeLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        return ((MassifHeapTreeNode) element).getText();
    }

    @Override
    public Image getImage(Object element) {
        Image img = null;
        if (((MassifHeapTreeNode) element).getParent() == null) { // only show for root elements
            img = AbstractUIPlugin.imageDescriptorFromPlugin(MassifPlugin.PLUGIN_ID, "icons/memory_view.gif").createImage(); //$NON-NLS-1$
        } else { // stack frame
            img = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_STACKFRAME);
        }
        return img;
    }

}
