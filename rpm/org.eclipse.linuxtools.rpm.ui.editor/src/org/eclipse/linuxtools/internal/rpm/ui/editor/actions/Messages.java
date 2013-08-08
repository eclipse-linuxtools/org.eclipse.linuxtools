/*******************************************************************************
 * Copyright (c) 2009, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.rpm.ui.editor.actions.messages"; //$NON-NLS-1$
	public static String SpecfileChangelogFormatter_0;
	public static String SpecfileChangelogFormatter_1;

	// SpecfileEditorDownloadSourcesActionDelegate
	public static String DownloadSources_malformedURL;
	public static String DownloadSources_cannotConnectToURL;

	// SpecfileEditorPrepareSourcesActionDelegate
	public static String PrepareSources_error;
	public static String PrepareSources_downloadSourcesMalformedURL;
	public static String PrepareSources_downloadCancelled;
	public static String PrepareSources_downloadConnectionFail;
	public static String PrepareSources_coreException;
	public static String PrepareSources_prepareSources;
	public static String PrepareSources_consoleName;

	// RPMHandlerUtils
	public static String RPMHandlerUtils_cannotCreateRPMProject;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
