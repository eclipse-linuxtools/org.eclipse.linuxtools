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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifLaunchConstants;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifTreeViewer;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DoubleClickTest extends AbstractMassifTest {
    private MassifHeapTreeNode node;

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

    private void doDoubleClick() {
        MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        MassifTreeViewer treeViewer = view.getTreeViewer();

        MassifSnapshot[] snapshots = view.getSnapshots();
        node = snapshots[1].getRoot(); // first detailed
        TreePath path = new TreePath(new Object[] { node });
        while (node.getChildren().length > 0 && !node.hasSourceFile()) {
            node = node.getChildren()[0];
            path = path.createChildPath(node);
        }
        assertTrue(node.hasSourceFile());
        treeViewer.getViewer().expandToLevel(node,
                AbstractTreeViewer.ALL_LEVELS);
        TreeSelection selection = new TreeSelection(path);

        // do double click
        IDoubleClickListener listener = treeViewer.getDoubleClickListener();
        listener.doubleClick(new DoubleClickEvent(treeViewer.getViewer(), selection));
    }
    @Test
    public void testDoubleClickFile() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
        wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, 2);
        wc.doSave();
        doLaunch(config, "testDoubleClickFile"); //$NON-NLS-1$

        doDoubleClick();

        checkFile(proj.getProject(), node);
    }
    @Test
    public void testDoubleClickLine() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
        wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, 2);
        wc.doSave();
        doLaunch(config, "testDoubleClickLine"); //$NON-NLS-1$

        doDoubleClick();

        checkLine(node);
    }
}
