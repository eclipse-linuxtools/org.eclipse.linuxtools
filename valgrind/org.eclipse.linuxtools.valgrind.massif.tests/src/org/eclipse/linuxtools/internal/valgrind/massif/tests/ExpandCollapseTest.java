/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExpandCollapseTest extends AbstractMassifTest {

	protected TreeViewer viewer;
	protected Menu contextMenu;

	@Before
	public void prep() throws Exception {
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
	}

	@Override
	@After
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
