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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePackagesScanner;
import org.eclipse.linuxtools.rpm.ui.editor.tests.AScannerTest;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SpecfilePackagesScannerTest extends AScannerTest {

	private IToken token;

	private TextAttribute ta;

	private static SpecfilePackagesScanner scanner;
	private static final String P_RPM_LIST_FILEPATH = "/tmp/pkglist1";
	private static ColorRegistry colors;

	@BeforeAll
	public static void init() throws IOException {
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_RPM_LIST_FILEPATH,
				P_RPM_LIST_FILEPATH);
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD, false);

		Files.write(Paths.get(P_RPM_LIST_FILEPATH), "setup\ntest_underscore\n".getBytes());
		// we ensure that proposals are rebuild
		Activator.packagesList = null;
		scanner = new SpecfilePackagesScanner();
		colors = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
	}

	@AfterAll
	public static void cleanUp() throws IOException {
		Files.deleteIfExists(Paths.get(P_RPM_LIST_FILEPATH));
	}

	@Override
	protected String getContents() {
		return "Requires: test_underscore\n%{name}\n# Requires:\n";
	}

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
		assertEquals(ta.getForeground(), colors.get(ISpecfileColorConstants.TAGS));
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
		assertEquals(ta.getForeground(), colors.get(ISpecfileColorConstants.PACKAGES));
	}

	@Test
	public void testMacro() {
		token = getToken(4);
		assertTrue(token instanceof Token);
		assertEquals(7, rulesBasedScanner.getTokenLength());
		assertEquals(26, rulesBasedScanner.getTokenOffset());
		ta = (TextAttribute) token.getData();
		assertEquals(ta.getForeground(), colors.get(ISpecfileColorConstants.MACROS));
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
		assertEquals(ta.getForeground(), colors.get(ISpecfileColorConstants.DEFAULT));
	}

}
