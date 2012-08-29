package org.eclipse.linuxtools.systemtap.ui.structures;

import java.io.File;

import org.eclipse.linuxtools.systemtap.ui.structures.CCodeFileFilter;

import junit.framework.TestCase;

public class CCodeFileFilterTest extends TestCase {
	public CCodeFileFilterTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		filter = new CCodeFileFilter();
	}

	public void testAccept() {
		assertFalse(filter.accept(null));
		assertFalse(filter.accept(new File("test")));
		assertFalse(filter.accept(new File("test.java")));
		assertTrue(filter.accept(new File("/root/")));
		assertTrue(filter.accept(new File("test.h")));
		assertTrue(filter.accept(new File("test.c")));
	}
	
	public void testGetDescription() {
		filter.getDescription();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	CCodeFileFilter filter;
}
