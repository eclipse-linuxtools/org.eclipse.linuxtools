/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.rpm.ui.editor.ColorManager;
import org.eclipse.linuxtools.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileScanner;
import org.eclipse.swt.graphics.Color;

public class SpecfileScannerTests extends AScannerTest {

	private IToken token0;

	private IToken token;

	private TextAttribute ta;
	
	private SpecfileScanner scanner;
	
	public SpecfileScannerTests(String name) {
		super(name);
		scanner = new SpecfileScanner(new ColorManager());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getContents()
	 */
	protected String getContents() {
		return "# This is a comment" + "\n" + "Name: test" + "\n" + "%prep"
				+ "\n" + "%{name}" + "\n" + "%define" + "\n" + "%if" + "\n"
				+ "Name=test";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getScanner()
	 */
	protected RuleBasedScanner getScanner() {
		return scanner;
	}

	public void testComment() {
		try {
			token0 = getNextToken();
			assertTrue(token0 instanceof Token);
			assertEquals(20, rulesBasedScanner.getTokenLength());
			assertEquals(0, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.COMMENT);
		} catch (Exception e) {
			fail();
		}
	}

	public void testTag() {
		try {
			token0 = getToken(2);
			assertTrue(token0 instanceof Token);
			assertEquals(5, rulesBasedScanner.getTokenLength());
			assertEquals(20, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.TAGS);
		} catch (Exception e) {
			fail();
		}
	}

	public void testSection() {
		try {
			token0 = getToken(9);
			assertTrue(token0 instanceof Token);
			assertEquals(5, rulesBasedScanner.getTokenLength());
			assertEquals(31, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.SECTIONS);
		} catch (Exception e) {
			fail();
		}
	}

	public void testMacro() {
		try {
			token0 = getToken(11);
			assertTrue(token0 instanceof Token);
			assertEquals(7, rulesBasedScanner.getTokenLength());
			assertEquals(37, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.MACROS);
		} catch (Exception e) {
			fail();
		}
	}

	public void testDefinedMacro() {
		try {
			token0 = getToken(13);
			assertTrue(token0 instanceof Token);
			assertEquals(7, rulesBasedScanner.getTokenLength());
			assertEquals(45, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.MACROS);
		} catch (Exception e) {
			fail();
		}
	}

	public void testKeyword() {
		try {
			token0 = getToken(15);
			assertTrue(token0 instanceof Token);
			assertEquals(3, rulesBasedScanner.getTokenLength());
			assertEquals(53, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.KEYWORDS);
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Check that defines are correctly scanned.
	 * See bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=182302 
	 * printscreen: https://bugs.eclipse.org/bugs/attachment.cgi?id=63722
	 */
	public void testDefineCorreclyScanned() {
		try {
			token0 = getToken(17);
			assertTrue(token0 instanceof Token);
			assertEquals(1, rulesBasedScanner.getTokenLength());
		} catch (Exception e) {
			fail();
		}
	}

}
