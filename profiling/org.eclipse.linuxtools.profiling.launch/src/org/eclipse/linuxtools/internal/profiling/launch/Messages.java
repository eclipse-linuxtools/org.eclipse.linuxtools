/*******************************************************************************
 * Copyright (c) 2008, 2013, 2015 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.profiling.launch.messages"; //$NON-NLS-1$
    public static String RemoteProxyManager_unrecognized_scheme;
    public static String ProfilingTabName;
    public static String ProfilingTab_no_profilers_installed;
    public static String ProfilingTab_specified_providerid_not_installed;
    public static String ProfilingTab_specified_profiler_not_installed;
    public static String ProfilingTab_no_category_profilers_installed;
    public static String ProfilingTab_providerid_not_found;
    public static String ProfileLaunchShortcut_Binaries;
    public static String ProfileLaunchShortcut_Binary_not_found;
    public static String ProfileLaunchShortcut_Choose_a_launch_configuration;
    public static String ProfileLaunchShortcut_Choose_a_local_application;
    public static String ProfileLaunchShortcut_Launch_Configuration_Selection;
    public static String ProfileLaunchShortcut_Looking_for_executables;
    public static String ProfileLaunchShortcut_no_project_selected;
    public static String ProfileLaunchShortcut_Profile;
    public static String ProfileLaunchShortcut_Qualifier;
    public static String ProfileLaunchShortcut_Launcher;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
