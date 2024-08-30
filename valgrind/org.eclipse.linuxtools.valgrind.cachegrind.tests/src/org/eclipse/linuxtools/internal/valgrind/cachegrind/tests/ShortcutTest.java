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
 *    Red Hat Inc. - modified to use with Cachegrind testing
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.valgrind.launch.LaunchConfigurationConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShortcutTest extends AbstractCachegrindTest {

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
    public void testShortcutSelection() throws Exception {
        ValgrindTestCachegrindLaunchShortcut shortcut = new ValgrindTestCachegrindLaunchShortcut();

        shortcut.launch(new StructuredSelection(proj.getProject()),
                ILaunchManager.PROFILE_MODE);
        ILaunchConfiguration config = shortcut.getConfig();

        compareWithDefaults(config);
    }

    @Test
    public void testShortcutEditor() throws Exception {
        ValgrindTestCachegrindLaunchShortcut shortcut = new ValgrindTestCachegrindLaunchShortcut();

        IWorkbenchPage page = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage();
        IFile file = proj.getProject().getFile("test.c"); //$NON-NLS-1$
        IEditorPart editor = IDE.openEditor(page, file);

        assertNotNull(editor);

        shortcut.launch(editor, ILaunchManager.PROFILE_MODE);
        ILaunchConfiguration config = shortcut.getConfig();

        compareWithDefaults(config);
    }

    @Test
    public void testShortcutExistingConfig() throws Exception {
        ILaunchConfiguration prev = createConfiguration(proj.getProject());

        ValgrindTestCachegrindLaunchShortcut shortcut = new ValgrindTestCachegrindLaunchShortcut();
        shortcut.launch(new StructuredSelection(proj.getProject()),
                ILaunchManager.PROFILE_MODE);
        ILaunchConfiguration current = shortcut.getConfig();

        assertEquals(prev, current);
    }

    private void compareWithDefaults(ILaunchConfiguration config)
            throws CoreException {
        // tests launch in foreground, this is not typical
        ILaunchConfiguration defaults = createConfiguration(proj.getProject());
        ILaunchConfigurationWorkingCopy wc = defaults.getWorkingCopy();
        wc.removeAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND);
        wc.setAttribute(LaunchConfigurationConstants.ATTR_FULLPATH_AFTER, true);
        wc.doSave();

        // Compare launch config with defaults
        assertEquals(config.getAttributes(), defaults.getAttributes());
    }
}
