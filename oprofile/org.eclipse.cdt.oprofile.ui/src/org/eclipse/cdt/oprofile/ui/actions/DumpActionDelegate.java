/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.ui.actions;

import org.eclipse.cdt.oprofile.core.OpcontrolException;
import org.eclipse.cdt.oprofile.core.OprofileCorePlugin;
import org.eclipse.jface.action.IAction;

/**
 * ActionDelegate for causing oprofiled to dump current profiling data.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class DumpActionDelegate extends AbstractOprofileUiAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.ui.actions.AbstractOprofileUiAction#runAction(org.eclipse.jface.action.IAction)
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
