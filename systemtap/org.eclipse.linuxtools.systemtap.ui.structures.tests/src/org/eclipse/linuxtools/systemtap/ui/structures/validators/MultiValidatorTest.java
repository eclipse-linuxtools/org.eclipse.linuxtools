package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import junit.framework.TestCase;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.ConditionalExpressionValidator;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.DirectoryValidator;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.IntegerValidator;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.MultiValidator;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.NumberValidator;


public class MultiValidatorTest extends TestCase {
	public MultiValidatorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		validator = new MultiValidator();
		validator.addValidator(new NumberValidator());
		validator.addValidator(new NumberValidator());
	}

	public void testAddValidator() {
		validator.addValidator(new IntegerValidator());
		validator.addValidator(new ConditionalExpressionValidator());
		validator.addValidator(new DirectoryValidator());
		validator.addValidator(new NumberValidator());
	}
	
	public void testIsValid() {
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

	MultiValidator validator;
}
