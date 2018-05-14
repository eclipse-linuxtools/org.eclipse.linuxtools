/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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
package org.eclipse.linuxtools.internal.docker.ui.consoles;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConsoleMessages {

	private static final String BUNDLE_NAME = ConsoleMessages.class.getName();

	public static String getString(String key) {
		try {
			return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		} catch (NullPointerException e) {
			return '#' + key + '#';
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getString(key), arg);
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), (Object[]) args);
	}

}
