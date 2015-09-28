/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
