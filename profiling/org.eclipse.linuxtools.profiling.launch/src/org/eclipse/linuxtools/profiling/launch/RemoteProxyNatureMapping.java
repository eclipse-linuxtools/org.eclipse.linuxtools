/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rodrigo Fraxino Araujo <rfaraujo@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;

/**
 * @since 2.0
 */
public class RemoteProxyNatureMapping {

	private static final String EXTENSION_POINT_ID = "RemoteProxyNatureMapping"; //$NON-NLS-1$
	private static final String MANAGER_NAME = "mapping"; //$NON-NLS-1$
	private static final String NATURE_ID = "nature"; //$NON-NLS-1$
	private static final String SCHEME_ID = "schema"; //$NON-NLS-1$

	public String getSchemeFromNature(IProject project) throws CoreException {

		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID, EXTENSION_POINT_ID);
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for(int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			if (configurationElement.getName().equals(MANAGER_NAME)) {
				for (int j=0;j<natures.length;j++) {
					if (configurationElement.getAttribute(NATURE_ID).equals(natures[j])) {
						return configurationElement.getAttribute(SCHEME_ID);
					}
				}
			}
		}
		return null;
	}
}
