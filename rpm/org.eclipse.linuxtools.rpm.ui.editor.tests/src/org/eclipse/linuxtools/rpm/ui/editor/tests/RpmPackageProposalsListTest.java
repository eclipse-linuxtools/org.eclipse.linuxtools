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
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.RpmPackageProposalsList;
import org.eclipse.linuxtools.rpm.ui.editor.Utils;
import org.eclipse.linuxtools.rpm.ui.editor.preferences.PreferenceConstants;

public class RpmPackageProposalsListTest extends TestCase {

	private RpmPackageProposalsList packageProposalsList;

	@Override
	protected void setUp() throws Exception {
		Activator.getDefault().getPreferenceStore().setValue(
				PreferenceConstants.P_RPM_LIST_FILEPATH, "/tmp/pkglist");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"/tmp/pkglist"));
			out.write("setup\ntest\nrpm\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		packageProposalsList = new RpmPackageProposalsList();
	}

	public final void testGetProposals() {
		List<String[]> proposals = packageProposalsList.getProposals("setup");
		if (!(proposals.size() == 1)) {
			fail("getProposals failed, setup package was retrieve as proposals!");
		}
	}

	public final void testGetValue() {
		if (Utils.fileExist("/bin/rpm")) {
			if (!packageProposalsList.getValue("rpm").startsWith(
					"<b>Name: </b>rpm")) {
				fail("getValue failed, rpm package info doesn't start with '<b>Name:<b> rpm'");
			}
		}
	}

	public final void testGetValue2() {
		if (packageProposalsList.getValue("test").indexOf("test") == -1) {
			fail("getValue failed, test package info doesn't contain 'test'");
		}
	}

	public final void testGetRpmInfo() {
		if (Utils.fileExist("/bin/rpm")) {
			if (!packageProposalsList.getRpmInfo("rpm").startsWith(
					"<b>Name: </b>rpm")) {
				fail("getRpmInfo failed, rpm package info doesn't start with '<b>Name:<b> rpm'");
			}
		}
	}

	public final void testGetRpmInfo2() {
		if (packageProposalsList.getValue("test").indexOf("test") == -1) {
			fail("getRpmInfo failed, test package info doesn't contain 'test'");
		}
	}

}
