/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.valgrind.launch.remote.messages"; //$NON-NLS-1$
	public static String ValgrindRemoteLaunchDelegate_error_launch_failed;
	public static String ValgrindRemoteLaunchDelegate_error_no_fs;
	public static String ValgrindRemoteLaunchDelegate_error_no_peers;
	public static String ValgrindRemoteLaunchDelegate_error_no_proc;
	public static String ValgrindRemoteLaunchDelegate_error_no_streams;
	public static String ValgrindRemoteLaunchDelegate_task_name;
	public static String ValgrindRemoteProcess_error_proc_not_term;
	public static String ValgrindRemoteTab_error_dest_wd;
	public static String ValgrindRemoteTab_error_location_VG;
	public static String ValgrindRemoteTab_error_peer;
	public static String ValgrindRemoteTab_error_tmp_dir;
	public static String ValgrindRemoteTab_header_ID;
	public static String ValgrindRemoteTab_header_name;
	public static String ValgrindRemoteTab_header_OS;
	public static String ValgrindRemoteTab_header_transport;
	public static String ValgrindRemoteTab_label_dest_wd;
	public static String ValgrindRemoteTab_label_location_VG;
	public static String ValgrindRemoteTab_label_peers;
	public static String ValgrindRemoteTab_label_tmp_dir;
	public static String ValgrindRemoteTab_tab_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
