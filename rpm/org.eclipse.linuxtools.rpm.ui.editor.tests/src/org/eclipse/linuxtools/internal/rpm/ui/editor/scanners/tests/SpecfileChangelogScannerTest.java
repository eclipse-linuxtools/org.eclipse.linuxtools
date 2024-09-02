/*******************************************************************************
 * Copyright (c) 2007, 2018 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileChangelogScanner;
import org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SpecfileChangelogScannerTest extends AScannerTest {

	private IToken token;

	private TextAttribute ta;

	private static SpecfileChangelogScanner scanner;
	private static ColorRegistry colors;

	@BeforeAll
	public static void init() {
		scanner = new SpecfileChangelogScanner();
		colors = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
	}

	@Override
	protected String getContents() {
		return "%changelog <toto@test.com> - 1.1-4";
	}

	@Override
	protected RuleBasedScanner getScanner() {
		return scanner;
	}

	@Test
	public void testSection() {
		token = getNextToken();
		assertTrue(token instanceof Token);
		assertEquals(10, rulesBasedScanner.getTokenLength());
		assertEquals(0, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ta.getForeground(), colors.get(ISpecfileColorConstants.SECTIONS));
	}

	@Test
	public void testMail() {
		token = getToken(3);
		assertTrue(token instanceof Token);
		assertEquals(15, rulesBasedScanner.getTokenLength());
		assertEquals(11, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ta.getForeground(), colors.get(ISpecfileColorConstants.AUTHOR_MAIL));
	}

	@Test
	public void testVerRel() {
		token = getToken(4);
		assertTrue(token instanceof Token);
		assertEquals(8, rulesBasedScanner.getTokenLength());
		assertEquals(26, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ta.getForeground(), colors.get(ISpecfileColorConstants.VER_REL));
	}
}
