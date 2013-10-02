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
package org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ColorManager;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePackagesScanner;
import org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpecfilePackagesScannerTest extends AScannerTest {

	private IToken token;

	private TextAttribute ta;

	private static SpecfilePackagesScanner scanner;

	@BeforeClass
	public static void init() {
		Activator.getDefault().getPreferenceStore().setValue(
				PreferenceConstants.P_RPM_LIST_FILEPATH, "/tmp/pkglist1");
		Activator.getDefault().getPreferenceStore().setValue(
				PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD, false);
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"/tmp/pkglist1"));
			out.write("setup\ntest_underscore\n");
			out.close();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		// we ensure that proposals are rebuild
		Activator.packagesList = null;
		scanner = new SpecfilePackagesScanner(new ColorManager());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest#getContents()
	 */
	@Override
	protected String getContents() {
		return "Requires: test_underscore\n%{name}\n# Requires:\n";
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
	public void testPackageTag() {
		token = getNextToken();
		assertTrue(token instanceof Token);
		assertEquals(9, rulesBasedScanner.getTokenLength());
		assertEquals(0, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ta.getForeground().getRGB(), ISpecfileColorConstants.TAGS);
	}

	/**
	 * We test a package with a underscore. see bug:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=182302 
	 */
	@Test
	public void testPackage() {
		token = getToken(2);
		assertTrue(token instanceof Token);
		assertEquals(16, rulesBasedScanner.getTokenLength());
		assertEquals(9, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals((ta.getForeground()).getRGB(),
				ISpecfileColorConstants.PACKAGES);
	}

	@Test
	public void testMacro() {
		token = getToken(4);
		assertTrue(token instanceof Token);
		assertEquals(7, rulesBasedScanner.getTokenLength());
		assertEquals(26, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals((ta.getForeground()).getRGB(),
				ISpecfileColorConstants.MACROS);
	}
	/**
	 * Check that comments are not handle with the package scanner. See bug:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=182302 
	 */
	@Test
	public void testComment() {
		token = getToken(6);
		assertTrue(token instanceof Token);
		assertEquals(1, rulesBasedScanner.getTokenLength());
		ta = (TextAttribute) token.getData();
		assertNull(ta);
	}

}
