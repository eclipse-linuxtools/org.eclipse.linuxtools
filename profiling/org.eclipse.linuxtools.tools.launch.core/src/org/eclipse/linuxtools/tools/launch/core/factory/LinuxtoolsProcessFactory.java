/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tools.launch.core.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.profiling.launch.RemoteEnvProxyManager;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;

/*
 * Abstract class with useful functions for ProcessFactory classes.
 */
public abstract class LinuxtoolsProcessFactory {
    private static final String PATH = "PATH"; //$NON-NLS-1$
    private static final String PATH_EQUAL = "PATH="; //$NON-NLS-1$
    private static final String SEPARATOR = ":"; //$NON-NLS-1$

    private String getEnvpPath(String[] envp) {
        if (envp == null) {
            return null;
        }
        for (String env : envp) {
            if (env.startsWith(PATH_EQUAL)) {
                return env.substring(PATH_EQUAL.length());
            }
        }
        return null;
    }

    /*
     * Update the environment variables list with system environment variables
     * and the Linux tools path project property (if applicable), and prepend
     * the latter to the PATH env in the list. Call this function if the
     * command to be run may be in the path selected in 'Linux tools path'
     * project property page.
     * */
    protected String[] updateEnvironment(String[] envp, IProject project) {
        String ltPath = LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project);
        String envpPath = getEnvpPath(envp);

        Map<String, String> envMap = new HashMap<>();
        if (project != null) {
            try {
                envMap.putAll(RemoteEnvProxyManager.class.newInstance().getEnv(project));
            } catch (InstantiationException|IllegalAccessException|CoreException e) {
                e.printStackTrace();
            }
        }
        String systemPath;
        if (!envMap.isEmpty()) {
            systemPath = envMap.get(PATH);
            if (systemPath == null) {
                systemPath = System.getenv(PATH);
                if (systemPath != null) {
                    envMap.put(PATH, systemPath);
                }
            }
        } else {
            envMap.putAll(System.getenv());
            systemPath = envMap.get(PATH);
        }

        StringBuffer newPath = new StringBuffer();
        newPath.append(PATH_EQUAL);

        if (ltPath != null && !ltPath.isEmpty()) {
            newPath.append(ltPath);
            newPath.append(SEPARATOR);
        }
        if (envpPath != null && !envpPath.isEmpty()) {
            newPath.append(envpPath);
            newPath.append(SEPARATOR);
        }
        if (systemPath != null && !systemPath.isEmpty()) {
            newPath.append(systemPath);
        }

        // Overwrite/update map of system environment variables with ones from the provided array
        if (newPath.length() > PATH_EQUAL.length()) {
            envMap.put(PATH, newPath.substring(PATH_EQUAL.length()));
        }
        if (envp != null) {
            for (String var : envp) {
                if (!var.startsWith(PATH_EQUAL)) {
                    int splitIndex = var.indexOf('=');
                    envMap.put(var.substring(0, splitIndex), var.substring(splitIndex + 1));
                }
            }
        }
        Set<String> keySet = envMap.keySet();
        String[] newEnvp = new String[keySet.size()];
        int i = 0;
        for (String key : keySet) {
            if (!key.startsWith(PATH_EQUAL)) {
                newEnvp[i] = key + "=" + envMap.get(key); //$NON-NLS-1$
            }
            i++;
        }

        return newEnvp;
    }
}
