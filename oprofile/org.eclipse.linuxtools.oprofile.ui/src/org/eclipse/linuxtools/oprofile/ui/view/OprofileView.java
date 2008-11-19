/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.linuxtools.oprofile.core.model.OpModelRoot;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class OprofileView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		
		_createActionMenu();

		OprofileUiPlugin.getDefault().setOprofileView(this);
	}

	private void _createActionMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		manager.add(new Action("Refresh Model"){
			public void run() {
				OpModelRoot r = OpModelRoot.getDefault();
				r.refreshModel();
				System.out.println(r);
			}
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

}
