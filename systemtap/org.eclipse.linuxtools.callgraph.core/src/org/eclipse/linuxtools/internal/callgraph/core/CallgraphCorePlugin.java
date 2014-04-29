/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.callgraph.core;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 *     Activator class behaves like standard Wizard-created activator,
 *  except for the checkRun() function.
 *
 */
public class CallgraphCorePlugin extends AbstractUIPlugin {


    public static final String PLUGIN_ID = "org.eclipse.linuxtools.callgraph.core"; //$NON-NLS-1$
    // The shared instance
    private static CallgraphCorePlugin plugin;

    /**
     * The constructor
     */
    public CallgraphCorePlugin() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
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
    public static CallgraphCorePlugin getDefault() {
        return plugin;
    }


    /**
     * Returns the location of the plugin by checking the path of the bundle's
     * locationURL.
     *
     * @return
     */
    public static String getPluginLocation() {
        Bundle bundle = Platform.getBundle(PLUGIN_ID);

        URL locationUrl = FileLocator.find(bundle,new Path("/"), null); //$NON-NLS-1$
        URL fileUrl = null;
        try {
            fileUrl = FileLocator.toFileURL(locationUrl);
            return fileUrl.getFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Log specified exception.
     * @param e Exception to log.
     */
    public static void logException(Exception e) {
        Status status = new Status(IStatus.ERROR, CallgraphCorePlugin.PLUGIN_ID,
                e.getMessage());
        CallgraphCorePlugin.getDefault().getLog().log(status);
    }

}
