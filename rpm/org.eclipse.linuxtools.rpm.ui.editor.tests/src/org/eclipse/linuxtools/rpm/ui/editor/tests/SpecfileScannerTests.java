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
import org.eclipse.linuxtools.rpm.ui.editor.scanners.SpecfileScanner;

public class SpecfileScannerTests extends AScannerTest {

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
	@Override
	protected String getContents() {
		return "# This is a comment" + "\n" + "Name: test" + "\n" + "%prep"
				+ "\n" + "%{name}" + "\n" + "%define" + "\n" + "%if" + "\n"
				+ "Name=test";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getScanner()
	 */
	@Override
	protected RuleBasedScanner getScanner() {
		return scanner;
	}

	public void testComment() {
			token = getNextToken();
			assertTrue(token instanceof Token);
			assertEquals(20, rulesBasedScanner.getTokenLength());
			assertEquals(0, rulesBasedScanner.getTokenOffset());
			ta = (TextAttribute) token.getData();
			assertEquals(ta.getForeground().getRGB(),
					ISpecfileColorConstants.COMMENT);
	}

	public void testTag() {
			token = getToken(2);
			assertTrue(token instanceof Token);
			assertEquals(5, rulesBasedScanner.getTokenLength());
			assertEquals(20, rulesBasedScanner.getTokenOffset());
			ta = (TextAttribute) token.getData();
			assertEquals(ta.getForeground().getRGB(),
					ISpecfileColorConstants.TAGS);
	}

	public void testSection() {
			token = getToken(9);
			assertTrue(token instanceof Token);
			assertEquals(5, rulesBasedScanner.getTokenLength());
			assertEquals(31, rulesBasedScanner.getTokenOffset());
			ta = (TextAttribute) token.getData();
			assertEquals(ta.getForeground().getRGB(),
					ISpecfileColorConstants.SECTIONS);
	}

	public void testMacro() {
			token = getToken(11);
			assertTrue(token instanceof Token);
			assertEquals(7, rulesBasedScanner.getTokenLength());
			assertEquals(37, rulesBasedScanner.getTokenOffset());
			ta = (TextAttribute) token.getData();
			assertEquals(ta.getForeground().getRGB(),
					ISpecfileColorConstants.MACROS);
	}

	public void testDefinedMacro() {
			token = getToken(13);
			assertTrue(token instanceof Token);
			assertEquals(7, rulesBasedScanner.getTokenLength());
			assertEquals(45, rulesBasedScanner.getTokenOffset());
			ta = (TextAttribute) token.getData();
			assertEquals(ta.getForeground().getRGB(),
					ISpecfileColorConstants.MACROS);
	}

	public void testKeyword() {
			token = getToken(15);
			assertTrue(token instanceof Token);
			assertEquals(3, rulesBasedScanner.getTokenLength());
			assertEquals(53, rulesBasedScanner.getTokenOffset());
			ta = (TextAttribute) token.getData();
			assertEquals(ta.getForeground().getRGB(),
					ISpecfileColorConstants.KEYWORDS);
	}

	/**
	 * Check that defines are correctly scanned.
	 * See bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=182302 
	 * printscreen: https://bugs.eclipse.org/bugs/attachment.cgi?id=63722
	 */
	public void testDefineCorreclyScanned() {
			token = getToken(17);
			assertTrue(token instanceof Token);
			assertEquals(1, rulesBasedScanner.getTokenLength());
	}

}
