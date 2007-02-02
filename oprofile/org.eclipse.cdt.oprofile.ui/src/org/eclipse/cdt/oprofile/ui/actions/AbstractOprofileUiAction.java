/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.ui.actions;

import org.eclipse.cdt.oprofile.ui.OprofilePlugin;
import org.eclipse.cdt.oprofile.ui.OprofileUIMessages;
import org.eclipse.cdt.oprofile.ui.system.SystemProfileView;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


/**
 * Generic baseclass for oprofile ui actions. All implementing classes must define the "run" method.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public abstract class AbstractOprofileUiAction implements IWorkbenchWindowActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	abstract public void run(IAction action);
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	/**
	 * Updates the oprofile perspective views
	 */
	protected void _updateViews() {
		SystemProfileView view = OprofilePlugin.getDefault().getSystemProfileView();
		if (view != null) {
			view.refreshView();
		}
	}
	
	/**
	 * Displays an error dialog
	 * @param shell the parent shell to use (or <code>null</code>)
	 * @param key the properties key from which to derive title and message to display (key + ".error.dialog.title/message")
	 * @param except the CoreException from the error
	 */
	protected void _showErrorDialog(Shell shell, String key, CoreException except) {
		String title = OprofileUIMessages.getString(key + ".error.dialog.title"); //$NON-NLS-1$
		String msg = OprofileUIMessages.getString(key + ".error.dialog.message"); //$NON-NLS-1$
		ErrorDialog.openError(shell, title, msg, except.getStatus());
	}
}
