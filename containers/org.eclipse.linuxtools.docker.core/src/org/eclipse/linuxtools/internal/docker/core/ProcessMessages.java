/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ProcessMessages {

	private static final String BUNDLE_NAME = ProcessMessages.class.getName();

	public static String getString(String key) {
		try {
			return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		} catch (NullPointerException e) {
			return '#' + key + '#';
		}
	}

	public static String getFormattedString(final String key,
			final Object arg) {
		return MessageFormat.format(getString(key), arg);
	}

	public static String getFormattedString(final String key,
			final Object... args) {
		return MessageFormat.format(getString(key), args);
	}

}
