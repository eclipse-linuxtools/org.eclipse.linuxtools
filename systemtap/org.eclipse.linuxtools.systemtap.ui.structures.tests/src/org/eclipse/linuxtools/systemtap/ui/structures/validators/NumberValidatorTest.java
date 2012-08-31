package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import org.eclipse.linuxtools.systemtap.ui.structures.validators.NumberValidator;

import junit.framework.TestCase;

public class NumberValidatorTest extends TestCase {
	public NumberValidatorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

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
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
