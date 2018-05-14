/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
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
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions.messages"; //$NON-NLS-1$
	public static String HardcodedPackagerTag_0;
	public static String HardcodedPrefixTag_0;
	public static String MacroInChangelog_0;
	public static String MacroInChangelog_1;
	public static String NoBuildrootTag_0;
	public static String NoBuildSection_0;
	public static String NoCleaningOfBuildroot_0;
	public static String NoCleanSection_0;
	public static String NoInstallSection_0;
	public static String NoPrepSection_0;
	public static String PatchNotApplied_0;
	public static String RpmBuildrootUsage_0;
	public static String SetupNotQuiet_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// should not be instantiated
	}
}
