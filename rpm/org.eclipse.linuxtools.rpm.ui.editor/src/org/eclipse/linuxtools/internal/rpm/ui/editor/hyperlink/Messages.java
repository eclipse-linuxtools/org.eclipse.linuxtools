/*******************************************************************************
 * Copyright (c) 2009, 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.messages"; //$NON-NLS-1$
	public static String SourcesFileDownloadHyperlink_0;
	public static String SourcesFileDownloadHyperlink_1;
	public static String SourcesFileDownloadHyperlink_2;
	public static String SourcesFileDownloadHyperlink_3;
	public static String SourcesFileDownloadHyperlink_4;
	public static String SourcesFileHyperlink_0;
	public static String SourcesFileHyperlink_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
