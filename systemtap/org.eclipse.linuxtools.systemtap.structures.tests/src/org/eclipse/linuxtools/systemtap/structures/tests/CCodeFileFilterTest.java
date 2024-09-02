/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
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
package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.systemtap.structures.CCodeFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CCodeFileFilterTest {

    @BeforeEach
    public void setUp(){
        filter = new CCodeFileFilter();
    }

    @Test
    public void testAccept() {
        assertFalse(filter.accept(null));
        assertFalse(filter.accept(new File("test")));
        assertFalse(filter.accept(new File("test.java")));
        assertTrue(filter.accept(new File("/root/")));
        assertTrue(filter.accept(new File("test.h")));
        assertTrue(filter.accept(new File("test.c")));
    }

    @Test
    public void testGetDescription() {
        filter.getDescription();
    }

    private CCodeFileFilter filter;
}
