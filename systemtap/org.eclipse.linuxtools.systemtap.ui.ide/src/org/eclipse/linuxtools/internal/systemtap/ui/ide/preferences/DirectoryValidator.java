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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import org.eclipse.jface.dialogs.IInputValidator;



public class DirectoryValidator implements IInputValidator {

    /**
     * Determines whether or not the string is valid within the contraints.
     *
     * @param s The string to check.
     *
     * @return The return message.
     */
    @Override
    public String isValid(String s) {
        if(null == s) {
            return Messages.DirectoryValidator_NotNull;
        }
        if(s.isEmpty()) {
            return Messages.DirectoryValidator_FolderName;
        }
        if(!s.endsWith("/")) {//$NON-NLS-1$
            return Messages.DirectoryValidator_MustEnd;
        }
        if(s.contains("//")) {//$NON-NLS-1$
            return Messages.DirectoryValidator_CanNotContain;
        }
        return null;
    }
}
