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
package org.eclipse.linuxtools.rpm.ui.editor.tests.scanners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ColorManager;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileScanner;
import org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpecfileScannerTest extends AScannerTest {

	private IToken token;

	private TextAttribute ta;

	private static SpecfileScanner scanner;

	@BeforeClass
	public static void init() {
		scanner = new SpecfileScanner(new ColorManager());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getContents()
	 */
	@Override
	protected String getContents() {
		return "# This is a comment\nName: test\n%prep\n%{name}\n%define\n%if\nName=test";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getScanner()
	 */
	@Override
	protected RuleBasedScanner getScanner() {
		return scanner;
	}

	@Test
	public void testComment() {
		token = getNextToken();
		assertTrue(token instanceof Token);
		assertEquals(20, rulesBasedScanner.getTokenLength());
		assertEquals(0, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ISpecfileColorConstants.COMMENT, ta.getForeground().getRGB());
	}

	@Test
	public void testTag() {
		token = getToken(2);
		assertTrue(token instanceof Token);
		assertEquals(5, rulesBasedScanner.getTokenLength());
		assertEquals(20, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ISpecfileColorConstants.TAGS, ta.getForeground().getRGB());
	}

	@Test
	public void testSection() {
		token = getToken(9);
		assertTrue(token instanceof Token);
		assertEquals(5, rulesBasedScanner.getTokenLength());
		assertEquals(31, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ISpecfileColorConstants.SECTIONS, ta.getForeground().getRGB());
	}

	@Test
	public void testMacro() {
		token = getToken(11);
		assertTrue(token instanceof Token);
		assertEquals(7, rulesBasedScanner.getTokenLength());
		assertEquals(37, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ISpecfileColorConstants.MACROS, ta.getForeground().getRGB());
	}

	@Test
	public void testDefinedMacro() {
		token = getToken(13);
		assertTrue(token instanceof Token);
		assertEquals(7, rulesBasedScanner.getTokenLength());
		assertEquals(45, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ISpecfileColorConstants.MACROS, ta.getForeground().getRGB());
	}

	@Test
	public void testKeyword() {
		token = getToken(15);
		assertTrue(token instanceof Token);
		assertEquals(3, rulesBasedScanner.getTokenLength());
		assertEquals(53, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ISpecfileColorConstants.KEYWORDS, ta.getForeground().getRGB());
	}

	/**
	 * Check that defines are correctly scanned. See bug:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=182302 printscreen:
	 * https://bugs.eclipse.org/bugs/attachment.cgi?id=63722
	 */
	@Test
	public void testDefineCorreclyScanned() {
		token = getToken(17);
		assertTrue(token instanceof Token);
		assertEquals(1, rulesBasedScanner.getTokenLength());
	}

}
