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

import java.util.Collection;

import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSource;

public class LinePositionTests extends FileTestCase {

	public void testLineNumber() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n"
				+ "Patch2: someotherfile.patch";

		newFile(specText);
		Collection<SpecfileSource> patches = specfile.getPatches();
		for (SpecfileSource patch : patches) {
			if (patch.getNumber() == 2)
				assertEquals(1, patch.getLineNumber());
			else if (patch.getNumber() == 3)
				assertEquals(0, patch.getLineNumber());
			else
				fail();
		}
	}

	public void testLineNumber2() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3";

		newFile(specText);
		Collection<SpecfileSource> patches = specfile.getPatches();
		for (SpecfileSource patch : patches) {
			if (patch.getNumber() == 3) {
				assertEquals(0, patch.getLineNumber());
				assertEquals(1, patch.getLinesUsed().get(0).intValue());
			} else
				fail();
		}
	}

}
