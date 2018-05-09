/*******************************************************************************
 * Copyright (c) 2013, 2018 IBM Corporation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Rodrigo Fraxino De Araujo <rfaraujo@br.ibm.com>
 *     IBM Corporation - Wainer Santos Moschetta <wainersm@br.ibm.com>
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.launch;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Class to handle system's environment variables.
 *
 * @since 2.1
 */
public class RemoteEnvProxyManager extends RemoteProxyManager implements IRemoteEnvProxyManager {

    @Override
    public Map<String, String> getEnv(IProject project) throws CoreException {
        String scheme = mapping.getSchemeFromNature(project);
        if (scheme != null) {
            IRemoteEnvProxyManager manager = (IRemoteEnvProxyManager) getRemoteManager(scheme);
            return manager.getEnv(project);
        }
        URI projectURI = project.getLocationURI();
        return getEnv(projectURI);
    }

    @Override
    public Map<String, String> getEnv(URI uri) throws CoreException {
        String scheme = uri.getScheme();
        if (scheme != null && !scheme.equals(LOCALSCHEME)){
            IRemoteEnvProxyManager manager = (IRemoteEnvProxyManager) getRemoteManager(scheme);
            return manager.getEnv(uri);
        }
        return System.getenv();
    }

}
