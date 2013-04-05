package org.eclipse.linuxtools.systemtap.ui.ide.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.ConditionalExpressionValidator;
import org.junit.Test;

public class ConditionalExpressionValidatorTest {

	@Test
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

}
