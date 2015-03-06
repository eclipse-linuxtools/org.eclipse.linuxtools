/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ConsoleLogPlugin extends AbstractUIPlugin {

    /**
     * @since 2.0
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.systemtap.ui.consolelog"; //$NON-NLS-1$

    //The shared instance.
    private static ConsoleLogPlugin plugin;

    /**
     * The constructor.
     */
    public ConsoleLogPlugin() {
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
     * @return The bundle activator.
     */
    public static ConsoleLogPlugin getDefault() {
        return plugin;
    }

}
