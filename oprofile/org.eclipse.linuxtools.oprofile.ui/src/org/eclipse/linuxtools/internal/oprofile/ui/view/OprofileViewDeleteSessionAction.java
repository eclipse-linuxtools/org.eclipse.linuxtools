/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSession;

public class OprofileViewDeleteSessionAction extends Action {

	private TreeViewer treeViewer;

	public OprofileViewDeleteSessionAction(TreeViewer tree) {
		super("Delete Session"); //$NON-NLS-1$
		treeViewer = tree;
		setEnabled(false);
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
		IUiModelElement[] modelEvents = sess.getChildren();
		try {
			for (int i = 0; i < modelEvents.length; i++) {
				OprofileCorePlugin
						.getDefault()
						.getOpcontrolProvider()
						.deleteSession(sessionName,
								modelEvents[i].getLabelText());
			}
			// clear out collected data by this session
			// check if profile is done through operf or oprofile
			if (Oprofile.OprofileProject.getProfilingBinary().equals(
					Oprofile.OprofileProject.OPERF_BINARY)) {
				// delete operf_data folder
				deleteOperfDataFolder(Oprofile.OprofileProject.getProject()
						.getFolder(Oprofile.OprofileProject.OPERF_DATA));
			} else {
				OprofileCorePlugin.getDefault().getOpcontrolProvider().reset();
			}
		} catch (OpcontrolException e) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", e); //$NON-NLS-1$
		}
	}

	public static void deleteOperfDataFolder(IFolder operfData)	{
		if(operfData.exists()) {
			try {
				operfData.delete(true,null);
			} catch (CoreException e) {
				OprofileCorePlugin.showErrorDialog("opcontrolProvider", e); //$NON-NLS-1$
			}
		}

	}
}
