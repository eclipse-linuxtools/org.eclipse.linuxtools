/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
    public static TestPlugin getDefault() {
        return plugin;
    }

}
