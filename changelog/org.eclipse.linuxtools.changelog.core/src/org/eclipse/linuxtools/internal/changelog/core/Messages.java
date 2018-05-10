/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.changelog.core.strings"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
        // It shouldn't be instantiated.
    }
    /**
     * Returns the message for the given key.
     * @param key The key of the message looking for.
     * @return The found message or "!key!" if no such key.
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
