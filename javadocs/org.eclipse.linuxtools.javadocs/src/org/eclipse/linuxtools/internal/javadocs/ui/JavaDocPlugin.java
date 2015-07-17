/*******************************************************************************
 * Copyright (c) 2012-2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 * Eric Williams <ericwill@redhat.com> - modification for Javadocs
 *******************************************************************************/

package org.eclipse.linuxtools.internal.javadocs.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle.
 */
public class JavaDocPlugin extends AbstractUIPlugin {


	// The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.javadocs"; //$NON-NLS-1$


    // The shared instance
    private static JavaDocPlugin plugin;

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
	 * Returns the shared instance of the plugin.
	 *
	 * @return  the shared instance of the plugin
	 */
	public static JavaDocPlugin getDefault() {
        return plugin;
    }
}
