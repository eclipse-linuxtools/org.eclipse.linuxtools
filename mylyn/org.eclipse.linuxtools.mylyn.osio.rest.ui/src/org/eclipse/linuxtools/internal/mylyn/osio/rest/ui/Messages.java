/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.messages"; //$NON-NLS-1$
	
	public static String OSIOKeywordAttributeEditor_Select_X;
	public static String OSIORestRepositorySettingsPage_auth_token;
	public static String OSIORestRepositorySettingsPage_auth_username;
	public static String OSIORestRepositorySettingsPage_Description;
	public static String OSIORestRepositorySettingsPage_Please_copy_the_Auth_Token_from;
	public static String OSIORestRepositorySettingsPage_RestRepositorySetting;
	public static String OSIORestRepositorySettingsPage_View_your_auth_token_settings;
	public static String OSIORestTaskEditorPageFactory_OSIO;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

	
}
