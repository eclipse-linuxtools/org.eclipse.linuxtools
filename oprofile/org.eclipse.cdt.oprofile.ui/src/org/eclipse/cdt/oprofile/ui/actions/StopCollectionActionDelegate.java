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
 * Action to cause Oprofiled to stop collecting data (without shutting down oprofiled)
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class StopCollectionActionDelegate extends AbstractOprofileUiAction {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			OprofileCorePlugin.getDefault().getOpcontrolProvider().stopCollection();
			_updateViews();
		} catch (OpcontrolException oe) {
			_showErrorDialog(null /* parent shell */, "opcontrolProvider", oe); //$NON-NLS-1$
		}
	}
}
