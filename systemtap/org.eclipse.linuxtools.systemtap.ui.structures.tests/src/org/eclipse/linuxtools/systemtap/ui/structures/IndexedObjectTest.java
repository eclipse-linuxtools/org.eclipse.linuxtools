package org.eclipse.linuxtools.systemtap.ui.structures;

import org.eclipse.linuxtools.systemtap.ui.structures.IndexedObject;

import junit.framework.TestCase;

public class IndexedObjectTest extends TestCase {
	public IndexedObjectTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		one = new IndexedObject(1, "one");
		two = new IndexedObject(2, "two");
		three = new IndexedObject(3, "three");
	}
	
	public void testToString() {
		assertEquals("one", one.toString());
		assertEquals("two", two.toString());
		assertEquals("three", three.toString());
	}
	
	public void testCompareTo() {
		assertEquals(0, one.compareTo(one));
		assertTrue(-1 >= one.compareTo(two));
		assertTrue(1 <= three.compareTo(one));
		assertEquals(0, one.compareTo(null));
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	IndexedObject one, two, three;
}
