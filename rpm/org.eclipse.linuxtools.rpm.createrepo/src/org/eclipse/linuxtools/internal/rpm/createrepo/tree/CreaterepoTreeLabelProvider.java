/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        if (element instanceof CreaterepoTreeCategory) {
            CreaterepoTreeCategory category = (CreaterepoTreeCategory) element;
            return category.getName();
        }
        return element.toString();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        if (element instanceof CreaterepoTreeCategory) {
            return Activator.getImageDescriptor(CATEGORY_IMAGE).createImage();
        }
        return Activator.getImageDescriptor(TAG_IMAGE).createImage();
    }

}
