/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.ide.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.ConditionalExpressionValidator;
import org.junit.jupiter.api.Test;

public class ConditionalExpressionValidatorTest {

    @Test
    public void testIsValid() {
        ConditionalExpressionValidator validator = new ConditionalExpressionValidator();

        assertNotNull(validator.isValid(null), "Null not valid");
        assertNotNull(validator.isValid(""), "Blank not valid");
        assertNotNull(validator.isValid("sdf"), "String not valid");
        assertNotNull(validator.isValid("if"), "if not valid");
        assertNotNull(validator.isValid("if("), "if( not valid");
        assertNotNull(validator.isValid("if)"), "if) not valid");
        assertNotNull(validator.isValid("if()"), "if() not valid");
        assertNull(validator.isValid("if(a)"), "if(a) valid");
        assertNull(validator.isValid("if ()"), "if (a) valid");
        assertNull(validator.isValid("if(a=b)"), "if(a=b) valid");
        assertNotNull(validator.isValid("if(a)d"), "if(a)b not valid");
    }

}
