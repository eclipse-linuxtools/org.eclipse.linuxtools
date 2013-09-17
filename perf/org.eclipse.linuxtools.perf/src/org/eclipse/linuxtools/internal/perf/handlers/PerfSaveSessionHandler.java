/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.handlers;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.ui.PerfProfileView;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for saving a perf profile session.
 */
public class PerfSaveSessionHandler extends AbstractSaveDataHandler {

	public static final String DATA_EXT = "data"; //$NON-NLS-1$

	@Override
	public IPath saveData(String filename) {
		// get paths
		IPath newDataLoc = getNewDataLocation(filename, DATA_EXT);
		IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();
		URI newDataLocURI = null;
		URI defaultDataLocURI = null;
		// get files
		IRemoteFileProxy proxy = null;
		try {
			newDataLocURI = new URI(newDataLoc.toPortableString());
			defaultDataLocURI = new URI(defaultDataLoc.toPortableString());
			proxy = RemoteProxyManager.getInstance().getFileProxy(newDataLocURI);
		} catch (URISyntaxException e) {
			openErroDialog(Messages.MsgProxyError,
					Messages.MsgProxyError,
					newDataLoc.lastSegment());
		} catch (CoreException e) {
			openErroDialog(Messages.MsgProxyError,
					Messages.MsgProxyError,
					newDataLoc.lastSegment());
		}
		IFileStore newDataFileStore = proxy.getResource(newDataLocURI.getPath());
		IFileStore defaultDataFileStore = proxy.getResource(defaultDataLocURI.getPath());

		if (canSave(newDataLoc)) {
			// copy default data into new location
			try {
				defaultDataFileStore.copy(newDataFileStore, EFS.OVERWRITE, null);
				PerfPlugin.getDefault().setPerfProfileData(newDataLoc);
				try {
					PerfProfileView view = (PerfProfileView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(PerfPlugin.VIEW_ID);
					view.setContentDescription(newDataLoc.toOSString());
				} catch (PartInitException e) {
					// fail silently
				}
				IFileInfo info = newDataFileStore.fetchInfo();
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
				newDataFileStore.putInfo(info, EFS.SET_ATTRIBUTES, null);
				return newDataLoc;
			} catch (CoreException e) {
				openErroDialog(Messages.PerfSaveSession_failure_title,
						Messages.PerfSaveSession_failure_msg,
						newDataLoc.lastSegment());
			}
		}
		return null;
	}

	@Override
	public boolean verifyData() {
		IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();
		return defaultDataLoc != null && !defaultDataLoc.isEmpty();
	}
}
