/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;

/**
 * ActionDelegate for causing oprofiled to dump current profiling data.
 */
public class DumpActionDelegate extends AbstractOprofileUiAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.oprofile.ui.actions.AbstractOprofileUiAction#runAction(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		
		try {
			OprofileCorePlugin.getDefault().getOpcontrolProvider().dumpSamples();
			_updateViews();
		} catch (OpcontrolException oe) {
			_showErrorDialog(null /* parent shell */, "opcontrolProvider", oe); //$NON-NLS-1$
		}
	}
}
