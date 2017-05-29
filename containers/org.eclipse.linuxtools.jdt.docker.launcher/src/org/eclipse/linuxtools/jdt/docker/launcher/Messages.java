/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.jdt.docker.launcher.messages"; //$NON-NLS-1$
	public static String ContainerVMRunner_Specified_executable__0__does_not_exist_for__1__4;
	public static String ContainerVMRunner_Unable_to_locate_executable_for__0__1;
	public static String ImageSelectionDialog_connection_label;
	public static String ImageSelectionDialog_image_label;
	public static String ImageSelectionDialog_title;
	public static String JavaAppInContainerLaunchDelegate_Creating_source_locator____2;
	public static String JavaAppInContainerLaunchDelegate_Verifying_launch_attributes____1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
