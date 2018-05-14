/*******************************************************************************
 * (C) Copyright 2010, 2018 IBM Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.model.PMDso;
import org.eclipse.linuxtools.internal.perf.model.PMEvent;
import org.eclipse.linuxtools.internal.perf.model.PMFile;
import org.eclipse.linuxtools.internal.perf.model.PMLineRef;
import org.eclipse.linuxtools.internal.perf.model.PMSymbol;
import org.eclipse.linuxtools.internal.perf.model.TreeParent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class PerfViewLabelProvider extends LabelProvider {

    @Override
    public String getText(Object obj) {
        return obj.toString();
    }
    @Override
    public Image getImage(Object obj) {
        String imageKey;

        if (obj instanceof PMDso) {
            imageKey = "icons/dso.gif"; //$NON-NLS-1$
        } else if (obj instanceof PMSymbol) {
            imageKey = "icons/symbol.gif"; //$NON-NLS-1$
            if (((PMSymbol)obj).conflicted())
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (obj instanceof PMLineRef) {
            imageKey = "icons/line.gif"; //$NON-NLS-1$
        } else if (obj instanceof PMEvent) {
            imageKey = "icons/event.gif"; //$NON-NLS-1$
        } else if (obj instanceof PMFile) {
            imageKey = "icons/file.gif"; //$NON-NLS-1$
            if (((PMFile)obj).getName().equals(PerfPlugin.STRINGS_MultipleFilesForSymbol)) {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            }
        } else if (obj instanceof TreeParent) {
            imageKey = ISharedImages.IMG_OBJ_FOLDER;
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        } else {
            imageKey = ISharedImages.IMG_OBJ_ELEMENT;
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }
        return PerfPlugin.getImageDescriptor(imageKey).createImage();
    }
}
