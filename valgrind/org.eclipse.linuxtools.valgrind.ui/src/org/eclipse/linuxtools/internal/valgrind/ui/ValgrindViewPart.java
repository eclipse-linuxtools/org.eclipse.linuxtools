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
package org.eclipse.linuxtools.internal.valgrind.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindInfo;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

public class ValgrindViewPart extends ViewPart {
    private static final String TOOLBAR_LOC_GROUP_ID = "toolbarLocal"; //$NON-NLS-1$
    private PageBook pageBook;
    private Composite dynamicViewHolder;
    private IValgrindToolView dynamicView;
    private ActionContributionItem[] dynamicActions;
    private IValgrindMessage[] messages;
    private CoreMessagesViewer messagesViewer;
    private Action showCoreAction;
    private Action showToolAction;
    private boolean hasDynamicContent = false;

    @Override
    public void createPartControl(Composite parent) {
        setContentDescription(Messages.getString("ValgrindViewPart.No_Valgrind_output")); //$NON-NLS-1$

        pageBook = new PageBook(parent, SWT.NONE);
        pageBook.setLayoutData(new GridData(GridData.FILL_BOTH));

        messagesViewer = new CoreMessagesViewer(pageBook, SWT.NONE);

        dynamicViewHolder = new Composite(pageBook, SWT.NONE);
        GridLayout dynamicViewLayout = new GridLayout();
        dynamicViewLayout.marginWidth = dynamicViewLayout.marginHeight = 0;
        dynamicViewHolder.setLayout(dynamicViewLayout);
        dynamicViewHolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        showCoreAction = new Action(Messages.getString("ValgrindViewPart.Show_Core_Action"), IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
            @Override
            public void run() {
                showCorePage();
            }
        };
        showToolAction = new Action(Messages.getString("ValgrindViewPart.Show_Tool_Action"), IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
            @Override
            public void run() {
                showToolPage();
            }
        };

        ValgrindUIPlugin.getDefault().setView(this);
    }

    public IValgrindToolView createDynamicContent(String description, String toolID) throws CoreException {
        setContentDescription(description);

        // remove tool specific toolbar controls
        IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        ToolBar tb = ((ToolBarManager) toolbar).getControl();
        if (tb == null || tb.isDisposed()) {
            throw new CoreException(new Status(IStatus.ERROR, ValgrindUIPlugin.PLUGIN_ID, "Toolbar is disposed")); //$NON-NLS-1$
        }

        if (dynamicActions != null) {
            for (ActionContributionItem item : dynamicActions) {
                toolbar.remove(item);
            }
        }

        // remove old view controls
        if (dynamicView != null) {
            dynamicView.dispose();
        }

        // remove old messages
        if (messages != null){
            messagesViewer.getTreeViewer().setInput(null);
            messages = null;
        }

        for (Control child : dynamicViewHolder.getChildren()) {
            if (!child.isDisposed()) {
                child.dispose();
            }
        }

        if (toolID != null) {
            dynamicView = ValgrindUIPlugin.getDefault().getToolView(toolID);
            dynamicView.createPartControl(dynamicViewHolder);

            // create toolbar items
            IAction[] actions = dynamicView.getToolbarActions();
            if (actions != null) {
                dynamicActions = new ActionContributionItem[actions.length];
                for (int i = 0; i < actions.length; i++) {
                    dynamicActions[i] = new ActionContributionItem(actions[i]);
                    toolbar.appendToGroup(TOOLBAR_LOC_GROUP_ID, dynamicActions[i]);
                }
            }
        } else {
            dynamicView = null;
        }

        // remove old menu items
        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        menu.removeAll();
        // was content was created?
        hasDynamicContent = dynamicViewHolder.getChildren().length > 0;
        if (hasDynamicContent) {
            menu.add(showCoreAction);
            menu.add(showToolAction);
        }

        menu.update(true);
        toolbar.update(true);
        // Update to notify the workbench items have been changed
        getViewSite().getActionBars().updateActionBars();
        dynamicViewHolder.layout(true);

        return dynamicView;
    }

    public void setMessages(IValgrindMessage[] messages) {
        this.messages = messages;
    }

    public IValgrindMessage[] getMessages() {
        return messages;
    }

    @Override
    public void setFocus() {
        if (dynamicView != null) {
            dynamicView.setFocus();
        }
    }

    public void refreshView() {
        if (messages != null && messages.length > 0) {
            messagesViewer.getTreeViewer().setInput(messages);

            // decide which page to show
            if (hasDynamicContent && messages[0] instanceof ValgrindInfo) {
                // no valgrind messages to show
                showCoreAction.setEnabled(false);
                showToolPage();
            }
            else {
                showCoreAction.setEnabled(true);
                showCorePage();
            }
        }
        if (dynamicView != null) {
            dynamicView.refreshView();
        }
    }

    @Override
    public void dispose() {
        if (dynamicView != null) {
            dynamicView.dispose();
        }

        // Unset this view in the UI plugin
        ValgrindUIPlugin.getDefault().setView(null);

        super.dispose();
    }

    public IValgrindToolView getDynamicView() {
        return dynamicView;
    }

    public CoreMessagesViewer getMessagesViewer() {
        return messagesViewer;
    }

    private void showCorePage() {
        pageBook.showPage(messagesViewer.getTreeViewer().getControl());
        showCoreAction.setChecked(true);
        showToolAction.setChecked(false);
    }

    private void showToolPage() {
        pageBook.showPage(dynamicViewHolder);
        showToolAction.setChecked(true);
        showCoreAction.setChecked(false);
    }

}
