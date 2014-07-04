/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.gprof"; //$NON-NLS-1$

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
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Error messages for when you throw/catch exceptions.
     *
     * <p> It shows information about which plugin threw the exception and shows a stack trace </p>
     *
     * @param ex      the exception that will be viewable as a stacktrace in the dialogue
     * @param title   title of the message dialogue
     */
    public void openError(Exception ex, final String title) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));

        final String message = ex.getMessage();
        final String formattedMessage = PLUGIN_ID + " : " + message; //$NON-NLS-1$
        final Status status = new Status(IStatus.ERROR, PLUGIN_ID, formattedMessage, new Throwable(writer.toString()));

        getLog().log(status);
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                ErrorDialog.openError(Display.getDefault().getActiveShell(),
                        title, message, status);
            }
        });
    }

}
