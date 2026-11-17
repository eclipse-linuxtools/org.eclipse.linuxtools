/*******************************************************************************
 * Copyright (c) 2009, 2026 Red Hat, Inc.
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
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.linuxtools.internal.valgrind.tests.ValgrindStubCommand;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class MinVersionTest extends AbstractMemcheckTest {

    static class ValgrindIncorrectVersion extends ValgrindStubCommand {
        @Override
        public String whichVersion(IProject project) {
            return "valgrind-3.2.1"; //$NON-NLS-1$
        }
    }

    @BeforeAll
    public void createProjects() throws Exception {
        proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
    }

    @AfterAll
    public void deleteProjects() throws CoreException {
        deleteProject(proj);
    }

    @BeforeEach
    public void prep() {
        saveVersion();
    }

    private void saveVersion() {
        ValgrindLaunchPlugin.getDefault().setValgrindCommand(
                new ValgrindIncorrectVersion());
    }

    @Override
    @AfterEach
    public void tearDown() throws CoreException {
        restoreVersion();
        super.tearDown();
    }

    private void restoreVersion() {
        ValgrindLaunchPlugin.getDefault().setValgrindCommand(
                new ValgrindCommand());
    }

    @Test
    public void testLaunchBadVersion() throws Exception {
        // Put this back so we can make a valid config
        restoreVersion();
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        // For some reason we downgraded
        saveVersion();

        doLaunch(config, "testDefaults"); //$NON-NLS-1$

        // The launch reports the bad version in an asynchronously opened error
        // dialog. Queue a runnable after it that closes the dialog from within
        // its event loop, so the dialog does not block a later test.
        Display display = Display.getCurrent();
        boolean[] dialogShown = { false };
        display.asyncExec(() -> {
            for (Shell shell : display.getShells()) {
                if (shell.getData() instanceof MessageDialog dialog) {
                    dialogShown[0] = true;
                    dialog.close();
                }
            }
        });
        while (display.readAndDispatch()) {
            // process the queued runnables
        }
        assertTrue(dialogShown[0]);
    }

    @Test
    public void testTabsBadVersion() throws Exception {
        Shell testShell = new Shell(Display.getDefault());
        testShell.setLayout(new GridLayout());
        ValgrindOptionsTab tab = new ValgrindOptionsTab();

        ILaunchConfiguration config = getLaunchConfigType().newInstance(
                null,
                getLaunchManager().generateLaunchConfigurationName(
                        proj.getProject().getName()));
        ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
        tab.setDefaults(wc);
        tab.createControl(testShell);
        tab.initializeFrom(config);
        tab.performApply(wc);

        assertFalse(tab.isValid(config));
        assertNotNull(tab.getErrorMessage());

        testShell.dispose();
    }

}
