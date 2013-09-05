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

import static org.junit.Assert.*;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifLaunchConstants;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TreeTest extends AbstractMassifTest {
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
	}

	@Override
	@After
	public void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}
	@Test
	public void testTreeNodes() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, 2);
		wc.doSave();
		doLaunch(config, "testTreeNodes"); //$NON-NLS-1$

		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		TreeViewer treeViewer = view.getTreeViewer().getViewer();

		MassifSnapshot[] snapshots = view.getSnapshots();
		MassifHeapTreeNode[] nodes = (MassifHeapTreeNode[]) treeViewer.getInput();
		for (int i = 0; i < nodes.length; i++) {
			// every odd snapshot should be detailed with --detailed-freq=2
			// and thus in the tree
			assertEquals(snapshots[2 * i + 1].getRoot(), nodes[i]);
		}
	}
	@Test
	public void testNoDetailed() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, 12); // > #snapshots
		wc.doSave();
		doLaunch(config, "testNoDetailed"); //$NON-NLS-1$

		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		TreeViewer treeViewer = view.getTreeViewer().getViewer();

		MassifHeapTreeNode[] nodes = (MassifHeapTreeNode[]) treeViewer.getInput();

		assertNotNull(nodes);
		assertEquals(1, nodes.length); // should always contain peak
	}

}
