package org.eclipse.linuxtools.oprofile.ui.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSession;

public class OprofileViewDeleteSessionAction extends Action {
	
	private TreeViewer treeViewer;

	public OprofileViewDeleteSessionAction(TreeViewer tree) {
		super("Delete Session"); //$NON-NLS-1$
		treeViewer = tree;
	}

	@Override
	public void run() {
		TreeSelection tsl = (TreeSelection) treeViewer.getSelection();
		if (tsl.getFirstElement() instanceof UiModelSession) {
			UiModelSession sess = (UiModelSession) tsl.getFirstElement();
			deleteSession(sess);
		}

		OprofileUiPlugin.getDefault().getOprofileView().refreshView();
	}

	/**
	 * Delete the session with the specified name for the specified event
	 * @param sessionName The name of the session to delete
	 * @param eventName The name of the event containing the session
	 */
	private void deleteSession(UiModelSession sess) {
		String sessionName = sess.getLabelText();
		String eventName = sess.getParent().getLabelText();
		try {
			OprofileCorePlugin.getDefault().getOpcontrolProvider().deleteSession(sessionName, eventName);
		} catch (OpcontrolException e) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", e); //$NON-NLS-1$
		}
	}
}
