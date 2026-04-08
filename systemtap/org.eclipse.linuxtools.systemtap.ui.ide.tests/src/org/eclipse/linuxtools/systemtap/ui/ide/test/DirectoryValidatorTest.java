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

import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.DirectoryValidator;
import org.junit.jupiter.api.Test;

public class DirectoryValidatorTest {

    @Test
    public void testIsValid() {
        DirectoryValidator validator = new DirectoryValidator();

        assertNotNull(validator.isValid(null), "Null not valid");
        assertNotNull(validator.isValid(""), "Blank not valid");
        assertNotNull(validator.isValid("sdf"), "String valid");
        assertNotNull(validator.isValid("//"), "// not valid");
        assertNotNull(validator.isValid("/root/ad"), "/root/ad not valid");
        assertNull(validator.isValid("/"), "/ is valid");
        assertNull(validator.isValid("/root/"), "/root/ is valid");
        assertNull(validator.isValid("/blah/bld/"), "/blah/bld/ is valid");
    }

}
