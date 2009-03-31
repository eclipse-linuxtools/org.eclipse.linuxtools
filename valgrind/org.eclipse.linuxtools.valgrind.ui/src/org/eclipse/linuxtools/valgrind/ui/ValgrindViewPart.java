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
package org.eclipse.linuxtools.valgrind.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;

public class ValgrindViewPart extends ViewPart {

	protected Composite dynamicViewHolder;
	protected IValgrindToolView dynamicView;
	protected ActionContributionItem[] dynamicActions;

	@Override
	public void createPartControl(Composite parent) {
		setContentDescription(Messages.getString("ValgrindViewPart.No_Valgrind_output")); //$NON-NLS-1$

		dynamicViewHolder = new Composite(parent, SWT.NONE);
		GridLayout dynamicViewLayout = new GridLayout();
		dynamicViewLayout.marginWidth = dynamicViewLayout.marginHeight = 0;
		dynamicViewHolder.setLayout(dynamicViewLayout);
		dynamicViewHolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		ValgrindUIPlugin.getDefault().setView(this);
	}

	public IValgrindToolView createDynamicContent(String description, String toolID) throws CoreException {
		setContentDescription(description);

		// remove tool specific toolbar controls
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		if (dynamicActions != null) {
			for (ActionContributionItem item : dynamicActions) {
				toolbar.remove(item);
			}
		}

		// remove old view controls
		if (dynamicView != null) {
			dynamicView.dispose();
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
					toolbar.appendToGroup(ValgrindUIPlugin.TOOLBAR_LOC_GROUP_ID, dynamicActions[i]);
				}
			}
		}
		else {
			dynamicView = null;
		}

		toolbar.update(true);

		dynamicViewHolder.layout(true);

		return dynamicView;
	}

	@Override
	public void setFocus() {
		if (dynamicView != null) {
			dynamicView.setFocus();
		}
	}

	public void refreshView() {
		if (dynamicView != null) {
			dynamicView.refreshView();
		}
	}

	@Override
	public void dispose() {
		if (dynamicView != null) {
			dynamicView.dispose();
		}
		super.dispose();
	}

	public IValgrindToolView getDynamicView() {
		return dynamicView;
	}

}
