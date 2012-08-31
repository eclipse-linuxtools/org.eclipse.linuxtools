package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;


public class MultiValidatorTest {

	@Before
	public void setUp() {
		validator = new MultiValidator();
		validator.addValidator(new NumberValidator());
		validator.addValidator(new NumberValidator());
	}

	@Test
	public void testAddValidator() {
		validator.addValidator(new IntegerValidator());
		validator.addValidator(new ConditionalExpressionValidator());
		validator.addValidator(new DirectoryValidator());
		validator.addValidator(new NumberValidator());
	}
	
	@Test
	public void testIsValid() {
		assertNotNull("Null not a number", validator.isValid(null));
		assertNotNull("Blank not a number", validator.isValid(""));
		assertNotNull("String not a number", validator.isValid("sdf"));
		assertNotNull("Not a number", validator.isValid("2.2.2"));
		assertNull("Integer is valid", validator.isValid("3"));
		assertNull("Double is valid", validator.isValid("2.2"));
		assertNull("Double is a number", validator.isValid(".3"));
	}

	MultiValidator validator;
}
