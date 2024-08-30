/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExpandCollapseTest extends AbstractMassifTest {

    protected TreeViewer viewer;
    protected Menu contextMenu;

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
    public void testExpand() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testDefaults"); //$NON-NLS-1$

        MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        viewer = view.getTreeViewer().getViewer();
        contextMenu = viewer.getTree().getMenu();

        // Select first snapshot and expand it
        MassifHeapTreeNode[] snapshots = (MassifHeapTreeNode[]) viewer.getInput();
        MassifHeapTreeNode snapshot = snapshots[0];
        TreeSelection selection = new TreeSelection(new TreePath(new Object[] { snapshot }));
        viewer.setSelection(selection);
        contextMenu.notifyListeners(SWT.Show, null);
        contextMenu.getItem(0).notifyListeners(SWT.Selection, null);

        checkExpanded(snapshot, true);
    }
    @Test
    public void testCollapse() throws Exception {
        // Expand the element first
        testExpand();

        // Then collapse it
        MassifHeapTreeNode[] snapshots = (MassifHeapTreeNode[]) viewer.getInput();
        MassifHeapTreeNode snapshot = snapshots[0];
        TreeSelection selection = new TreeSelection(new TreePath(new Object[] { snapshot }));
        viewer.setSelection(selection);
        contextMenu.notifyListeners(SWT.Show, null);
        contextMenu.getItem(1).notifyListeners(SWT.Selection, null);

        checkExpanded(snapshot, false);
    }

    private void checkExpanded(MassifHeapTreeNode element, boolean expanded) {
        if (element.getChildren().length > 0) {
            // only applicable to internal nodes
            if (expanded) {
                assertTrue(viewer.getExpandedState(element));
            }
            else {
                assertFalse(viewer.getExpandedState(element));
            }
        }
        for (MassifHeapTreeNode child : element.getChildren()) {
            checkExpanded(child, expanded);
        }
    }
}
