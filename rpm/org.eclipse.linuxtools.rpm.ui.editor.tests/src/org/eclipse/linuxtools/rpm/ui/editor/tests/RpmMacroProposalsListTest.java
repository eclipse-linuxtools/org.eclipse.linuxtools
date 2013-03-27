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

import static org.junit.Assert.fail;

import java.util.Map;

import org.eclipse.linuxtools.internal.rpm.ui.editor.RpmMacroProposalsList;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.junit.Before;
import org.junit.Test;

public class RpmMacroProposalsListTest {

	RpmMacroProposalsList macroProposalsList;

	@Before
	public void setUp() {
		macroProposalsList = new RpmMacroProposalsList();
	}

	@Test
	public final void testBuildMacroList() {
		macroProposalsList.buildMacroList();
		if (!macroProposalsList.findKey("%_libdir"))
			fail("buildMacroList faild, %_libdir macro was not found!");
	}

	@Test
	public final void testGetProposals() {
		Map<String, String> proposals = macroProposalsList
				.getProposals("%_libdir");
		if (proposals.size() != 1) {
			fail("getProposals faild, %_libdir macro was retrive as proposals");
		}
	}

	@Test
	public final void testGetProposals2() {
		Map<String, String> proposals = macroProposalsList
				.getProposals("%_unexistingmacro");
		if (proposals.size() != 0) {
			fail("getProposals faild, %_unexistingmacro don't can exist");
		}
	}

	@Test
	public final void testGetValue() {
		if (macroProposalsList.getValue("_libdir").indexOf("lib") == -1) {
			fail("getValue faild, %_libdir value don't end with '%{_lib}'");
		}
	}

	@Test
	public final void testGetValue2() {
		if (macroProposalsList.getValue("_unexistingmacro") != null) {
			fail("getValue faild, %_libdir value don't end with '%{_lib}'");
		}
	}

	@Test
	public final void testGetMacroEval() {
		if (Utils.fileExist("/bin/rpm")) {
			if (RpmMacroProposalsList.getMacroEval("%_libdir").indexOf("lib") == -1) {
				fail("getMacroEval faild, eval don't end with 'lib'");
			}
		}
	}

	@Test
	public final void testGetMacroEval2() {
		if (!RpmMacroProposalsList.getMacroEval("%_unexistingmacro").equals(
				"%_unexistingmacro")) {
			fail("getMacroEval faild, eval don't contain %_unexistingmacro macro name");
		}
	}

}
