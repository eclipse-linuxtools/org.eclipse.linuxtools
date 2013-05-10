/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;


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

public class DoubleClickTest extends AbstractMassifTest {
	private MassifHeapTreeNode node;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
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

	public void testDoubleClickFile() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, 2);
		wc.doSave();
		doLaunch(config, "testDoubleClickFile"); //$NON-NLS-1$

		doDoubleClick();

		checkFile(proj.getProject(), node);
	}

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
