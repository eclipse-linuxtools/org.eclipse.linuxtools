/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.systemtap.structures.CCodeFileFilter;
import org.junit.Before;
import org.junit.Test;

public class CCodeFileFilterTest {

	@Before
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

	CCodeFileFilter filter;
}
