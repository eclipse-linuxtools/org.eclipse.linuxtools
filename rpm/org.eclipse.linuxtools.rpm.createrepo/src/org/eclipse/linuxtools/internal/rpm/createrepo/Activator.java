/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.rpm.createrepo"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, path).get();
    }

    /**
     * Get the enabled status of using the project specific settings of
     * createrepo.
     *
     * @return True if it is being used, false otherwise.
     */
    public static boolean isProjectPrefEnabled() {
        return getDefault().getPreferenceStore().getBoolean(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED);
    }

    /**
     * Get the enabled status of using the delta preferences of
     * createrepo.
     *
     * @return True if it is being used, false otherwise.
     */
    public static boolean isDeltaPrefEnabled() {
        return getDefault().getPreferenceStore().getBoolean(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE);
    }

    /**
     * Log an error.
     *
     * @param message A human-readable message.
     * @param exception The exception to log.
     */
    public static void logError(String message, Throwable exception) {
		getDefault().getLog().log(Status.error(message, exception));
    }

}
