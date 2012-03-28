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

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.rpm.ui.editor.RpmMacroProposalsList;
import org.eclipse.linuxtools.rpm.core.utils.Utils;

public class RpmMacroProposalsListTest extends TestCase {

	RpmMacroProposalsList macroProposalsList;

	@Override
	protected void setUp() throws Exception {
		macroProposalsList = new RpmMacroProposalsList();
	}

	public final void testBuildMacroList() {
		macroProposalsList.buildMacroList();
		if (!macroProposalsList.findKey("%_libdir"))
			fail("buildMacroList faild, %_libdir macro was not found!");
	}

	public final void testGetProposals() {
		Map<String, String> proposals = macroProposalsList
				.getProposals("%_libdir");
		if (proposals.size() != 1) {
			fail("getProposals faild, %_libdir macro was retrive as proposals");
		}
	}

	public final void testGetProposals2() {
		Map<String, String> proposals = macroProposalsList
				.getProposals("%_unexistingmacro");
		if (proposals.size() != 0) {
			fail("getProposals faild, %_unexistingmacro don't can exist");
		}
	}

	public final void testGetValue() {
		if (macroProposalsList.getValue("_libdir").indexOf("lib") == -1) {
			fail("getValue faild, %_libdir value don't end with '%{_lib}'");
		}
	}

	public final void testGetValue2() {
		if (macroProposalsList.getValue("_unexistingmacro") != null) {
			fail("getValue faild, %_libdir value don't end with '%{_lib}'");
		}
	}

	public final void testGetMacroEval() {
		if (Utils.fileExist("/bin/rpm")) {
			if (RpmMacroProposalsList.getMacroEval("%_libdir").indexOf("lib") == -1) {
				fail("getMacroEval faild, eval don't end with 'lib'");
			}
		}
	}

	public final void testGetMacroEval2() {
		if (!RpmMacroProposalsList.getMacroEval("%_unexistingmacro").equals(
				"%_unexistingmacro")) {
			fail("getMacroEval faild, eval don't contain %_unexistingmacro macro name");
		}
	}

}
