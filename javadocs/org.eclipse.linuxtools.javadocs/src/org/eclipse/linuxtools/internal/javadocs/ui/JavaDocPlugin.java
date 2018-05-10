/*******************************************************************************
 * Copyright (c) 2012, 2015 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
