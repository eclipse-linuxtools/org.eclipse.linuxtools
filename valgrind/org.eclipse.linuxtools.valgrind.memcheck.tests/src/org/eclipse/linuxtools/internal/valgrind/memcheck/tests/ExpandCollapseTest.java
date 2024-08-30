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
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.valgrind.ui.CoreMessagesViewer;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExpandCollapseTest extends AbstractMemcheckTest {

    private CoreMessagesViewer viewer;
    private Menu contextMenu;

    @BeforeEach
    public void prep() throws Exception {
        proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
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

        ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
        viewer = view.getMessagesViewer();
        contextMenu = viewer.getTreeViewer().getTree().getMenu();

        // Select first error and expand it
        IValgrindMessage[] messages = (IValgrindMessage[]) viewer
                .getTreeViewer().getInput();
        IValgrindMessage element = messages[0];
        TreeSelection selection = new TreeSelection(new TreePath(
                new Object[] { element }));
        viewer.getTreeViewer().setSelection(selection);
        contextMenu.notifyListeners(SWT.Show, null);
        contextMenu.getItem(0).notifyListeners(SWT.Selection, null);

        checkExpanded(element, true);
    }

    @Test
    public void testCollapse() throws Exception {
        // Expand the element first
        testExpand();

        // Then collapse it
        IValgrindMessage[] messages = (IValgrindMessage[]) viewer
                .getTreeViewer().getInput();
        IValgrindMessage element = messages[0];
        TreeSelection selection = new TreeSelection(new TreePath(
                new Object[] { element }));
        viewer.getTreeViewer().setSelection(selection);
        contextMenu.notifyListeners(SWT.Show, null);
        contextMenu.getItem(1).notifyListeners(SWT.Selection, null);

        checkExpanded(element, false);
    }

    private void checkExpanded(IValgrindMessage element, boolean expanded) {
        if (element.getChildren().length > 0) {
            // only applicable to internal nodes
            if (expanded) {
                assertTrue(viewer.getTreeViewer().getExpandedState(element));
            } else {
                assertFalse(viewer.getTreeViewer().getExpandedState(element));
            }
        }
        for (IValgrindMessage child : element.getChildren()) {
            checkExpanded(child, expanded);
        }
    }
}
