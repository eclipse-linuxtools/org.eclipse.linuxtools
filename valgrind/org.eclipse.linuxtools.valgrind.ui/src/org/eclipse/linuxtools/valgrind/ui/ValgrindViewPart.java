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

	@Override
	public void createPartControl(Composite parent) {
		dynamicViewHolder = new Composite(parent, SWT.NONE);
		dynamicViewHolder.setLayout(new GridLayout());
		dynamicViewHolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		ValgrindUIPlugin.getDefault().setView(this);
	}

	public void createDynamicView(String toolID) throws CoreException {
		for (Control child : dynamicViewHolder.getChildren()) {
			child.dispose();
		}
		dynamicView = ValgrindUIPlugin.getDefault().getToolView(toolID);
		dynamicView.createPartControl(dynamicViewHolder);
		
		dynamicViewHolder.layout(true);
	}

	@Override
	public void setFocus() {
		if (dynamicView != null) {
			dynamicView.setFocus();
		}
	}

	public void refreshView() {
		// remove tool specific toolbar controls
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.removeAll();
		toolbar.update(true);
		
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
