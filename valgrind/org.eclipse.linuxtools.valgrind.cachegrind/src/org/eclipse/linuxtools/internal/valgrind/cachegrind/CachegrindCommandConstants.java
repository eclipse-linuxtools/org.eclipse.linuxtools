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
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind;

public final class CachegrindCommandConstants {
    // Valgrind program arguments
    public static final String OPT_CACHEGRIND_OUTFILE = "--cachegrind-out-file"; //$NON-NLS-1$
    public static final String OPT_I1 = "--I1"; //$NON-NLS-1$
    public static final String OPT_D1 = "--D1"; //$NON-NLS-1$
    public static final String OPT_L2 = "--L2"; //$NON-NLS-1$
    public static final String OPT_CACHE_SIM = "--cache-sim"; //$NON-NLS-1$
    public static final String OPT_BRANCH_SIM = "--branch-sim"; //$NON-NLS-1$
}
