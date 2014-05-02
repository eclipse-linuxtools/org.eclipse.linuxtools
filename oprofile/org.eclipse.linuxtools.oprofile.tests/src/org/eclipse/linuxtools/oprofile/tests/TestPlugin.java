/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class TestPlugin extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.tests"; //$NON-NLS-1$

    public static final String SYMBOL1_FILENAME = "/test/path/for/src/image.cpp"; //$NON-NLS-1$
    public static final String SYMBOL2_FILENAME = "/test/path/for/src/image2.cpp"; //$NON-NLS-1$

    public static final String DEP2_SYMBOL1_FILENAME = "dl-lookup.c"; //$NON-NLS-1$
    public static final String DEP2_SYMBOL2_FILENAME = "rawmemchr.c"; //$NON-NLS-1$
    public static final String DEP4_SYMBOL_FILENAME = ""; //$NON-NLS-1$

    // The shared instance
    private static TestPlugin plugin = null;

    /**
     * The constructor
     */
    public TestPlugin() {
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
    public static TestPlugin getDefault() {
        return plugin;
    }

}
