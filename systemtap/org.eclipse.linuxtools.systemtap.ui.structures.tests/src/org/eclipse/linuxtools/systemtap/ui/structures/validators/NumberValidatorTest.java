package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;


public class NumberValidatorTest {

	@Test
	public void testIsValid() {
		NumberValidator validator = new NumberValidator();

		assertNotNull("Null not a number", validator.isValid(null));
		assertNotNull("Blank not a number", validator.isValid(""));
		assertNotNull("String not a number", validator.isValid("sdf"));
		assertNotNull("Not a number", validator.isValid("2.2.2"));
		assertNull("Integer is valid", validator.isValid("3"));
		assertNull("Double is valid", validator.isValid("2.2"));
		assertNull("Double is a number", validator.isValid(".3"));
	}
}
