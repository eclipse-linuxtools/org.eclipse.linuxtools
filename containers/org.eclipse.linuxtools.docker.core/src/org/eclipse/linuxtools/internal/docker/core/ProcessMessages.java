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
		return MessageFormat.format(getString(key), new Object[] { arg });
	}

	public static String getFormattedString(final String key,
			final Object... args) {
		return MessageFormat.format(getString(key), args);
	}

}
