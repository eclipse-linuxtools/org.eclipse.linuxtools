/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ProfileLaunchPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.profiling.launch"; //$NON-NLS-1$

    // The shared instance
    private static ProfileLaunchPlugin plugin;

    // The launch mode this plug-in supports
    public static final String LAUNCH_MODE = "linuxtools"; //$NON-NLS-1$


    /**
     * The constructor
     */
    public ProfileLaunchPlugin() {
    }

    /**
     * Convenience method which returns the unique identifier of this plugin.
     * @return The identifier.
     * @since 1.1
     */
    public static String getUniqueIdentifier() {
        if (getDefault() == null) {
            // If the default instance is not yet initialized,
            // return a static identifier. This identifier must
            // match the plugin id defined in plugin.xml
            return PLUGIN_ID;
        }
        return getDefault().getBundle().getSymbolicName();
    }

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
    public static ProfileLaunchPlugin getDefault() {
        return plugin;
    }

    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            return window.getShell();
        }
        return null;
    }

    public static Shell getShell() {
        if (getActiveWorkbenchShell() != null) {
            return getActiveWorkbenchShell();
        }
        IWorkbenchWindow[] windows = getDefault().getWorkbench().getWorkbenchWindows();
        return windows[0].getShell();
    }

    /**
     * Logs the specified status with this plug-in's log.
     *
     * @param status
     *            status to log
     * @since 1.1
     */
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }
    /**
     * Logs an internal error with the specified message.
     *
     * @param message
     *            the error message to log
     * @since 1.1
     */
    public static void logErrorMessage(String message) {
        log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
    }

    /**
     * Logs an internal error with the specified throwable
     *
     * @param e
     *            the exception to be logged
     * @since 1.1
     */
    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
    }

    public static void log(int status, String msg, Throwable e) {
        plugin.getLog().log(new Status(status, PLUGIN_ID, IStatus.OK, msg, e));
    }

    public static void log(int status, String msg) {
        log(status, msg, null);
    }
}
