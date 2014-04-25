/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind;

/*
 *  user options for Helgrind:
 *   --track-lockorders=no|yes show lock ordering errors? [yes]
 *   --history-level=none|approx|full [full]
 *      full:   show both stack traces for a data race (can be very slow)
 *      approx: full trace for one thread, approx for the other (faster)
 *      none:   only show trace for one thread in a race (fastest)
 *   --conflict-cache-size=N   size of 'full' history cache [1000000]
 */
public final class HelgrindCommandConstants
{
    public static final String OPT_TRACK_LOCKORDERS = "--track-lockorders"; //$NON-NLS-1$
    public static final String OPT_HISTORY_LEVEL = "--history-level"; //$NON-NLS-1$
    public static final String OPT_CONFLICT_CACHE_SIZE = "--conflict-cache-size"; //$NON-NLS-1$

}
