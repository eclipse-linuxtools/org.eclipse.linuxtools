/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures;

import java.io.File;
import java.io.FileFilter;

public class CCodeFileFilter implements FileFilter {
    /**
     * Checks a file type and only passes it (returns true) if it is either a directory, a .c, or a .h
     * file type.
     *
     * @param f The file to check.
     *
     * @return A boolean value indicating whether or not to display the file.
     */
    @Override
    public boolean accept(File f) {
        if(null == f)
            return false;
        return accept(f.getName(), f.isDirectory());
    }

    /**
     * Checks a file type and only passes it (returns true) if it is either a directory, a .c, or a .h
     * file type.
     *
     * @param name The name of the file
     * @param isDir Is this file a directory?
     *
     * @return A boolean value indicating whether or not to display the file.
     *
     * @since 1.1
     */
    public boolean accept(String name, boolean isDir) {
        String lower = name.toLowerCase();
        return isDir ||
                lower.endsWith(".c") || //$NON-NLS-1$
                lower.endsWith(".h"); //$NON-NLS-1$
    }

    public String getDescription() {
        return ".c, .h files"; //$NON-NLS-1$
    }
}
