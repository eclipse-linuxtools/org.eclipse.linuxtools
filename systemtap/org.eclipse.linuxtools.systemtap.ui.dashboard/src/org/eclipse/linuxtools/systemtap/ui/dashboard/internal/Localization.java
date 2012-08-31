package org.eclipse.linuxtools.systemtap.ui.dashboard.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localization {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.ui.dashboard.internal.localization"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Localization() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
