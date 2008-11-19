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
package org.eclipse.linuxtools.oprofile.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.oprofile.core.model.OpModelRoot;

/**
 * ActionDelegate to refresh the System view without running any oprofile commands.
 */
public class RefreshActionDelegate extends AbstractOprofileUiAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		//refresh System Profile View
//		_updateViews();
		
		OpModelRoot r = OpModelRoot.getDefault();
	
		r.refreshModel();
		System.out.println(r);
	}
}
