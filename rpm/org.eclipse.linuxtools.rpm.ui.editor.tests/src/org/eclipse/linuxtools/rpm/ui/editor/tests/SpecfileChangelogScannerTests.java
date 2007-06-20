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
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileChangelogScanner;
import org.eclipse.swt.graphics.Color;

public class SpecfileChangelogScannerTests extends AScannerTest {

	private IToken token0;

	private IToken token;

	private TextAttribute ta;
	
	private SpecfileChangelogScanner scanner;
	
	public SpecfileChangelogScannerTests(String name) {
		super(name);
		scanner = new SpecfileChangelogScanner(new ColorManager());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getContents()
	 */
	protected String getContents() {
		return "%changelog <toto@test.com> - 1.1-4";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getScanner()
	 */
	protected RuleBasedScanner getScanner() {
		return scanner;
	}

	public void testSection() {
		try {
			token0 = getNextToken();
			assertTrue(token0 instanceof Token);
			assertEquals(10, rulesBasedScanner.getTokenLength());
			assertEquals(0, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.SECTIONS);
		} catch (Exception e) {
			fail();
		}
	}

	public void testMail() {
		try {
			token0 = getToken(3);
			assertTrue(token0 instanceof Token);
			assertEquals(15, rulesBasedScanner.getTokenLength());
			assertEquals(11, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.AUTHOR_MAIL);
		} catch (Exception e) {
			fail();
		}
	}

	public void testVerRel() {
		try {
			token0 = getToken(4);
			assertTrue(token0 instanceof Token);
			assertEquals(8, rulesBasedScanner.getTokenLength());
			assertEquals(26, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.VER_REL);
		} catch (Exception e) {
			fail();
		}
	}
}
