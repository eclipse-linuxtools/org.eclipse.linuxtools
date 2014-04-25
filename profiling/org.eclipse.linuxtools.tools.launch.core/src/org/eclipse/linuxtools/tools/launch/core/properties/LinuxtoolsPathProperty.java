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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tools.launch.core.LaunchCoreConstants;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Class used to get information provided in Linux Tools Path Property Page.
 * This class provides the path to be used to run linux tools commands
 */
public class LinuxtoolsPathProperty {
    private static final String LINUXTOOLS_PATH_EXT_POINT = "LinuxtoolsPathOptions"; //$NON-NLS-1$
    private static final String LINUXTOOLS_PATH_OPTION = "option"; //$NON-NLS-1$
    private static final String LINUXTOOLS_PATH_OPTION_PATH = "path"; //$NON-NLS-1$
    private static final String LINUXTOOLS_PATH_OPTION_DEFAULT = "default"; //$NON-NLS-1$
    private String linuxtoolsPathDefault = ""; //$NON-NLS-1$
    private boolean linuxtoolsPathSystemDefault = true;
    private static LinuxtoolsPathProperty instance = null;


    private void fillLinuxtoolsPath(String path) {
        if (path != null && !path.isEmpty()) {
            linuxtoolsPathSystemDefault = false;
            linuxtoolsPathDefault = path;
        }
    }

    private LinuxtoolsPathProperty() {
        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(LaunchCoreConstants.PLUGIN_ID, LINUXTOOLS_PATH_EXT_POINT);
        if (extPoint != null) {
            IConfigurationElement[] configs = extPoint.getConfigurationElements();
            for (IConfigurationElement config : configs)
                if (config.getName().equals(LINUXTOOLS_PATH_OPTION)) {
                    String sdefault = config.getAttribute(LINUXTOOLS_PATH_OPTION_DEFAULT);
                    if (sdefault != null && sdefault.equals(Boolean.toString(true))) {
                        fillLinuxtoolsPath(config.getAttribute(LINUXTOOLS_PATH_OPTION_PATH));
                        break;
                    }
                }
        }
    }

    public static LinuxtoolsPathProperty getInstance() {
        if (instance == null) {
            instance = new LinuxtoolsPathProperty();
        }
        return instance;
    }

    /*
     * This function will return the selected Linuxtools Path in the
     * 'Linux Tools Path' project properties page.
     * If there is a default path setted by the extension point it should be
     * selected as the default by the tools.launch.ui classes. This function
     * will not care about it.
     *
     * * If the tools.launch.ui is not installed this function should always
     * return "".
     * * If the option 'Use the System env PATH' this function should return ""
     * * If the option 'Prepend string to path' is selected this function
     * should return the value of the selected path.
     */
    public String getLinuxtoolsPath(IProject project) {
        if (project == null) {
            return ""; //$NON-NLS-1$
        }

        ScopedPreferenceStore store = new ScopedPreferenceStore(
                new ProjectScope(project),
                LaunchCoreConstants.PLUGIN_ID);

        //If the value is not stored we use the default
        boolean systemPathSelected;
        if (store.contains(LaunchCoreConstants.LINUXTOOLS_PATH_SYSTEM_NAME)) {
            systemPathSelected = store.getBoolean(LaunchCoreConstants.LINUXTOOLS_PATH_SYSTEM_NAME);
        } else {
            systemPathSelected = getLinuxtoolsPathSystemDefault();
        }

        if (systemPathSelected) {
            return ""; //$NON-NLS-1$
        }

        String path = null;
        if (store.contains(LaunchCoreConstants.LINUXTOOLS_PATH_NAME)) {
            path = store.getString(LaunchCoreConstants.LINUXTOOLS_PATH_NAME);
        }

        if (path == null) {
            return getLinuxtoolsPathDefault();
        }
        return path;
    }

    public String getLinuxtoolsPathDefault() {
        return linuxtoolsPathDefault;
    }

    public boolean getLinuxtoolsPathSystemDefault() {
        return linuxtoolsPathSystemDefault;
    }
}
