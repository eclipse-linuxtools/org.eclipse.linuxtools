/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.profiling.launch.ui.rdt.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.profiling.launch.ui.IRemoteResourceSelectorProxy;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.swt.widgets.Shell;

public class RDTResourceSelectorProxy implements IRemoteResourceSelectorProxy {

	private enum ResourceType { FILE, DIRECTORY }

	private URI getEmptyPathURI(String scheme) {
		try {
			return new URI(scheme, null, "/", null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			Activator.log(IStatus.ERROR, Messages.RDTResourceSelectorProxy_URI_syntax_error, e);
			return null;
		}
	}

	private URI selectResource(String scheme, String initialPath, String prompt, Shell shell, ResourceType resourceType) {
		IRemoteUIFileManager uiFileManager;
		boolean schemeSwitch = false;
		URI uri;
		try {
			uri = new URI(initialPath);
			if (!scheme.equals(uri.getScheme())) {
				uri = getEmptyPathURI(scheme);
				schemeSwitch = true;
			}
		} catch (URISyntaxException e) {
			uri = getEmptyPathURI(scheme);
			schemeSwitch = true;
		}
		// If the user is switching schemes, start with an empty host and path
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(uri);
		services.initialize();

		IRemoteUIServices uiServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(services);
		uiFileManager = uiServices.getUIFileManager();
		uiFileManager.showConnections(true);
		IRemoteConnection connection = null;
		if (!schemeSwitch) {
			connection = services.getConnectionManager().getConnection(uri);
			uiFileManager.setConnection(connection);
		}
		String selectedPath = null;
		switch (resourceType) {
		case FILE:
			selectedPath = uiFileManager.browseFile(shell, prompt, uri.getPath(), IRemoteUIConstants.NONE);
			break;
		case DIRECTORY:
			selectedPath = uiFileManager.browseDirectory(shell, prompt, uri.getPath(), IRemoteUIConstants.NONE);
			break;
		default:
			Activator.log(IStatus.ERROR, Messages.RDTResourceSelectorProxy_unsupported_resourceType + resourceType);
			return null;
		}
		URI selectedURI = null;
		if (selectedPath != null) {
			connection = uiFileManager.getConnection();
			IRemoteFileManager remoteFileManager = services.getFileManager(connection);
			selectedURI = remoteFileManager.toURI(selectedPath);
		}
		return selectedURI;
	}

	@Override
	public URI selectFile(String scheme, String initialPath, String prompt, Shell shell) {
		return selectResource(scheme, initialPath, prompt, shell, ResourceType.FILE);
	}

	@Override
	public URI selectDirectory(String scheme, String initialPath, String prompt, Shell shell) {
		return selectResource(scheme, initialPath, prompt, shell, ResourceType.DIRECTORY);
	}

}
