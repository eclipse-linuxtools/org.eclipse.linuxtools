package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import org.eclipse.linuxtools.systemtap.ui.structures.validators.DirectoryValidator;

import junit.framework.TestCase;

public class DirectoryValidatorTest extends TestCase {
	public DirectoryValidatorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testIsValid() {
		DirectoryValidator validator = new DirectoryValidator();
		
		assertNotNull("Null not valid", validator.isValid(null));
		assertNotNull("Blank not valid", validator.isValid(""));
		assertNotNull("String valid", validator.isValid("sdf"));
		assertNotNull("// not valid", validator.isValid("//"));
		assertNotNull("/root/ad not valid", validator.isValid("/root/ad"));
		assertNull("/ is valid", validator.isValid("/"));
		assertNull("/root/ is valid", validator.isValid("/root/"));
		assertNull("/blah/bld/ is valid", validator.isValid("/blah/bld/"));
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
