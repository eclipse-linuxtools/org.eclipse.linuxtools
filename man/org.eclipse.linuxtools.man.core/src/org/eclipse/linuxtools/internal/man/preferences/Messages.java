/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * Message constants.
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.man.preferences.messages"; //$NON-NLS-1$
	/**
	 * Preference page title.
	 */
	public static String ManPathPage_0;
	/**
	 * Preference page label.
	 */
	public static String ManPathPage_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
