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
	public static String PerfSaveSession_invalid_filename_title;
	public static String PerfSaveSession_invalid_filename_msg;
	public static String PerfSaveSession_file_exists_title;
	public static String PerfSaveSession_file_exists_msg;
	public static String PerfSaveStat_error_title;
	public static String PerfSaveStat_error_msg;
	public static String PerfSaveSession_failure_title;
	public static String PerfSaveSession_failure_msg;
	public static String PerfStatDiffMenuAction_diff_text;
	public static String PerfStatDiffMenuAction_new_tooltip;
	public static String PerfStatDiffMenuAction_new_text;
	public static String PerfStatDiffMenuAction_old_tooltip;
	public static String PerfStatDiffMenuAction_old_text;
	public static String PerfStatDiffMenuAction_stats_tooltip;
	public static String PerfResourceLeak_title;
	public static String PerfResourceLeak_msg;

	public static String MsgClearSelection;
	public static String MsgSelectionDiff;
	public static String MsgSelectFiles;
	public static String MsgError;
	public static String MsgError_0;
	public static String MsgError_1;
	public static String MsgError_2;
	public static String MsgWarning_0;
	public static String MsgWarning_1;
	public static String MsgConfirm_title;
	public static String MsgConfirm_msg;
	public static String ContentDescription_0;

	public static String  PerfEditorLauncher_stat_title;
	public static String PerfEditorLauncher_file_dne_error;
	public static String PerfEditorLauncher_file_read_error;
	public static String StatComparisonData_temp_files_error;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
