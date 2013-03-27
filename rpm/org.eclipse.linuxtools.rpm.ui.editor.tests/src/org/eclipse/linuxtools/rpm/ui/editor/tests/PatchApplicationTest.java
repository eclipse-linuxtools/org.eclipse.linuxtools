/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfilePatchMacro;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.junit.Test;

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
		List<Integer> usedList = new ArrayList<Integer>(1);
		usedList.add(Integer.valueOf(1));
		assertEquals(thisPatch.getLinesUsed(), usedList);
	}
	@Test
	public void testMultiplePatchLineNumbers() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3"
				+ "\n" + "blah" + "\n" + "%patch3";
		newFile(specText);
		SpecfileSource thisPatch = specfile.getPatch(3);
		List<Integer> usedList = new ArrayList<Integer>(2);
		usedList.add(Integer.valueOf(1));
		usedList.add(Integer.valueOf(3));
		assertEquals(thisPatch.getLinesUsed(), usedList);
	}
}
