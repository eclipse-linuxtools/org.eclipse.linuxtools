/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.launch;

/**
 * The main plugin class to be used in the desktop.
 */
public class GcovLaunchPlugin {
    //shared cache instance for configuration

    public static final String PLUGIN_ID = "org.eclipse.linuxtools.gcov.launch"; //$NON-NLS-1$
    public static final String ID_GCOV_VIEW = "org.eclipse.linuxtools.gcov.view"; //$NON-NLS-1$
    public static final String LAUNCH_ID = PLUGIN_ID + ".gcovLaunch"; //$NON-NLS-1$

}
