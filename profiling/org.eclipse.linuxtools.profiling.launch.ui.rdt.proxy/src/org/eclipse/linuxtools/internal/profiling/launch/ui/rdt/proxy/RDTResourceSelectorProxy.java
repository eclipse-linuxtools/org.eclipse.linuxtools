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

package org.eclipse.linuxtools.internal.profiling.launch.ui.rdt.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.profiling.launch.ui.IRemoteResourceSelectorProxy;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.ui.IRemoteUIConstants;
import org.eclipse.remote.ui.IRemoteUIFileService;
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
        IRemoteUIFileService uiFileService;
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

        IRemoteServicesManager sm = Activator.getService(IRemoteServicesManager.class);
        IRemoteConnectionType ct = sm.getConnectionType(uri);
        IRemoteConnection connection = ct.getConnection(uri);
        IRemoteFileService fileService = connection.getService(IRemoteFileService.class);

        // If the user is switching schemes, start with an empty host and path
        uiFileService = ct.getService(IRemoteUIFileService.class);
        uiFileService.showConnections(true);
        if (!schemeSwitch) {
            uiFileService.setConnection(connection);
        }
        String selectedPath = null;
        switch (resourceType) {
        case FILE:
            selectedPath = uiFileService.browseFile(shell, prompt, uri.getPath(), IRemoteUIConstants.NONE);
            break;
        case DIRECTORY:
            selectedPath = uiFileService.browseDirectory(shell, prompt, uri.getPath(), IRemoteUIConstants.NONE);
            break;
        default:
            Activator.log(IStatus.ERROR, Messages.RDTResourceSelectorProxy_unsupported_resourceType + resourceType);
            return null;
        }
        URI selectedURI = null;
        if (selectedPath != null) {
            connection = uiFileService.getConnection();
            fileService = connection.getService(IRemoteFileService.class);
            selectedURI = fileService.toURI(selectedPath);
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
