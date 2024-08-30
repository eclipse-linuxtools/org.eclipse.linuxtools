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
package org.eclipse.linuxtools.internal.valgrind.cachegrind.tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindLabelProvider;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.ICachegrindElement;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CModelLabelsTest extends AbstractCachegrindTest {

    @BeforeEach
    public void prep() throws Exception {
        proj = createProjectAndBuild("cpptest"); //$NON-NLS-1$
    }

    @Override
    @AfterEach
    public void tearDown() throws CoreException {
        deleteProject(proj);
        super.tearDown();
    }
    @Test
    public void testFileLabelsCPP() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testFileLabelsCPP"); //$NON-NLS-1$

        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        CachegrindOutput output = view.getOutputs()[0];
        CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$

        assertTrue(file.getModel() instanceof ITranslationUnit);

        checkLabelProvider(file);
    }
    @Test
    public void testFileLabelsH() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testFileLabelsH"); //$NON-NLS-1$

        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        CachegrindOutput output = view.getOutputs()[0];
        CachegrindFile file = getFileByName(output, "cpptest.h"); //$NON-NLS-1$

        assertTrue(file.getModel() instanceof ITranslationUnit);
        assertTrue(((ITranslationUnit) file.getModel()).isHeaderUnit());

        checkLabelProvider(file);
    }
    @Test
    public void testFunctionLabel() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testFunctionLabel"); //$NON-NLS-1$

        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        CachegrindOutput output = view.getOutputs()[0];
        CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
        CachegrindFunction func = getFunctionByName(file, "main"); //$NON-NLS-1$

        assertTrue(func.getModel() instanceof IFunction);

        checkLabelProvider(func, file);
    }
    @Test
    public void testMethodLabel() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testMethodLabel"); //$NON-NLS-1$

        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        CachegrindOutput output = view.getOutputs()[0];
        CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
        CachegrindFunction func = getFunctionByName(file, "A::A()"); //$NON-NLS-1$

        assertTrue(func.getModel() instanceof IMethod);

        checkLabelProvider(func, file);
    }
    @Test
    public void testNestedMethodLabel() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testNestedMethodLabel"); //$NON-NLS-1$

        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        CachegrindOutput output = view.getOutputs()[0];
        CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
        CachegrindFunction func = getFunctionByName(file, "A::B::e()"); //$NON-NLS-1$

        assertTrue(func.getModel() instanceof IMethod);

        checkLabelProvider(func, file);
    }

    private static void checkLabelProvider(CachegrindFile file) {
        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        TreeViewer viewer = view.getViewer();

        TreePath path = new TreePath(new Object[] { view.getOutputs()[0], file });
        checkLabelProvider(viewer, path, file);
    }

    private static void checkLabelProvider(CachegrindFunction func, CachegrindFile file) {
        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
        TreeViewer viewer = view.getViewer();

        TreePath path = new TreePath(new Object[] { view.getOutputs()[0], file, func });
        checkLabelProvider(viewer, path, func);
    }

    private static void checkLabelProvider(TreeViewer viewer, TreePath path, ICachegrindElement element) {
        // expand only the interesting item
        viewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
        TreeSelection selection = new TreeSelection(path);
        viewer.setSelection(selection);
        TreeItem item = viewer.getTree().getSelection()[0];

        // ensure the CElementLabelProvider is called correctly
        CElementLabelProvider provider = ((CachegrindLabelProvider) viewer.getLabelProvider(0)).getCLabelProvider();
        assertEquals(provider.getText(element.getModel()), item.getText());
        assertEquals(provider.getImage(element.getModel()), item.getImage());
    }

}
