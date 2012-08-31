package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import org.eclipse.linuxtools.systemtap.ui.structures.validators.IntegerValidator;

import junit.framework.TestCase;

public class IntegerValidatorTest extends TestCase {
	public IntegerValidatorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

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
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
