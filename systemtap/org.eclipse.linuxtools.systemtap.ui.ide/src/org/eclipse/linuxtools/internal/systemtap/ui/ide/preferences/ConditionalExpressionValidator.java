/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import org.eclipse.jface.dialogs.IInputValidator;



public class ConditionalExpressionValidator implements IInputValidator {

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
            return Messages.ConditionalExpressionValidator_NotNull;
        }
        if(!s.startsWith("if")) {//$NON-NLS-1$
            return Messages.ConditionalExpressionValidator_MustStartWith;
        }
        if(!s.contains("(")) {//$NON-NLS-1$
            return Messages.ConditionalExpressionValidator_MustContain;
        }
        if(!s.endsWith(")")) {//$NON-NLS-1$
            return Messages.ConditionalExpressionValidator_MustEndWith;
        }
        if(s.length() < 5) {
            return Messages.ConditionalExpressionValidator_MustEnterSomething;
        }
        return null;
    }
}
