/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.help;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.man.help.messages"; //$NON-NLS-1$

	public static String ManPageProducer_ParseError;

	public static String ManualToc_Section0;
	public static String ManualToc_Section1;
	public static String ManualToc_Section2;
	public static String ManualToc_Section3;
	public static String ManualToc_Section4;
	public static String ManualToc_Section5;
	public static String ManualToc_Section6;
	public static String ManualToc_Section7;
	public static String ManualToc_Section8;
	public static String ManualToc_Section9;
	public static String ManualToc_SectionAM;
	public static String ManualToc_SectionG;
	public static String ManualToc_SectionP;
	public static String ManualToc_SectionPM;
	public static String ManualToc_SectionPY;
	public static String ManualToc_SectionX;
	public static String ManualToc_SectionSSL;
	public static String ManualToc_SectionSTAP;
	public static String ManualToc_TocLabel;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
