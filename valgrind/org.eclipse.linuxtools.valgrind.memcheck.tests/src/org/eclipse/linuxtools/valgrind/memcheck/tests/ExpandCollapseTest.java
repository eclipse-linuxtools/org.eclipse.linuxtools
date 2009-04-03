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
package org.eclipse.linuxtools.valgrind.memcheck.tests;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckViewPart;
import org.eclipse.linuxtools.valgrind.memcheck.model.ValgrindTreeElement;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

public class ExpandCollapseTest extends AbstractMemcheckTest {
	
	protected TreeViewer viewer;
	protected Menu contextMenu;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}
	
	public void testExpand() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testDefaults"); //$NON-NLS-1$
		
		MemcheckViewPart view = (MemcheckViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		viewer = view.getViewer();
		contextMenu = viewer.getTree().getMenu();
		
		// Select first error and expand it
		ValgrindTreeElement root = (ValgrindTreeElement) viewer.getInput();
		ValgrindTreeElement element = root.getChildren()[0];
		TreeSelection selection = new TreeSelection(new TreePath(new Object[] { root, element }));
		viewer.setSelection(selection);
		contextMenu.notifyListeners(SWT.Show, null);
		contextMenu.getItem(0).notifyListeners(SWT.Selection, null);
		
		checkExpanded(element, true);
	}
	
	public void testCollapse() throws Exception {
		// Expand the element first
		testExpand();
		
		// Then collapse it
		ValgrindTreeElement root = (ValgrindTreeElement) viewer.getInput();
		ValgrindTreeElement element = root.getChildren()[0];
		TreeSelection selection = new TreeSelection(new TreePath(new Object[] { root, element }));
		viewer.setSelection(selection);
		contextMenu.notifyListeners(SWT.Show, null);
		contextMenu.getItem(1).notifyListeners(SWT.Selection, null);
		
		checkExpanded(element, false);
	}

	private void checkExpanded(ValgrindTreeElement element, boolean expanded) {
		if (element.getChildren().length > 0) {
			// only applicable to internal nodes
			if (expanded) {
				assertTrue(viewer.getExpandedState(element));
			}
			else {
				assertFalse(viewer.getExpandedState(element));
			}
		}
		for (ValgrindTreeElement child : element.getChildren()) {
			checkExpanded(child, expanded);
		}
	}
}
