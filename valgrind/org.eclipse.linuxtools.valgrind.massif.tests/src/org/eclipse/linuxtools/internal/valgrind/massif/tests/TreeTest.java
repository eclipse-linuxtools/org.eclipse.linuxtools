/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifLaunchConstants;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TreeTest extends AbstractMassifTest {
    @BeforeEach
    public void prep() throws Exception {
        proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
    }

    @Override
    @AfterEach
    public void tearDown() throws CoreException {
        deleteProject(proj);
        super.tearDown();
    }

    @Test
    public void testTreeNodes() throws CoreException, URISyntaxException,
            IOException {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
        wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, 2);
        wc.doSave();
        doLaunch(config, "testTreeNodes"); //$NON-NLS-1$

        MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault()
                .getView().getDynamicView();
        TreeViewer treeViewer = view.getTreeViewer().getViewer();

        MassifSnapshot[] snapshots = view.getSnapshots();
        MassifHeapTreeNode[] nodes = (MassifHeapTreeNode[]) treeViewer
                .getInput();
        for (int i = 0; i < nodes.length; i++) {
            // every odd snapshot should be detailed with --detailed-freq=2
            // and thus in the tree
            assertEquals(snapshots[2 * i + 1].getRoot(), nodes[i]);
        }
    }

    @Test
    public void testNoDetailed() throws CoreException, URISyntaxException,
            IOException {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
        wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, 12); // >
                                                                                // #snapshots
        wc.doSave();
        doLaunch(config, "testNoDetailed"); //$NON-NLS-1$

        MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault()
                .getView().getDynamicView();
        TreeViewer treeViewer = view.getTreeViewer().getViewer();

        MassifHeapTreeNode[] nodes = (MassifHeapTreeNode[]) treeViewer
                .getInput();

        assertNotNull(nodes);
        assertEquals(1, nodes.length); // should always contain peak
    }

}
