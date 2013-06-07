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
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.actions.messages"; //$NON-NLS-1$
	public static String OpenSourceFileAction_view_error;
	public static String OpenSourceFileAction_Error;
	public static String OpenSourceFileAction_file_dne;
	public static String OpenSourceFileAction_warning_inconsistency;
	public static String OpenSourceFileAction_open_src_action_text;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
