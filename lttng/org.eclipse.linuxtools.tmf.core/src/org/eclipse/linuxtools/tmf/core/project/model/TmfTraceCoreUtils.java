/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.project.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Utility class for common tmf.core functionalities
 *
 * @since 3.2
 */
public class TmfTraceCoreUtils {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows") ? true : false; //$NON-NLS-1$ //$NON-NLS-2$
    private static final String INVALID_RESOURCE_CHARACTERS_WIN = "[\\\\/:*?\\\"<>]|\\.$"; //$NON-NLS-1$
    private static final String INVALID_RESOURCE_CHARACTERS_OTHER = "[/\0]"; //$NON-NLS-1$

    /**
     * Validates whether the given input file or folder string is a valid
     * resource string for one of the given types. It replaces invalid
     * characters by '_' and prefixes the name with '_' if needed.
     *
     * @param input
     *            a input name to validate
     * @return valid name
     */
    public static String validateName(String input) {
        String output = input;
        String pattern;
        if (IS_WINDOWS) {
            pattern = INVALID_RESOURCE_CHARACTERS_WIN;
        } else {
            pattern = INVALID_RESOURCE_CHARACTERS_OTHER;
        }

        output = output.replaceAll(pattern, String.valueOf('_'));
        if(!ResourcesPlugin.getWorkspace().validateName(output, IResource.FILE | IResource.FOLDER).isOK()) {
            output = '_' + output;
        }
        return output;
    }
}
