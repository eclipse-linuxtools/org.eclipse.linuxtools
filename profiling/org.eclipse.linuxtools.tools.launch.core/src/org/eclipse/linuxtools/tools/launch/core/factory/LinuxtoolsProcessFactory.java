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

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.profiling.launch.RemoteEnvProxyManager;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;

/*
 * Abstract class with usefull functions for ProcessFactory classes.
 */
public abstract class LinuxtoolsProcessFactory {
	private static final String PATH = "PATH"; //$NON-NLS-1$
	private static final String PATH_EQUAL = "PATH="; //$NON-NLS-1$
	private static final String SEPARATOR = ":"; //$NON-NLS-1$

	private String getEnvpPath(String[] envp) {
		for (String env:envp)
			if (env.startsWith(PATH_EQUAL))
				return env.substring(PATH_EQUAL.length());
		return null;
	}

	/*
	 * Update the environment variables list with the Linux tools path project
	 * property, prepending it to the PATH env. Call this function if the
	 * command to be run may be in the path selected in 'Linux tools path'
	 * project property page.
	 * */
	protected String[] updateEnvironment(String[] envp, IProject project) {
		if (project == null) {
			return envp;
		}
		if (envp == null) {
			envp = new String[0];
		}
		String ltPath = LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project);
		String envpPath = getEnvpPath(envp);
		String systemPath = null;
		Map<String, String> systemEnvMap = null;
		try {
			systemEnvMap = RemoteEnvProxyManager.class.newInstance().getEnv(project);
			systemPath = systemEnvMap.get(PATH);
			if (systemPath==null) {
				systemPath = System.getenv(PATH);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		StringBuffer newPath = new StringBuffer();
		newPath.append(PATH_EQUAL);

		if (ltPath != null && ltPath.length() > 0) {
			newPath.append(ltPath);
			newPath.append(SEPARATOR);
		}
		if (envpPath != null && envpPath.length() > 0) {
			newPath.append(envpPath);
			newPath.append(SEPARATOR);
		}
		if (systemPath != null && systemPath.length() > 0) {
			newPath.append(systemPath);
			newPath.append(SEPARATOR);
		}

		if (newPath.length() == PATH_EQUAL.length())
			//there is nothing to add
			return envp;

		String[] newEnvp = new String[] {};

		if (envpPath != null) {
			newEnvp = new String[envp.length];
			for (int i = 0; i < envp.length; i++) {
				if (envp[i].startsWith(PATH_EQUAL)) {
					newEnvp[i] = newPath.toString();
				} else {
					newEnvp[i] = envp[i];
				}
			}
		} else if (systemEnvMap != null) {
			Map<String, String> envVars = systemEnvMap;
			Set<String> keySet = envVars.keySet();
			newEnvp = new String[envVars.size()];

			int i = 0;
			for (String key : keySet) {
				if(key.startsWith(PATH)) {
					if (ltPath!=null) {
						newEnvp[i] = key + "=" + ltPath + SEPARATOR + envVars.get(key); //$NON-NLS-1$
					} else {
						newEnvp[i] = key + "=" + envVars.get(key); //$NON-NLS-1$
					}
				} else {
					newEnvp[i] = key + "=" + envVars.get(key); //$NON-NLS-1$
				}
				i++;
			}
		} else {
			newEnvp = new String[] {};
		}
		return newEnvp;
	}
}
