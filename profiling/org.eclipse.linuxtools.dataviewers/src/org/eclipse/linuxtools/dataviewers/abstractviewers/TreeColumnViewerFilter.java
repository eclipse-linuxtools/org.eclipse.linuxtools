/*******************************************************************************
 * Copyright (c) 2013 Kalray.eu
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Implementation of ViewerFilter based on a ISTDataViewersField. Object are filtered according 3 conditions:
 * <ul>
 * <li> {@link ISTDataViewersField#getValue(Object)} matches the given pattern
 * <li>At least one child of the Object matches the given pattern
 * <li>Optionally, a parent of the children matches the given pattern
 * </ul>
 *
 * Note: content provider of the given TreeViewer must be a ITreeContentProvider
 *
 * @since 5.0
 */
public class TreeColumnViewerFilter extends ViewerFilter {

    private String matchingText = "";
    private final TreeViewer treeViewer;
    private final ISTDataViewersField field;
    private final boolean keepAllChildIfParentMatch;

    /**
     * Creates the filter for the given viewer and field to filter on.
     * @param viewer The viewer to filter.
     * @param field The field to filter on.
     * @param keepAllChildIfParentMatch Whether to keep all children of matching element.
     */
    public TreeColumnViewerFilter(TreeViewer viewer, ISTDataViewersField field, boolean keepAllChildIfParentMatch) {
        super();
        this.treeViewer = viewer;
        this.field = field;
        this.keepAllChildIfParentMatch = keepAllChildIfParentMatch;
    }

    /**
     * @param matchingText
     *            the matchingText to set
     */
    public void setMatchingText(String matchingText) {
        this.matchingText = matchingText;
        treeViewer.refresh();
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        ITreeContentProvider provider = (ITreeContentProvider) treeViewer.getContentProvider();
        String s = field.getValue(element);
        if (s.contains(matchingText)) {
            return true;
        }
        if (provider.hasChildren(element)) {
            for (Object o : provider.getChildren(element)) {
                if (select(viewer, element, o)) {
                    return true;
                }
            }
        }
        if (keepAllChildIfParentMatch) {
            while (parentElement != null) {
                String ps = field.getValue(parentElement);
                if (ps.contains(matchingText)) {
                    return true;
                }
                parentElement = provider.getParent(parentElement);
            }
        }
        return false;
    }

}
