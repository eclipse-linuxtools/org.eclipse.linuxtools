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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The content provider for the category tree in the metadata form page.
 */
public class CreaterepoTreeContentProvider implements ITreeContentProvider {

    private CreaterepoCategoryModel model;

    @Override
    public void dispose() {/* not implemented */}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof CreaterepoCategoryModel) {
            model = (CreaterepoCategoryModel) newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return model.getCategories().toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof CreaterepoTreeCategory) {
            CreaterepoTreeCategory category = (CreaterepoTreeCategory) parentElement;
            return category.getTags().toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof CreaterepoTreeCategory) {
            CreaterepoTreeCategory category = (CreaterepoTreeCategory) element;
            // category has children if ! empty
            return !category.getTags().isEmpty();
        }
        return false;
    }

}
