/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindExportWizard;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindExportWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExportWizardTest extends AbstractMassifTest {

    private ValgrindExportWizard wizard;
    private WizardDialog dialog;
    private ValgrindExportWizardPage page;

    @BeforeEach
    public void prep() throws Exception {
        proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
    }

    @Override
    @AfterEach
    public void tearDown() throws CoreException {
        deleteProject(proj);
        // close dialog just in case
        if (dialog != null) {
            dialog.close();
        }
        super.tearDown();
    }

    @Test
    public void testExportBadPath() throws CoreException, URISyntaxException,
            IOException {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunch launch = doLaunch(config, "testExport"); //$NON-NLS-1$

        IProcess p = launch.getProcesses()[0];
        assertEquals(0, p.getExitValue());

        createWizard();

        // set the output path, should trigger modify listener
        page.getDestText().setText("DOESNOTEXIST"); //$NON-NLS-1$

        assertNotNull(page.getErrorMessage());
        assertFalse(page.isPageComplete());
        assertFalse(wizard.canFinish());
    }

    @Test
    public void testExportNotDir() throws CoreException, URISyntaxException,
            IOException {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunch launch = doLaunch(config, "testExport"); //$NON-NLS-1$

        IProcess p = launch.getProcesses()[0];
        assertEquals(0, p.getExitValue());

        createWizard();

        // set the output path, should trigger modify listener
        IPath pathToFiles = proj.getProject().getLocation();
        pathToFiles = pathToFiles.append("alloctest.c"); //$NON-NLS-1$
        assertTrue(pathToFiles.toFile().exists());
        page.getDestText().setText(pathToFiles.toOSString());

        assertNotNull(page.getErrorMessage());
        assertFalse(page.isPageComplete());
        assertFalse(wizard.canFinish());
    }

    @Test
    public void testExportBoth() throws CoreException, URISyntaxException,
            IOException {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunch launch = doLaunch(config, "testExport"); //$NON-NLS-1$

        IProcess p = launch.getProcesses()[0];
        assertEquals(0, p.getExitValue());

        createWizard();

        // set the output path, should trigger modify listener
        File[] selectedFiles = page.getSelectedFiles();
        IPath pathToFiles = proj.getProject().getLocation();
        page.getDestText().setText(pathToFiles.toOSString());

        assertTrue(page.isPageComplete());
        assertTrue(wizard.canFinish());

        assertTrue(wizard.performFinish());

        // check files were copied
        for (File log : selectedFiles) {
            File copy = new File(pathToFiles.toFile(), log.getName());
            assertTrue(copy.exists());
        }
    }

    @Test
    public void testExportOne() throws CoreException, URISyntaxException,
            IOException {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunch launch = doLaunch(config, "testExport"); //$NON-NLS-1$

        IProcess p = launch.getProcesses()[0];
        assertEquals(0, p.getExitValue());

        createWizard();

        // set the output path, should trigger modify listener
        File[] selectedFiles = page.getSelectedFiles();
        // uncheck first file
        File unselectedFile = selectedFiles[0];
        page.getViewer().setChecked(unselectedFile, false);
        selectedFiles = page.getSelectedFiles();

        IPath pathToFiles = proj.getProject().getLocation();
        page.getDestText().setText(pathToFiles.toOSString());

        assertTrue(page.isPageComplete());
        assertTrue(wizard.canFinish());

        assertTrue(wizard.performFinish());

        // check only selected file was copied
        for (File log : selectedFiles) {
            File copy = new File(pathToFiles.toFile(), log.getName());
            assertTrue(copy.exists());
        }
        File copy = new File(pathToFiles.toFile(), unselectedFile.getName());
        assertFalse(copy.exists());
    }

    @Test
    public void testExportNone() throws CoreException, URISyntaxException,
            IOException {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunch launch = doLaunch(config, "testExport"); //$NON-NLS-1$

        IProcess p = launch.getProcesses()[0];
        assertEquals(0, p.getExitValue());

        createWizard();

        // set the output path, should trigger modify listener
        File[] selectedFiles = page.getSelectedFiles();
        page.getViewer().setCheckedElements(new Object[0]);
        assertEquals(0, page.getSelectedFiles().length);

        IPath pathToFiles = proj.getProject().getLocation();
        page.getDestText().setText(pathToFiles.toOSString());

        assertTrue(page.isPageComplete());
        assertTrue(wizard.canFinish());

        assertTrue(wizard.performFinish());

        // check files were not copied
        for (File log : selectedFiles) {
            File copy = new File(pathToFiles.toFile(), log.getName());
            assertFalse(copy.exists());
        }
    }

    protected void createWizard() {
        Display.getDefault().syncExec(() -> {
		    wizard = new ValgrindExportWizard();
		    wizard.init(PlatformUI.getWorkbench(), null);

		    dialog = new WizardDialog(PlatformUI.getWorkbench()
		            .getActiveWorkbenchWindow().getShell(), wizard);
		    dialog.setBlockOnOpen(false);
		    dialog.open();

		    assertFalse(wizard.canFinish());

		    page = (ValgrindExportWizardPage) wizard.getPages()[0];
		    assertFalse(page.isPageComplete());

		});
    }
}
