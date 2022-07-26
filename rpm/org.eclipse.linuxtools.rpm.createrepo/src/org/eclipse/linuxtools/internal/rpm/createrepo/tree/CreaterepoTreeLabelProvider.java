/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.tree;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.swt.graphics.Image;

/**
 * Provide the labels and images.
 */
public class CreaterepoTreeLabelProvider extends LabelProvider {

    private static final String CATEGORY_IMAGE = "icons/library_obj.gif"; //$NON-NLS-1$
    private static final String TAG_IMAGE = "icons/templateprop_co.gif"; //$NON-NLS-1$

    @Override
    public String getText(Object element) {
        if (element instanceof CreaterepoTreeCategory category) {
            return category.getName();
        }
        return element.toString();
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof CreaterepoTreeCategory) {
            return Activator.getImageDescriptor(CATEGORY_IMAGE).createImage();
        }
        return Activator.getImageDescriptor(TAG_IMAGE).createImage();
    }

}
