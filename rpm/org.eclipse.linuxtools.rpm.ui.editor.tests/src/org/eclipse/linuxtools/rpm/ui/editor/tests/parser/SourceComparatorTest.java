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

package org.eclipse.linuxtools.rpm.ui.editor.tests.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.junit.Test;

public class SourceComparatorTest extends FileTestCase {

    @Test
    public void testPatchComparator() {
        String specText = "Patch3: somefilesomewhere.patch" + "\n"
                + "Patch2: someotherfile.patch";

        newFile(specText);
        Collection<SpecfileSource> patches = specfile.getPatches();
        int i = 1;
        for (SpecfileSource patch : patches) {
            i++;
            if (i == 2) {
                assertEquals(2, patch.getNumber());
            } else if (i == 3) {
                assertEquals(3, patch.getNumber());
            } else {
                fail();
            }
        }
    }

    @Test
    public void testPatchComparator2() {
        String specText = "Patch3: somefilesomewhere.patch" + "\n"
                + "Patch2: someotherfile.patch";

        newFile(specText);
        List<SpecfileSource> patches = specfile.getPatches();
        assertEquals(2, patches.get(0).getNumber());
        assertEquals(3, patches.get(1).getNumber());
    }
}
