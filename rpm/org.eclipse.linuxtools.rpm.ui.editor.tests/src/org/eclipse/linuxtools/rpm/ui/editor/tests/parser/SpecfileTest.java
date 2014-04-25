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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.junit.Test;

public class SpecfileTest extends FileTestCase {
    @Test
    public void testGetLine() throws BadLocationException {
        String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3";
        newFile(specText);
        assertEquals("%patch3", specfile.getLine(1));
    }
    @Test
    public void testChangeLine() throws BadLocationException {
        String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3";
        newFile(specText);
        assertEquals("%patch3", specfile.getLine(1));
        specfile.changeLine(1, "%patch4");
        assertEquals("%patch4", specfile.getLine(1));
    }
    @Test
    public void testChangeLine2() throws BadLocationException {
        String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3";
        newFile(specText);
        assertEquals("Patch3: somefilesomewhere.patch", specfile.getLine(0));
        specfile.changeLine(0, "Patch4: somefilesomewhere.patch");
        assertEquals("Patch4: somefilesomewhere.patch", specfile.getLine(0));
    }
    @Test
    public void testChangeLine3() throws BadLocationException {
        String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3";
        newFile(specText);
        assertEquals("Patch3: somefilesomewhere.patch", specfile.getLine(0));
        specfile.changeLine(0, "andrew");
        assertEquals("andrew", specfile.getLine(0));
    }
    @Test
    public void testOrganizePatches() throws BadLocationException {
        String specText = "Patch3: somefilesomewhere.patch" + "\n" + "%patch3";
        newFile(specText);
        assertEquals("Patch3: somefilesomewhere.patch", specfile.getLine(0));
        assertEquals("%patch3", specfile.getLine(1));
        assertEquals(0, specfile.getPatch(3).getLineNumber());
        SpecfileSource patch = specfile.getPatch(3);
        List<Integer> linesUsed = patch.getLinesUsed();
        assertEquals(1, linesUsed.size());
        Integer lineUsedNumber = linesUsed.get(0);
        assertEquals(1, lineUsedNumber.intValue());
        specfile.organizePatches();
        assertEquals("Patch0: somefilesomewhere.patch", specfile.getLine(0));
        assertEquals("%patch0", specfile.getLine(1));
        assertEquals(0, specfile.getPatch(0).getLineNumber());
        patch = specfile.getPatch(3);
        assertNull(patch);
        patch = specfile.getPatch(0);
        assertEquals(0, patch.getNumber());
        linesUsed = patch.getLinesUsed();
        assertEquals(1, linesUsed.size());
        lineUsedNumber = linesUsed.get(0);
        assertEquals(1, lineUsedNumber.intValue());
    }

}
