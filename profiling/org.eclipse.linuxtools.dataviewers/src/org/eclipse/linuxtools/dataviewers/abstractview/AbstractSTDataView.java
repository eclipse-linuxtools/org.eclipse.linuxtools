/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.dataviewers.abstractview;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.actions.STCollapseAllTreeAction;
import org.eclipse.linuxtools.dataviewers.actions.STCollapseSelectionAction;
import org.eclipse.linuxtools.dataviewers.actions.STCopyAction;
import org.eclipse.linuxtools.dataviewers.actions.STDataViewersSortAction;
import org.eclipse.linuxtools.dataviewers.actions.STExpandAllTreeAction;
import org.eclipse.linuxtools.dataviewers.actions.STExpandSelectionAction;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.linuxtools.dataviewers.actions.STHideShowColAction;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

/**
 * The AbstractSTDataView is a view that generically implements data result views.
 */
public abstract class AbstractSTDataView extends ViewPart {

    private AbstractSTViewer stViewer = null;

    private IAction sortAction = null;

    private IAction preferencesAction = null;

    private IAction hideShowColAction = null;

    private IAction expandAllAction = null;

    private IAction collapseAllAction = null;

    private IAction expandSelectionAction = null;

    private IAction collapseSelectionAction = null;

    private IAction exportToCSVAction = null;

    private IAction copyToAction = null;

    /**
     * Create optional arbitrary Controls, on top of viewer. Does nothing by default. Can be used to display a title, or
     * some other informations.
     *
     * @param parent The parent composite, with a gridlayout (1 column).
     */
    protected abstract void createTitle(Composite parent) ;

    /**
     * Creates a wrapper handling a TreeTable or Table Viewer
     *
     * <p>
     * Subclasses may override it.
     * </p>
     *
     * @param parent The parent composite.
     * @return An AbstractSTViewer
     */
    protected abstract AbstractSTViewer createAbstractSTViewer(Composite parent);

    /**
     * Add actions to the toolbar (top-right toolbar)
     * <p>
     * Subclasses may override it.
     * </p>
     *
     * @param manager The IToolBarManager to contribute to.
     */
    protected abstract void contributeToToolbar(IToolBarManager manager);

    @Override
    public void createPartControl(Composite parent) {
        GridLayout gridLayout = new GridLayout(1, true);
        parent.setLayout(gridLayout);
        createTitle(parent);
        stViewer = createAbstractSTViewer(parent);

        // create the actions before the input is set on the viewer but after
        // the sorter and filter are set so the actions will be enabled correctly.
        createActions();

        final MenuManager mgr = initContextMenu();
        final Menu menu = mgr.createContextMenu(parent);
        stViewer.getViewer().getControl().setMenu(menu);

        getSite().registerContextMenu(mgr, stViewer.getViewer());

        // the selection provider registered
        getSite().setSelectionProvider(stViewer.getViewer());

        IActionBars actionBars = getViewSite().getActionBars();

        initMenu(actionBars.getMenuManager());
        initToolBar(actionBars.getToolBarManager());

    }

    /**
     * Creates the actions for the receiver.
     */
    protected void createActions() {
        // menu bar
        expandAllAction = createExpandAllAction();
        collapseAllAction = createCollapseAllAction();

        sortAction = new STDataViewersSortAction(getSTViewer());

        hideShowColAction = new STHideShowColAction(getSTViewer());

        exportToCSVAction = createExportToCSVAction();

        // context menu (right-click)
        expandSelectionAction = createExpandSelectionAction();
        collapseSelectionAction = createCollapseSelectionAction();
        copyToAction = new STCopyAction(getSTViewer());
    }


    /**
     * Creates the export To CSV actions.
     *
     * @return IAction The newly created action.
     */
    protected IAction createExportToCSVAction() {
        return new STExportToCSVAction(getSTViewer());
    }

    /**
     * Creates the collapse selection actions.
     *
     * @return IAction The newly created action.
     */
    private IAction createCollapseSelectionAction() {
        if (getSTViewer() instanceof AbstractSTTreeViewer stTreeViewer) {
            return new STCollapseSelectionAction(stTreeViewer);
        }
        return null;
    }

    /**
     * Creates the expand selection actions.
     *
     * @return IAction The newly created action.
     */
    private IAction createExpandSelectionAction() {
        if (getSTViewer() instanceof AbstractSTTreeViewer stTreeViewer) {
            return new STExpandSelectionAction(stTreeViewer);
        }
        return null;
    }

    /**
     * Creates the collapse all action.
     *
     * @return IAction The newly created action.
     */
    private IAction createCollapseAllAction() {
        if (getSTViewer() instanceof AbstractSTTreeViewer stTreeViewer) {
            return new STCollapseAllTreeAction(stTreeViewer);
        }
        return null;
    }

    /**
     * Creates the expand all action.
     *
     * @return IAction The newly created action.
     */
    private IAction createExpandAllAction() {
        if (getSTViewer() instanceof AbstractSTTreeViewer stTreeViewer) {
            return new STExpandAllTreeAction(stTreeViewer);
        }
        return null;
    }

    /**
     * Init the context menu
     * <p>
     * If you intend to add an action in the context menu you may override the <code>fillContextMenu</code> method.
     * </p>
     *
     * @see #fillContextMenu(IMenuManager manager)
     */
    private MenuManager initContextMenu() {
        MenuManager mgr = new MenuManager();
        mgr.setRemoveAllWhenShown(true);
        mgr.addMenuListener(mgr1 -> {
		    getSTViewer().getViewer().cancelEditing();
		    fillContextMenu(mgr1);
		});
        return mgr;
    }

    /**
     * Init the toolbar for the receiver
     * <p>
     * You can override this method if you want - but remember to call <code>super.initToolBar()</code>.
     * </p>
     *
     * @param manager The tool bar manager of this view.
     */
    private void initToolBar(IToolBarManager manager) {
        if (expandAllAction != null) {
            manager.add(expandAllAction);
        }
        if (collapseAllAction != null) {
            manager.add(collapseAllAction);
        }
        if (hideShowColAction != null) {
            manager.add(hideShowColAction);
        }
        if (exportToCSVAction != null) {
            manager.add(exportToCSVAction);
        }
        if (sortAction != null) {
            manager.add(sortAction);
        }
        contributeToToolbar(manager);
        manager.update(true);
    }

    /**
     * Init the menu for the receiver.
     *
     * @param menu The IMenuManager to add to.
     */
    private void initMenu(IMenuManager menu) {
        if (preferencesAction != null) {
            menu.add(preferencesAction);
        }
    }

    @Override
    public void setFocus() {
        Viewer viewer = getSTViewer().getViewer();
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    /**
     * Return the viewer.
     *
     * @return AbstractSTViewer
     */
    public AbstractSTViewer getSTViewer() {
        return stViewer;
    }

    /**
     * Shortcut for getViewer().setInput(input).
     *
     * @param input The viewer input.
     * @see org.eclipse.jface.viewers.TreeViewer#setInput(Object)
     */
    public void setInput(Object input) {
        stViewer.getViewer().setInput(input);
    }

    /**
     * Fills the context menu (mouse right-click)
     * <p>
     * Subclasses may extend it. don't forget to call <code>super.fillContextMenu(...)</code>
     * </p>
     *
     * @param manager The IMenuManager to fill.
     * @since 6.0
     */
    protected void fillContextMenu(IMenuManager manager) {
        Control control = stViewer.getViewer().getControl();
        if (control instanceof Tree tree) {
            TreeItem[] selection = tree.getSelection();
            if (selection != null && selection.length > 0) {
                if (collapseSelectionAction != null) {
                    manager.add(collapseSelectionAction);
                }
                if (expandSelectionAction != null) {
                    manager.add(expandSelectionAction);
                }
                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                if (copyToAction != null) {
                    manager.add(copyToAction);
                }
            }
        }
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
}
