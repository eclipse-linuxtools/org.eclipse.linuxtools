/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.linuxtools.systemtap.structures.KernelSourceTree;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.junit.Before;
import org.junit.Test;

public class KernelSourceTreeTest {

    @Before
    public void setUp() {
        kst = new KernelSourceTree();
    }

    @Test
    public void testGetTree() {
        assertNull("Inital tree is null", kst.getTree());
    }

    @Test
    public void testBuildKernelTree() {
        TreeNode t;

        String direct = null;    //Null
        String[] excluded = null;
        kst.buildKernelTree(direct, excluded);
        assertNull("Null directory", kst.getTree());

        direct = "";    //Empty string for directory
        kst.buildKernelTree(direct, excluded);
        assertNull("Empty string directory", kst.getTree());

        direct = "/noSuchDirectory/";    //Missing folder
        kst.buildKernelTree(direct, excluded);
        assertEquals("Missing directory", 0, kst.getTree().getChildCount());

        direct = "/root/";    //Inaccessible
        kst.buildKernelTree(direct, excluded);
        assertEquals("Inaccessable directory", 0, kst.getTree().getChildCount());

        direct = "/bin/";    //No .c or .h files
        kst.buildKernelTree(direct, excluded);
        t = kst.getTree();
        assertEquals("Bin folder item count", 0, t.getChildCount());
        assertTrue("Bin folder name", "bin".equals(t.toString()));
        assertTrue("Bin has file", t.getData() instanceof IFileStore);

        excluded = new String[] {".git"};
        direct = "/tmp/";    //No .c or .h files
        kst.buildKernelTree(direct, excluded);
    }

    public void testDispose() {
        kst.dispose();
    }

    private KernelSourceTree kst;
}
