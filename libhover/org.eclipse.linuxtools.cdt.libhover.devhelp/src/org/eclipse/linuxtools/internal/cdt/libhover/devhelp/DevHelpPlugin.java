/*******************************************************************************
 * Copyright (c) 2012-2014 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class DevHelpPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.cdt.libhover.devhelp"; //$NON-NLS-1$

    // The shared instance
    private static DevHelpPlugin plugin;

    // Startup job
    private static Job k;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    	if (k != null)
    		k.cancel();
        plugin = null;
        super.stop(context);
    }

    public void setJob(Job j) {
    	k = j;
    }
    
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static DevHelpPlugin getDefault() {
        return plugin;
    }

}