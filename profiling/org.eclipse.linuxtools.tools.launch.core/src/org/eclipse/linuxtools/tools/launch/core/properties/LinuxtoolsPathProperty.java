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

package org.eclipse.linuxtools.tools.launch.core.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.linuxtools.tools.launch.core.Activator;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class LinuxtoolsPathProperty {
	public static final String LINUXTOOLS_PATH_NAME = Activator.PLUGIN_ID + ".LinuxtoolsPath"; //$NON-NLS-1$
	public static final String LINUXTOOLS_PATH_SYSTEM_NAME = Activator.PLUGIN_ID + ".LinuxtoolsSystemEnvPath"; //$NON-NLS-1$
	public static final String LINUXTOOLS_PATH_DEFAULT = ""; //$NON-NLS-1$
	public static String getLinuxtoolsPath(IProject project) {
		if (project == null)
			return null;

		ScopedPreferenceStore store = new ScopedPreferenceStore(
				new ProjectScope(project),
				Activator.PLUGIN_ID);

		//If the value is not stored we use the default that is True
		boolean systemPathSelected = !store.contains(LINUXTOOLS_PATH_SYSTEM_NAME) ||
				store.getBoolean(LINUXTOOLS_PATH_SYSTEM_NAME);
		if (systemPathSelected)
			return LINUXTOOLS_PATH_DEFAULT;

		String path = store.getString(LINUXTOOLS_PATH_NAME);
		if (path == null || path.equals(""))
			return LINUXTOOLS_PATH_DEFAULT;
		return path;
	}
}
