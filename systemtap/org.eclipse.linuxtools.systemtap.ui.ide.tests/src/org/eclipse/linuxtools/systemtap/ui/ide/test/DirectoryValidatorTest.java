package org.eclipse.linuxtools.systemtap.ui.ide.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.DirectoryValidator;
import org.junit.Test;

public class DirectoryValidatorTest {

	@Test
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
	
}
