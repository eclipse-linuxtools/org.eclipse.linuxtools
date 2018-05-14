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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.junit.Test;

public class LinePositionTest extends FileTestCase {
	@Test
	public void testLineNumber() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" + "Patch2: someotherfile.patch";

		newFile(specText);
		Collection<SpecfileSource> patches = specfile.getPatches();
		for (SpecfileSource patch : patches) {
			if (patch.getNumber() == 2)
				assertEquals(1, patch.getLineNumber());
			else if (patch.getNumber() == 3)
				assertEquals(0, patch.getLineNumber());
			else
				fail("Found patch with number different from the expected numbers (2 or 3).");
		}
	}

	@Test
	public void testLineNumber2() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3";

		newFile(specText);
		Collection<SpecfileSource> patches = specfile.getPatches();
		for (SpecfileSource patch : patches) {
			if (patch.getNumber() == 3) {
				assertEquals(0, patch.getLineNumber());
				assertEquals(1, patch.getLinesUsed().get(0).intValue());
			} else
				fail("Found patch with number different from the expected 3.");
		}
	}

}
