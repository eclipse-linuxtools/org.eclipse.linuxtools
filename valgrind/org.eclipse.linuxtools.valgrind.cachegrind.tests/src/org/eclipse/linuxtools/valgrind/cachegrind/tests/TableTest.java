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
package org.eclipse.linuxtools.valgrind.cachegrind.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.valgrind.cachegrind.CachegrindLabelProvider;
import org.eclipse.linuxtools.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.widgets.TreeItem;

public class TableTest extends AbstractCachegrindTest {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProject("cpptest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}
	
	public void testFileLabelsCPP() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testFileLabelsCPP"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();		
		CachegrindOutput output = view.getOutputs()[0];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		
		assertTrue(file.getModel() instanceof ITranslationUnit);
		
		checkLabelProvider(file);
	}
	
	public void testFileLabelsH() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testFileLabelsH"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();		
		CachegrindOutput output = view.getOutputs()[0];
		CachegrindFile file = getFileByName(output, "cpptest.h"); //$NON-NLS-1$
		
		assertTrue(file.getModel() instanceof ITranslationUnit);
		assertTrue(((ITranslationUnit) file.getModel()).isHeaderUnit());
		
		checkLabelProvider(file);
	}

	private void checkLabelProvider(CachegrindFile file) {
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		TreeViewer viewer = view.getViewer();
		
		// collapse the tree, then expand only the interesting item
		viewer.expandToLevel(file, TreeViewer.ALL_LEVELS);
		TreePath path = new TreePath(new Object[] { view.getOutputs()[0], file });
		TreeSelection selection = new TreeSelection(path);
		viewer.setSelection(selection);		
		TreeItem item = viewer.getTree().getSelection()[0];
		
		// ensure the CElementLabelProvider is called correctly
		CElementLabelProvider provider = ((CachegrindLabelProvider) viewer.getLabelProvider(0)).getCLabelProvider();
		assertEquals(provider.getText(file.getModel()), item.getText());
		assertEquals(provider.getImage(file.getModel()), item.getImage());
	}

}
