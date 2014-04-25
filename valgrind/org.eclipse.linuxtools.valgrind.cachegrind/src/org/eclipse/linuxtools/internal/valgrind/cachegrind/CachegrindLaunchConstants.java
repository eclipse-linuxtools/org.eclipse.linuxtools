/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind;

public final class CachegrindLaunchConstants {
    // LaunchConfiguration attributes
    public static final String ATTR_CACHEGRIND_CACHE_SIM = CachegrindPlugin.PLUGIN_ID + ".CACHE_SIM"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_BRANCH_SIM = CachegrindPlugin.PLUGIN_ID + ".BRANCH_SIM"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_I1 = CachegrindPlugin.PLUGIN_ID + ".I1"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_I1_SIZE = CachegrindPlugin.PLUGIN_ID + ".I1_SIZE"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_I1_ASSOC = CachegrindPlugin.PLUGIN_ID + ".I1_ASSOC"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_I1_LSIZE = CachegrindPlugin.PLUGIN_ID + ".I1_LSIZE"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_D1 = CachegrindPlugin.PLUGIN_ID + ".D1"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_D1_SIZE = CachegrindPlugin.PLUGIN_ID + ".D1_SIZE"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_D1_ASSOC = CachegrindPlugin.PLUGIN_ID + ".D1_ASSOC"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_D1_LSIZE = CachegrindPlugin.PLUGIN_ID + ".D1_LSIZE"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_L2 = CachegrindPlugin.PLUGIN_ID + ".L2"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_L2_SIZE = CachegrindPlugin.PLUGIN_ID + ".L2_SIZE"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_L2_ASSOC = CachegrindPlugin.PLUGIN_ID + ".L2_ASSOC"; //$NON-NLS-1$
    public static final String ATTR_CACHEGRIND_L2_LSIZE = CachegrindPlugin.PLUGIN_ID + ".L2_LSIZE"; //$NON-NLS-1$

    public static final boolean DEFAULT_CACHEGRIND_CACHE_SIM = true;
    public static final boolean DEFAULT_CACHEGRIND_BRANCH_SIM = false;
    public static final boolean DEFAULT_CACHEGRIND_I1 = false;
    public static final int DEFAULT_CACHEGRIND_I1_SIZE = 0;
    public static final int DEFAULT_CACHEGRIND_I1_ASSOC = 0;
    public static final int DEFAULT_CACHEGRIND_I1_LSIZE = 0;
    public static final boolean DEFAULT_CACHEGRIND_D1 = false;
    public static final int DEFAULT_CACHEGRIND_D1_SIZE = 0;
    public static final int DEFAULT_CACHEGRIND_D1_ASSOC = 0;
    public static final int DEFAULT_CACHEGRIND_D1_LSIZE = 0;
    public static final boolean DEFAULT_CACHEGRIND_L2 = false;
    public static final int DEFAULT_CACHEGRIND_L2_SIZE = 0;
    public static final int DEFAULT_CACHEGRIND_L2_ASSOC = 0;
    public static final int DEFAULT_CACHEGRIND_L2_LSIZE = 0;
}
