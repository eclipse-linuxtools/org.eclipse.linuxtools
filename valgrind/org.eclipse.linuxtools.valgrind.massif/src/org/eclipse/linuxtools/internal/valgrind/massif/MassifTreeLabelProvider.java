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
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class MassifTreeLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        return ((MassifHeapTreeNode) element).getText();
    }

    @Override
    public Image getImage(Object element) {
        Image img = null;
        if (((MassifHeapTreeNode) element).getParent() == null) { // only show for root elements
			img = ResourceLocator.imageDescriptorFromBundle(MassifPlugin.PLUGIN_ID, "icons/memory_view.gif").get() //$NON-NLS-1$
					.createImage();
        } else { // stack frame
            img = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_STACKFRAME);
        }
        return img;
    }

}
