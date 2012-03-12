package org.eclipse.linuxtools.oprofile.ui.view;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions.SessionManager;
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

	private void deleteSession(UiModelSession sess) {
		String sessionName = sess.getLabelText();
		String eventName = sess.getParent().getLabelText();
		File file = new File (SessionManager.OPXML_PREFIX + SessionManager.MODEL_DATA + eventName + sessionName);
		file.delete();
		try {
			SessionManager sessMan = new SessionManager(SessionManager.SESSION_LOCATION);
			sessMan.removeSession(sessionName, eventName);
			sessMan.write();
		} catch (FileNotFoundException e) {
			// intentionally left blank
			// the file will be created if it does not exist
		}
	}
}
