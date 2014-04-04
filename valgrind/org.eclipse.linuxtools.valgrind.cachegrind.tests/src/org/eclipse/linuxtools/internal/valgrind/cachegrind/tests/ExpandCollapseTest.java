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
package org.eclipse.linuxtools.internal.valgrind.cachegrind.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.ICachegrindElement;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExpandCollapseTest extends AbstractCachegrindTest {

	private TreeViewer viewer;
	private Menu contextMenu;

	@Before
	public void prep() throws Exception {
		proj = createProjectAndBuild("cpptest"); //$NON-NLS-1$
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

		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		viewer = view.getViewer();
		contextMenu = viewer.getTree().getMenu();

		// Select first snapshot and expand it
		CachegrindOutput[] outputs = (CachegrindOutput[]) viewer.getInput();
		CachegrindOutput output = outputs[0];
		TreeSelection selection = new TreeSelection(new TreePath(new Object[] { output }));
		viewer.setSelection(selection);
		contextMenu.notifyListeners(SWT.Show, null);
		contextMenu.getItem(0).notifyListeners(SWT.Selection, null);

		checkExpanded(output, true);
	}
	@Test
	public void testCollapse() throws Exception {
		// Expand the element first
		testExpand();

		// Then collapse it
		CachegrindOutput[] outputs = (CachegrindOutput[]) viewer.getInput();
		CachegrindOutput output = outputs[0];
		TreeSelection selection = new TreeSelection(new TreePath(new Object[] { output }));
		viewer.setSelection(selection);
		contextMenu.notifyListeners(SWT.Show, null);
		contextMenu.getItem(1).notifyListeners(SWT.Selection, null);

		checkExpanded(output, false);
	}

	private void checkExpanded(ICachegrindElement element, boolean expanded) {
		if (element.getChildren() != null && element.getChildren().length > 0) {
			// only applicable to internal nodes
			if (expanded) {
				assertTrue(viewer.getExpandedState(element));
			}
			else {
				assertFalse(viewer.getExpandedState(element));
			}
			for (ICachegrindElement child : element.getChildren()) {
				checkExpanded(child, expanded);
			}
		}
	}

}
