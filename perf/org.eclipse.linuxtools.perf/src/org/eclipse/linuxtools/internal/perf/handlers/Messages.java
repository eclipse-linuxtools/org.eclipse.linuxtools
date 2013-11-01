/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.handlers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.perf.handlers.messages"; //$NON-NLS-1$
	public static String PerfSaveSession_title;
	public static String PerfSaveSession_msg;
	public static String PerfSaveSession_invalid_filename_msg;
	public static String PerfSaveSession_file_exists_title;
	public static String PerfSaveSession_file_exists_msg;
	public static String PerfSaveStat_error_title;
	public static String PerfSaveStat_error_msg;
	public static String PerfSaveSession_failure_title;
	public static String PerfSaveSession_failure_msg;
	public static String PerfResourceLeak_title;

	public static String MsgProxyError;
	public static String MsgError;
	public static String ContentDescription_0;

	public static String CompUnsupported;
	public static String PerfEditorLauncher_stat_title;
	public static String PerfEditorLauncher_file_dne_error;
	public static String PerfEditorLauncher_file_read_error;
	public static String ReportComparisonView_label;
	public static String StatComparisonView_label;
	public static String StatComparisonView_reverseToolTip;
	public static String StatComparisonView_reversedLabel;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
