/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.BrowserViewAction;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;



/**
 * The parent class of the Kernel Source browser, and grandparent of the Tapset browsers.
 * Contains code common to all three of those classes.
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView
 */
public abstract class BrowserView extends ViewPart {
    protected TreeViewer viewer;
    protected BrowserViewAction doubleClickAction;

    private CollapseAllHandler collapseHandler;
    private RefreshHandler refreshHandler;

    /**
     * Provides an interface for the TreeViewer to interact with the internal TreeNode data structure.
     * @author Ryan Morse
     *
     */
    static class ViewContentProvider implements ITreeContentProvider {
        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {}

        @Override
        public void dispose() {}

        @Override
        public Object[] getElements(Object parent) {
            return getChildren(parent);
        }

        @Override
        public Object getParent(Object child) {
            return null;
        }

        @Override
        public Object[] getChildren(Object par) {
            TreeNode parent = ((TreeNode)par);

            Object[] children = new Object[parent.getChildCount()];

            for(int i=0; i<children.length; i++) {
                children[i] = parent.getChildAt(i);
            }

            return children;
        }

        @Override
        public boolean hasChildren(Object parent) {
            return ((TreeNode)parent).getChildCount() > 0;
        }
    }

    protected abstract Image getEntryImage(TreeNode treeObj);

    /**
     * Provides the icon and text for each entry in the tapset tree.
     * @author Ryan Morse
     */
    protected class ViewLabelProvider extends LabelProvider {
        @Override
        public String getText(Object obj) {
            return obj.toString();
        }

        @Override
        public Image getImage(Object obj) {
            TreeNode treeObj = (TreeNode) obj;
            if (treeObj.toString().equals(Localization.getString("BrowserView.Loading")) //$NON-NLS-1$
             || treeObj.toString().equals(Localization.getString("BrowserView.TryRefresh"))) { //$NON-NLS-1$
                return null;
            }
            return getEntryImage(treeObj);
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        PatternFilter filter = new PatternFilter();
        FilteredTree filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
        viewer = filteredTree.getViewer();
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());

        IHandlerService handlerService = getSite().getService(IHandlerService.class);
        collapseHandler = new CollapseAllHandler(getViewer());
        handlerService.activateHandler(CollapseAllHandler.COMMAND_ID, collapseHandler);
        refreshHandler = new RefreshHandler();
        handlerService.activateHandler(RefreshHandler.COMMAND_ID, refreshHandler);
    }

    /**
     * Wires up all of the actions for this browser, such as double and right click handlers.
     */
    abstract void makeActions();

    protected void registerContextMenu(String menuName) {
        Control control = this.viewer.getControl();
        MenuManager manager = new MenuManager(menuName);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        Menu menu = manager.createContextMenu(control);
        viewer.getControl().setMenu(menu);

        IWorkbenchPartSite partSite = getSite();
        partSite.registerContextMenu(manager, viewer);
        partSite.setSelectionProvider(viewer);
    }

    public TreeViewer getViewer() {
        return viewer;
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (collapseHandler != null) {
            collapseHandler.dispose();
            collapseHandler = null;
        }
        if (refreshHandler != null) {
            refreshHandler.dispose();
            refreshHandler = null;
        }
        if (viewer != null) {
            if (doubleClickAction != null) {
                viewer.removeDoubleClickListener(doubleClickAction);
            }
            viewer = null;
        }
        if (doubleClickAction != null) {
            doubleClickAction.dispose();
            doubleClickAction = null;
        }
    }

    abstract void refresh();

    protected void displayLoadingMessage() {
        displayMessage(Localization.getString("BrowserView.Loading")); //$NON-NLS-1$
    }

    protected void displayMessage(String message) {
        TreeNode tree = new TreeNode(null, false);
        tree.add(new TreeNode(message, false));
        setViewerInput(tree);
    }

    protected void setViewerInput(final Object input) {
        if (viewer != null) {
            viewer.getControl().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.setInput(input);
                }
            });
        }
    }

    protected void setRefreshable(boolean state) {
        if (refreshHandler != null) {
            refreshHandler.setActive(state);
        }
    }
}
