/*******************************************************************************
 * Copyright (c) 2007, 2017 Alphonse Van Assche and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileScanner;
import org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest;
import org.eclipse.ui.PlatformUI;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpecfileScannerTest extends AScannerTest {

    private IToken token;

    private TextAttribute ta;

    private static SpecfileScanner scanner;
    private static ColorRegistry colors;

    @BeforeClass
    public static void init() {
        scanner = new SpecfileScanner();
        colors = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    }

    @Override
    protected String getContents() {
        return "# This is a comment\nName: test\n%prep\n%{name}\n%define\n%if\nName=test";
    }

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
        assertEquals(colors.get(ISpecfileColorConstants.COMMENT), ta.getForeground());
    }

    @Test
    public void testTag() {
        token = getToken(2);
        assertTrue(token instanceof Token);
        assertEquals(5, rulesBasedScanner.getTokenLength());
        assertEquals(20, rulesBasedScanner.getTokenOffset());
        ta = (TextAttribute) token.getData();
        assertEquals(colors.get(ISpecfileColorConstants.TAGS), ta.getForeground());
    }

    @Test
    public void testSection() {
        token = getToken(9);
        assertTrue(token instanceof Token);
        assertEquals(5, rulesBasedScanner.getTokenLength());
        assertEquals(31, rulesBasedScanner.getTokenOffset());
        ta = (TextAttribute) token.getData();
        assertEquals(colors.get(ISpecfileColorConstants.SECTIONS), ta.getForeground());
    }

    @Test
    public void testMacro() {
        token = getToken(11);
        assertTrue(token instanceof Token);
        assertEquals(7, rulesBasedScanner.getTokenLength());
        assertEquals(37, rulesBasedScanner.getTokenOffset());
        ta = (TextAttribute) token.getData();
        assertEquals(colors.get(ISpecfileColorConstants.MACROS), ta.getForeground());
    }

    @Test
    public void testDefinedMacro() {
        token = getToken(13);
        assertTrue(token instanceof Token);
        assertEquals(7, rulesBasedScanner.getTokenLength());
        assertEquals(45, rulesBasedScanner.getTokenOffset());
        ta = (TextAttribute) token.getData();
        assertEquals(colors.get(ISpecfileColorConstants.MACROS), ta.getForeground());
    }

    @Test
    public void testKeyword() {
        token = getToken(15);
        assertTrue(token instanceof Token);
        assertEquals(3, rulesBasedScanner.getTokenLength());
        assertEquals(53, rulesBasedScanner.getTokenOffset());
        ta = (TextAttribute) token.getData();
        assertEquals(colors.get(ISpecfileColorConstants.KEYWORDS), ta.getForeground());
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
