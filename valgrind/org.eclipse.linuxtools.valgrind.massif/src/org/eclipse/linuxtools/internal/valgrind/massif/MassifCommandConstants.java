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
package org.eclipse.linuxtools.internal.valgrind.massif;

public final class MassifCommandConstants {
	// Valgrind program arguments
	public static final String OPT_MASSIF_OUTFILE = "--massif-out-file"; //$NON-NLS-1$
	public static final String OPT_HEAP = "--heap"; //$NON-NLS-1$
	public static final String OPT_HEAPADMIN = "--heap-admin"; //$NON-NLS-1$
	public static final String OPT_STACKS = "--stacks"; //$NON-NLS-1$
	public static final String OPT_DEPTH = "--depth"; //$NON-NLS-1$
	public static final String OPT_ALLOCFN = "--alloc-fn"; //$NON-NLS-1$
	public static final String OPT_IGNOREFN = "--ignore-fn"; //$NON-NLS-1$
	public static final String OPT_THRESHOLD = "--threshold"; //$NON-NLS-1$
	public static final String OPT_PEAKINACCURACY = "--peak-inaccuracy"; //$NON-NLS-1$
	public static final String OPT_TIMEUNIT = "--time-unit"; //$NON-NLS-1$
	public static final String OPT_DETAILEDFREQ = "--detailed-freq"; //$NON-NLS-1$
	public static final String OPT_MAXSNAPSHOTS = "--max-snapshots"; //$NON-NLS-1$
	public static final String OPT_ALIGNMENT = "--alignment"; //$NON-NLS-1$
	
	// VG >= 3.6.0
	public static final String OPT_PAGESASHEAP = "--pages-as-heap"; //$NON-NLS-1$
}
