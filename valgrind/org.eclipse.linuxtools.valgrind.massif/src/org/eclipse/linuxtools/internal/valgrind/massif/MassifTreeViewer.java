/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.valgrind.ui.CollapseAction;
import org.eclipse.linuxtools.valgrind.ui.ExpandAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

public class MassifTreeViewer {

    private IDoubleClickListener doubleClickListener;
    private ITreeContentProvider contentProvider;
    private Action expandAction;
    private Action collapseAction;

    private TreeViewer viewer;

    public MassifTreeViewer(Composite parent) {
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

        contentProvider = new ITreeContentProvider() {
            @Override
            public Object[] getChildren(Object parentElement) {
                return ((MassifHeapTreeNode) parentElement).getChildren();
            }

            @Override
            public Object getParent(Object element) {
                return ((MassifHeapTreeNode) element).getParent();
            }

            @Override
            public boolean hasChildren(Object element) {
                MassifHeapTreeNode[] children = ((MassifHeapTreeNode) element).getChildren();
                return children != null && children.length > 0;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return (Object[]) inputElement;
            }

            @Override
            public void dispose() {}

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {}

        };
        viewer.setContentProvider(contentProvider);

        viewer.setLabelProvider(new MassifTreeLabelProvider());

        doubleClickListener = new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                MassifHeapTreeNode element = (MassifHeapTreeNode) ((TreeSelection) event.getSelection()).getFirstElement();
                if (element.hasSourceFile()) {
                    MassifPlugin.getDefault().openEditorForNode(element);
                }
                if (contentProvider.hasChildren(element)) {
                    viewer.expandToLevel(element, 1);
                }
            }
        };
        viewer.addDoubleClickListener(doubleClickListener);

        expandAction = new ExpandAction(viewer);
        collapseAction = new CollapseAction(viewer);

        MenuManager manager = new MenuManager();
        manager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                ITreeSelection selection = (ITreeSelection) viewer.getSelection();
                MassifHeapTreeNode element = (MassifHeapTreeNode) selection.getFirstElement();
                if (contentProvider.hasChildren(element)) {
                    manager.add(expandAction);
                    manager.add(collapseAction);
                }
            }
        });

        manager.setRemoveAllWhenShown(true);
        Menu contextMenu = manager.createContextMenu(viewer.getTree());
        viewer.getControl().setMenu(contextMenu);
    }

    public IDoubleClickListener getDoubleClickListener() {
        return doubleClickListener;
    }

    public TreeViewer getViewer() {
        return viewer;
    }
}
