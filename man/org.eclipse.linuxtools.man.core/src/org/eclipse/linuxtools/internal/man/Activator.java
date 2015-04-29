/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The shared instance
    private static Activator plugin;

    private BundleContext context;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        this.context = context;
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        this.context = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance of the bundle activator.
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns the bundle symbolic name of the plug-in.
     *
     * @return an ID unique to this plug-in
     */
    public String getPluginId() {
        return context.getBundle().getSymbolicName();
    }
}
