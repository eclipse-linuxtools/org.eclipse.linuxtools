package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import org.eclipse.linuxtools.systemtap.ui.structures.validators.ConditionalExpressionValidator;

import junit.framework.TestCase;

public class ConditionalExpressionValidatorTest extends TestCase {
	public ConditionalExpressionValidatorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testIsValid() {
		ConditionalExpressionValidator validator = new ConditionalExpressionValidator();
		
		assertNotNull("Null not valid", validator.isValid(null));
		assertNotNull("Blank not valid", validator.isValid(""));
		assertNotNull("String not valid", validator.isValid("sdf"));
		assertNotNull("if not valid", validator.isValid("if"));
		assertNotNull("if( not valid", validator.isValid("if("));
		assertNotNull("if) not valid", validator.isValid("if)"));
		assertNotNull("if() not valid", validator.isValid("if()"));
		assertNull("if(a) valid", validator.isValid("if(a)"));
		assertNull("if (a) valid", validator.isValid("if ()"));
		assertNull("if(a=b) valid", validator.isValid("if(a=b)"));
		assertNotNull("if(a)b not valid", validator.isValid("if(a)d"));
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
