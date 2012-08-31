package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class IntegerValidatorTest {

	@Test
	public void testIsValid() {
		IntegerValidator validator = new IntegerValidator();

		assertNotNull("Null not an iteger", validator.isValid(null));
		assertNotNull("Blank not an iteger", validator.isValid(""));
		assertNotNull("String not an iteger", validator.isValid("sdf"));
		assertNotNull("Not an iteger", validator.isValid("2.2.2"));
		assertNotNull("Double is not valid", validator.isValid("2.2"));
		assertNotNull("Double is not valid", validator.isValid(".3"));
		assertNull("Integer is valid", validator.isValid("3"));
		assertNull("Integer is valid", validator.isValid("343"));
	}

}
