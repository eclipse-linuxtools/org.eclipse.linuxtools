/*******************************************************************************
 * Copyright (c) 2022 Mat Booth and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$

	public static String DevHelpContentProducer_ContentReadError;
	public static String DevHelpGenerateJob_GenerateJobName;
	public static String DevHelpToc_TocLabel;
	public static String DevHelpTopic_ParseXMLError;
	public static String ParseDevHelp_ParseFileTask;
	public static String ParseDevHelp_ParseTask;
	public static String LibHoverPreferencePage_DirChooserTitle;
	public static String LibHoverPreferencePage_DirsLabel;
	public static String LibHoverPreferencePage_GenButtonLabel;
	public static String LibHoverPreferencePage_PrefsTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
