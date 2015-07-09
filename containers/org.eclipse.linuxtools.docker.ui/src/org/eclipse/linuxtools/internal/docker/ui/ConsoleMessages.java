/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

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
		return MessageFormat.format(getString(key), new Object[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), (Object[]) args);
	}

}
