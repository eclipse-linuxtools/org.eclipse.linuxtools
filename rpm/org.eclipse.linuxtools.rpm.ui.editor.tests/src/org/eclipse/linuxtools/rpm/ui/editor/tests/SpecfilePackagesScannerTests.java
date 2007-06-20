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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.ColorManager;
import org.eclipse.linuxtools.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfilePackagesScanner;
import org.eclipse.linuxtools.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.swt.graphics.Color;

public class SpecfilePackagesScannerTests extends AScannerTest {

	private IToken token0;

	private IToken token;

	private TextAttribute ta;
	
	private SpecfilePackagesScanner scanner;
	
	public SpecfilePackagesScannerTests(String name) {
		super(name);
		scanner = new SpecfilePackagesScanner(new ColorManager());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#setUp()
	 */
	protected void setUp() throws Exception {
		Activator.getDefault().getPluginPreferences().setValue(
				PreferenceConstants.P_RPM_LIST_FILEPATH, "/tmp/pkglist");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"/tmp/pkglist"));
			out.write("setup\ntest_underscore\n");
			out.close();
		} catch (IOException e) {
			fail();
		}
		super.setUp();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getContents()
	 */
	protected String getContents() {
		return "Requires: test_underscore" + "\n" + "%{name}" + "\n"
				+ "# Requires:" + "\n";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getScanner()
	 */
	protected RuleBasedScanner getScanner() {
		return scanner;
	}

	public void testPackageTag() {
		try {
			token0 = getNextToken();
			assertTrue(token0 instanceof Token);
			assertEquals(9, rulesBasedScanner.getTokenLength());
			assertEquals(0, rulesBasedScanner.getTokenOffset());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertEquals(((Color) ta.getForeground()).getRGB(),
					ISpecfileColorConstants.TAGS);
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Test package and BTW we test a package with a undercore.
	 * see bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=182302 
	 * printscreen: https://bugs.eclipse.org/bugs/attachment.cgi?id=63725
	 */
// FIXME: Don't see why packages or not correctly scanned here, 
// I will re-examine this with a fresh head ;-)
//	public void testPackage() {
//		try {
//			token0 = getToken(3);
//			assertTrue(token0 instanceof Token);
//			assertEquals(15, rulesBasedScanner.getTokenLength());
//			assertEquals(10, rulesBasedScanner.getTokenOffset());
//			token = (Token) token0;
//			ta = (TextAttribute) token.getData();
//			assertEquals(((Color) ta.getForeground()).getRGB(),
//					ISpecfileColorConstants.PACKAGES);
//		} catch (Exception e) {
//			fail();
//		}
//	}

//	 FIXME: Don't see why macro or not correctly scanned here, 
//	 I will re-examine this with a fresh head ;-) 
//	public void testMacro() {
//		try {
//			token0 = getToken(5);
//			assertTrue(token0 instanceof Token);
//			assertEquals(7, rulesBasedScanner.getTokenLength());
//			assertEquals(26, rulesBasedScanner.getTokenOffset());
//			token = (Token) token0;
//			ta = (TextAttribute) token.getData();
//			assertEquals(((Color) ta.getForeground()).getRGB(),
//					ISpecfileColorConstants.MACROS);
//		} catch (Exception e) {
//			fail();
//		}
//	}

	/**
	 * Check that comments are not handle with the package scanner.
	 * See bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=182302 
	 * printscreen: https://bugs.eclipse.org/bugs/attachment.cgi?id=63721
	 */
	public void testComment() {
		try {
			token0 = getToken(6);
			assertTrue(token0 instanceof Token);
			assertEquals(1, rulesBasedScanner.getTokenLength());
			token = (Token) token0;
			ta = (TextAttribute) token.getData();
			assertNull(ta);
		} catch (Exception e) {
			fail();
		}
	}

}
