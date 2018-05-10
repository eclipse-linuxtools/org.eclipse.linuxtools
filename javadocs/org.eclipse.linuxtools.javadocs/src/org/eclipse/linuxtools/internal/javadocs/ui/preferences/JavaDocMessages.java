/*******************************************************************************
 * Copyright (c) 2002, 2018 QNX Software Systems and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Red Hat Inc. - Modification to use with LibHover
 * Eric Williams <ericwill@redhat.com> - modification for Javadocs
 *******************************************************************************/

package org.eclipse.linuxtools.internal.javadocs.ui.preferences;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Makefile PreferencesMessages
 */
public class JavaDocMessages {


    private static final String RESOURCE_BUNDLE= JavaDocMessages.class.getName();
    private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

    private JavaDocMessages() {
    }

    public static ResourceBundle getResourceBundle() {
        return fgResourceBundle;
    }

    public static String getString(String key) {
        try {
            return fgResourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        } catch (NullPointerException e) {
            return '#' + key + '#';
        }
    }

    /**
     * Returns the formatted string from the resource bundle,
     * or 'key' if not found.
     *
     * @param key the message key
     * @param args an array of substitution strings
     * @return the resource bundle message
     */
    public static String getFormattedString(String key, String[] args) {
        return MessageFormat.format(getString(key), (Object[])args);
    }

}