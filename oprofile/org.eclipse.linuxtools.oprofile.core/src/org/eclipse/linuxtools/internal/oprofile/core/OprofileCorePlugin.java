/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.internal.oprofile.core.linux.LinuxOpxmlProvider;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class OprofileCorePlugin extends Plugin {
    private static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.core"; //$NON-NLS-1$

    //The shared instance.
    private static OprofileCorePlugin plugin;

    public static final String DEBUG_PRINT_PREFIX = "DEBUG: "; //$NON-NLS-1$

    /**
     * The constructor.
     */
    public OprofileCorePlugin() {
        plugin = this;
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static OprofileCorePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the unique id of this plugin. Should match plugin.xml!
     */
    public static String getId() {
        return PLUGIN_ID;
    }

    /**
     * Returns the OpxmlProvider registered with the plugin or throws an exception
     * @return the OpxmlProvider
     */
    public IOpxmlProvider getOpxmlProvider() {
        return new LinuxOpxmlProvider();
    }

    /**
     * Returns the registered opcontrol provider or throws an exception
     * @return the OpcontrolProvider registered with the plugin
     * @throws OpcontrolException
     */
    public IOpcontrolProvider getOpcontrolProvider() throws OpcontrolException {
        IOpcontrolProvider opcontrolProvider = null;

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.linuxtools.oprofile.core.OpcontrolProvider"); //$NON-NLS-1$
        if (extPoint != null) {
            IExtension[] extensions = extPoint.getExtensions();
            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                if (configElements.length != 0) {
                    try {
                        String scheme = configElements[0].getAttribute("scheme");

                        // If no project associated, get the local opcontrol provider
                        if (Oprofile.OprofileProject.getProject() == null && scheme.equals("file")) {
                            opcontrolProvider = (IOpcontrolProvider) configElements[0].createExecutableExtension("class");//$NON-NLS-1$
                            break;
                        }

                        if(Oprofile.OprofileProject.getProject().getLocationURI().getScheme().equals(scheme)){
                            opcontrolProvider = (IOpcontrolProvider) configElements[0].createExecutableExtension("class");//$NON-NLS-1$
                        }
                    } catch (CoreException ce) {
                        ce.printStackTrace();
                    }
                }
            }
        }
        // If there was a problem finding opcontrol, throw an exception
        if(opcontrolProvider == null) {
            throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolProvider", null));
        }

        return opcontrolProvider;
    }

    /**
     * Creates an error status object
     * @param errorClassString A string of the error class
     * @param e The type of exception
     * @return the status object of the error
     */
    public static IStatus createErrorStatus(String errorClassString, Exception e) {
        String statusMessage = OprofileProperties.getString(errorClassString + ".error.statusMessage"); //$NON-NLS-1$

        if (e == null) {
            return new Status(IStatus.ERROR, getId(), IStatus.OK, statusMessage, null);
        } else {
            return new Status(IStatus.ERROR, getId(), IStatus.OK, statusMessage, e);
        }
    }

    /**
     * Shows an error Dialog
     * @param errorClassString A string of the error class
     * @param ex The type of exception
     */
    public static void showErrorDialog(String errorClassString, CoreException ex) {
        final IStatus status;
        final String dialogTitle = OprofileProperties.getString(errorClassString + ".error.dialog.title"); //$NON-NLS-1$
        final String errorMessage = OprofileProperties.getString(errorClassString + ".error.dialog.message"); //$NON-NLS-1$

        if (ex == null) {
            status = createErrorStatus(errorClassString, null);
        } else {
            status = ex.getStatus();
        }

        //needs to be run in the ui thread otherwise swt throws invalid thread access
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                ErrorDialog.openError(null, dialogTitle, errorMessage, status);
            }
        });

    }

    /**
     *
     * @return {@code true} when platform was started in debug mode ({@code -debug} switch)
     * and {@code org.eclipse.linuxtools.internal.oprofile.core/debug} is set in some .options file
     * either in $HOME/.options or $(pwd)/.options.
     */
    public static boolean isDebugMode() {
        return Platform.inDebugMode()
                && Platform.getDebugOption(OprofileCorePlugin.getId()
                        + "/debug") != null; //$NON-NLS-1$
    }

    /**
     * Log a string message with the given severity in the error log.
     *
     * @param severity the severity of this exception
     * @param msg the string message to be logged
     */
    public static void log(int severity, String msg) {
          plugin.getLog().log(new Status(severity, PLUGIN_ID, IStatus.OK, msg, null));
    }

    /**
     * Returns the location of the plugin by checking the path of the bundle's
     * locationURL.
     *
     * @return An absolute path representing the location of this plugin
     */
    public String getPluginLocation() {
        Bundle bundle = getBundle();

        URL locationUrl = FileLocator.find(bundle,new Path("/"), null); //$NON-NLS-1$
        URL fileUrl = null;
        try {
            fileUrl = FileLocator.toFileURL(locationUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileUrl.getFile();
    }

}
