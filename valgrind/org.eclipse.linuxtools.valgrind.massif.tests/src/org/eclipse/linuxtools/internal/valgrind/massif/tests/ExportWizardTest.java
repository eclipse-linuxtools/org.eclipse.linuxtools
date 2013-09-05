/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
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

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindExportWizard;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindExportWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExportWizardTest extends AbstractMassifTest {

	protected ValgrindExportWizard wizard;
	protected WizardDialog dialog;
	protected ValgrindExportWizardPage page;

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

		// close dialog just in case
		if (dialog != null) {
			dialog.close();
		}
		super.tearDown();
	}
	@Test
	public void testExportBadPath() throws Exception {
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
	public void testExportNotDir() throws Exception {
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
	public void testExportBoth() throws Exception {
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
	public void testExportOne() throws Exception {
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
	public void testExportNone() throws Exception {
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
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				wizard = new ValgrindExportWizard();
				wizard.init(PlatformUI.getWorkbench(), null);

				dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
				dialog.setBlockOnOpen(false);
				dialog.open();

				assertFalse(wizard.canFinish());

				page = (ValgrindExportWizardPage) wizard.getPages()[0];
				assertFalse(page.isPageComplete());

			}

		});
	}
}
