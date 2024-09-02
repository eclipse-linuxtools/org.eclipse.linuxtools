/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfilePatchMacro;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.junit.jupiter.api.Test;

public class PatchApplicationTest extends FileTestCase {
	@Test
	public void testParsePatchApplication() {
		String specText = "Patch3: somefilesomewhere.patch\n%patch3";
		String testText = "%patch3";
		newFile(specText);
		SpecfileElement element = parser.parseLine(testText, specfile, 1);
		assertEquals(SpecfilePatchMacro.class, element.getClass());
		assertEquals(3, ((SpecfilePatchMacro) element).getPatchNumber());
	}

	@Test
	public void testPatchLineNumber() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3";
		newFile(specText);
		SpecfileSource thisPatch = specfile.getPatch(3);
		List<Integer> usedList = new ArrayList<>(1);
		usedList.add(Integer.valueOf(1));
		assertEquals(thisPatch.getLinesUsed(), usedList);
	}

	@Test
	public void testMultiplePatchLineNumbers() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3" + "\n" + "blah" + "\n" + "%patch3";
		newFile(specText);
		SpecfileSource thisPatch = specfile.getPatch(3);
		List<Integer> usedList = new ArrayList<>(2);
		usedList.add(Integer.valueOf(1));
		usedList.add(Integer.valueOf(3));
		assertEquals(thisPatch.getLinesUsed(), usedList);
	}
}
