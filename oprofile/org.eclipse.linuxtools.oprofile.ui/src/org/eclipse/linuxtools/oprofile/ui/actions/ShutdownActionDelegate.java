/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;

/**
 * ActionDelegate for causing oprofiled to shutdown.
 */
public class ShutdownActionDelegate extends AbstractOprofileUiAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();
//			_updateViews();  //needed? have other functions to refresh views
		} catch (OpcontrolException oe) {
			_showErrorDialog(null /* parent shell */, "opcontrolProvider", oe); //$NON-NLS-1$
		}
	}
}
