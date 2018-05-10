/*******************************************************************************
 * Copyright (c) 2006, 2018 Phil Muldoon <pkmuldoon@picobot.org> and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *    Kyu Lee <klee@redhat.com>          - editor support
 *    Alexander Kurtakov (Red Hat)       - remove preferences initializing
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class ChangelogPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.eclipse.linuxtools.changelog.core"; // $NON-NLS-1$

    // The shared instance.
    private static ChangelogPlugin plugin;

    /**
     * The constructor.
     */
    public ChangelogPlugin() {
        // super();
        plugin = this;
    }

    /**
     * Returns the shared instance.
     * @return The plugin activator.
     */
    public static ChangelogPlugin getDefault() {
        return plugin;
    }

}
