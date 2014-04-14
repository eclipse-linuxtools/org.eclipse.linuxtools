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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;

/**
 * Handler for saving a perf statistics session.
 */
public class PerfSaveStatsHandler extends AbstractSaveDataHandler {

	public static String DATA_EXT = "stat"; //$NON-NLS-1$

	@Override
	public IPath saveData(String filename) {
		IPath newDataLoc = getNewDataLocation(filename, DATA_EXT);
		IPerfData statData = PerfPlugin.getDefault().getStatData();
		BufferedWriter writer = null;
		OutputStreamWriter osw = null;
		URI newDataLocURI = null;

		try {
			IRemoteFileProxy proxy = null;
			newDataLocURI = new URI(newDataLoc.toPortableString());
			proxy = RemoteProxyManager.getInstance().getFileProxy(newDataLocURI);
			IFileStore newDataFileStore = proxy.getResource(newDataLocURI.getPath());
			osw = new OutputStreamWriter(newDataFileStore.openOutputStream(EFS.NONE, null));
			writer = new BufferedWriter(osw);
			writer.write(statData.getPerfData());
			IFileInfo info = newDataFileStore.fetchInfo();
			info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
			newDataFileStore.putInfo(info, EFS.SET_ATTRIBUTES, null);
			return newDataLoc;
		} catch (IOException|CoreException|URISyntaxException e) {
			openErroDialog(Messages.PerfSaveStat_error_title,
					Messages.PerfSaveStat_error_msg,
					newDataLoc.lastSegment());
		} finally {
			closeResource(writer);
		}
		return null;
	}

	@Override
	public boolean verifyData() {
		IPerfData statData = PerfPlugin.getDefault().getStatData();
		return statData != null && statData.getPerfData() != null;
	}

}
