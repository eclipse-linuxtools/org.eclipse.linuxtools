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
package org.eclipse.linuxtools.valgrind.core;

public interface CommandLineConstants {
	String OPT_TOOL = "--tool"; //$NON-NLS-1$

	String OPT_XML = "--xml"; //$NON-NLS-1$
	String OPT_LOGFILE = "--log-file"; //$NON-NLS-1$
	String OPT_QUIET = "-q"; //$NON-NLS-1$
	String OPT_VERSION = "--version"; //$NON-NLS-1$

	String OPT_TRACECHILD = "--trace-children"; //$NON-NLS-1$
	String OPT_CHILDSILENT = "--child-silent-after-fork"; //$NON-NLS-1$
	String OPT_TRACKFDS = "--track-fds"; //$NON-NLS-1$
	String OPT_TIMESTAMP = "--time-stamp"; //$NON-NLS-1$
	String OPT_FREERES = "--run-libc-freeres"; //$NON-NLS-1$
	String OPT_DEMANGLE = "--demangle"; //$NON-NLS-1$
	String OPT_NUMCALLERS = "--num-callers"; //$NON-NLS-1$
	String OPT_ERRLIMIT = "--error-limit"; //$NON-NLS-1$
	String OPT_BELOWMAIN = "--show-below-main"; //$NON-NLS-1$
	String OPT_MAXFRAME = "--max-stackframe"; //$NON-NLS-1$
	String OPT_SUPPFILE = "--suppressions"; //$NON-NLS-1$

	// 3.4.0 specific
	String OPT_MAINSTACK = "--main-stacksize"; //$NON-NLS-1$

	// 3.6.0 specific
	String OPT_DSYMUTIL = "--dsymutil"; //$NON-NLS-1$

	String LOG_PREFIX = "valgrind_"; //$NON-NLS-1$
}
