/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.messages"; //$NON-NLS-1$
	public static String ConditionalExpressionValidator_MustContain;
	public static String ConditionalExpressionValidator_MustEndWith;
	public static String ConditionalExpressionValidator_MustEnterSomething;
	public static String ConditionalExpressionValidator_MustStartWith;
	public static String ConditionalExpressionValidator_NotNull;
	public static String DirectoryValidator_CanNotContain;
	public static String DirectoryValidator_FolderName;
	public static String DirectoryValidator_MustEnd;
	public static String DirectoryValidator_NotNull;
	public static String EnvironmentVariablesPreferencePage_Title;
	public static String SystemTapPreferencePageDescription;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
