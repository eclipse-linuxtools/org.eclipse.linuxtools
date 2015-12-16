/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Alena Laskavaia - javadoc comments
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core;

/**
 * Constants for valgrind options
 */
public interface CommandLineConstants {
	/** --tool */
	String OPT_TOOL = "--tool"; //$NON-NLS-1$
	/** --xml */
    String OPT_XML = "--xml"; //$NON-NLS-1$
	/** --log-file */
    String OPT_LOGFILE = "--log-file"; //$NON-NLS-1$
	/** -q */
    String OPT_QUIET = "-q"; //$NON-NLS-1$
	/** --version */
    String OPT_VERSION = "--version"; //$NON-NLS-1$
	/** --trace-children */
    String OPT_TRACECHILD = "--trace-children"; //$NON-NLS-1$
	/** --child-silent-after-fork */
    String OPT_CHILDSILENT = "--child-silent-after-fork"; //$NON-NLS-1$
	/** --track-fds */
    String OPT_TRACKFDS = "--track-fds"; //$NON-NLS-1$
    /** --time-stamp */
    String OPT_TIMESTAMP = "--time-stamp"; //$NON-NLS-1$
    /** --run-libc-freeres */
    String OPT_FREERES = "--run-libc-freeres"; //$NON-NLS-1$
    /** --demangle */
    String OPT_DEMANGLE = "--demangle"; //$NON-NLS-1$
    /** --num-callers */
    String OPT_NUMCALLERS = "--num-callers"; //$NON-NLS-1$
    /** --error-limit */
    String OPT_ERRLIMIT = "--error-limit"; //$NON-NLS-1$
    /** --show-below-main */
    String OPT_BELOWMAIN = "--show-below-main"; //$NON-NLS-1$
    /** --max-stackframe */
    String OPT_MAXFRAME = "--max-stackframe"; //$NON-NLS-1$
    /** --suppressions */
    String OPT_SUPPFILE = "--suppressions"; //$NON-NLS-1$

    /** --main-stacksize
     * 3.4.0 specific
     */
    String OPT_MAINSTACK = "--main-stacksize"; //$NON-NLS-1$

    /** --dsymutil
     * 3.6.0 specific
     */
    String OPT_DSYMUTIL = "--dsymutil"; //$NON-NLS-1$

    /** valgrind log file prefix */
    String LOG_PREFIX = "valgrind_"; //$NON-NLS-1$
}
