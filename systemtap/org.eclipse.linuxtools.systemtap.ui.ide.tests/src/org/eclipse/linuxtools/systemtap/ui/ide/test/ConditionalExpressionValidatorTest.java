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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.ConditionalExpressionValidator;
import org.junit.Test;

public class ConditionalExpressionValidatorTest {

    @Test
    public void testIsValid() {
        ConditionalExpressionValidator validator = new ConditionalExpressionValidator();

        assertNotNull("Null not valid", validator.isValid(null));
        assertNotNull("Blank not valid", validator.isValid(""));
        assertNotNull("String not valid", validator.isValid("sdf"));
        assertNotNull("if not valid", validator.isValid("if"));
        assertNotNull("if( not valid", validator.isValid("if("));
        assertNotNull("if) not valid", validator.isValid("if)"));
        assertNotNull("if() not valid", validator.isValid("if()"));
        assertNull("if(a) valid", validator.isValid("if(a)"));
        assertNull("if (a) valid", validator.isValid("if ()"));
        assertNull("if(a=b) valid", validator.isValid("if(a=b)"));
        assertNotNull("if(a)b not valid", validator.isValid("if(a)d"));
    }

}
