/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class OprofileProperties {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.oprofile.core.oprofile"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private OprofileProperties() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
