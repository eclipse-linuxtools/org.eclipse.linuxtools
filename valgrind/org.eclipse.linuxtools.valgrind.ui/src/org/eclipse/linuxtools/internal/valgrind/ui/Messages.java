/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper class to get localized messages. This is internal class to be used only in valgrind.ui plugin.
 */
public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.valgrind.ui.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	/**
	 * Gets a string from the resource bundle.
	 *
	 * @param key the string used to get the bundle value, must not be
	 *            <code>null</code>
	 * @return the string from the resource bundle
	 */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
