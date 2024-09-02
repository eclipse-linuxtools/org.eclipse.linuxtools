/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.linuxtools.systemtap.structures.KernelSourceTree;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KernelSourceTreeTest {

    @BeforeEach
    public void setUp() {
        kst = new KernelSourceTree();
    }

    @Test
    public void testGetTree() {
        assertNull(kst.getTree(), "Inital tree is null");
    }

    @Test
    public void testBuildKernelTree() {
        TreeNode t;

        String direct = null;    //Null
        String[] excluded = null;
        kst.buildKernelTree(direct, excluded);
        assertNull(kst.getTree(), "Null directory");

        direct = "";    //Empty string for directory
        kst.buildKernelTree(direct, excluded);
        assertNull(kst.getTree(), "Empty string directory");

        direct = "/noSuchDirectory/";    //Missing folder
        kst.buildKernelTree(direct, excluded);
        assertEquals(0, kst.getTree().getChildCount(), "Missing directory");

        direct = "/root/";    //Inaccessible
        kst.buildKernelTree(direct, excluded);
        assertEquals(0, kst.getTree().getChildCount(), "Inaccessable directory");

        direct = "/bin/";    //No .c or .h files
        kst.buildKernelTree(direct, excluded);
        t = kst.getTree();
        assertEquals(0, t.getChildCount(), "Bin folder item count");
        assertEquals("bin", t.toString(), "Bin folder name");
        assertInstanceOf(IFileStore.class, t.getData(), "Bin has file");

        excluded = new String[] {".git"};
        direct = "/tmp/";    //No .c or .h files
        kst.buildKernelTree(direct, excluded);
    }

    public void testDispose() {
        kst.dispose();
    }

    private KernelSourceTree kst;
}
