/*******************************************************************************
 * Copyright (c) 2010, 2011 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *    Red Hat Inc. - modified to be shared by remote tools
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.profiling.launch.remote;

import org.eclipse.osgi.util.NLS;

public class RemoteMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.profiling.launch.remote.remoteMessages"; //$NON-NLS-1$
	public static String RemoteLaunchDelegate_error_launch_failed;
	public static String RemoteLaunchDelegate_error_no_fs;
	public static String RemoteLaunchDelegate_error_no_host;
	public static String RemoteLaunchDelegate_error_no_proc;
	public static String RemoteLaunchDelegate_error_no_streams;
	public static String RemoteLaunchDelegate_task_name;
	public static String RemoteProcess_error_proc_not_term;
	public static String RemoteTab_error_dest_wd;
	public static String RemoteTab_error_location_VG;
	public static String RemoteTab_error_tmp_dir;
	public static String RemoteTab_header_ID;
	public static String RemoteTab_header_name;
	public static String RemoteTab_header_OS;
	public static String RemoteTab_header_hostname;
	public static String RemoteTab_header_type;
	public static String RemoteTab_header_transport;
	public static String RemoteTab_label_dest_wd;
	public static String RemoteTab_label_location_VG;
	public static String RemoteTab_label_hosts;
	public static String RemoteTab_label_tmp_dir;
	public static String RemoteTab_tab_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, RemoteMessages.class);
	}

	private RemoteMessages() {
	}
}
