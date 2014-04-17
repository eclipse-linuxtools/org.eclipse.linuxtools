/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Rodrigo Fraxino De Araujo <rfaraujo@br.ibm.com>
 *     Red Hat Inc. - Alexander Kurtakov<akurtako@redhat.com>
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.launch;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface to handle system's environment variables.
 *
 * @since 2.1
 */
public interface IRemoteEnvProxyManager extends IRemoteProxyManager {
    /**
     * Method to get system's environment variables.
     *
     * @param project
     *            The project to get env for.
     * @return Mapping of environment variables
     * @throws CoreException If an exception occurred.
     * @since 2.1
     */
    Map<String, String> getEnv(IProject project) throws CoreException;

    /**
     * Method to get system's environment variables.
     *
     * @param uri
     *            The uri to get env for.
     * @return Mapping of environment variables
     * @throws CoreException If an exception occurred.
     * @since 2.1
     */
    Map<String, String> getEnv(URI uri) throws CoreException;
}
