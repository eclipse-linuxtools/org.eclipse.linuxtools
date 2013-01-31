package org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Localization {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.localization";

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
