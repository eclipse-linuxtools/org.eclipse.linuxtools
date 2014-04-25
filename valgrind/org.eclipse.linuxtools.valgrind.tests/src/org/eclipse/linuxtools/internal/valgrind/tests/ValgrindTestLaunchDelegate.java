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
package org.eclipse.linuxtools.internal.valgrind.tests;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchConfigurationDelegate;

public class ValgrindTestLaunchDelegate extends
        ValgrindLaunchConfigurationDelegate {

    @Override
    protected ValgrindCommand getValgrindCommand() {
        if (!ValgrindTestsPlugin.RUN_VALGRIND) {
            return new ValgrindStubCommand();
        } else {
            return super.getValgrindCommand();
        }
    }

    @Override
    protected void createDirectory(IPath path) throws IOException {
        if (ValgrindTestsPlugin.RUN_VALGRIND) {
            super.createDirectory(path);
        }
    }

    @Override
    protected IProcess createNewProcess(ILaunch launch, Process systemProcess,
            String programName) {
        IProcess process;
        if (ValgrindTestsPlugin.RUN_VALGRIND) {
            process = super
                    .createNewProcess(launch, systemProcess, programName);
        } else {
            process = new ValgrindStubProcess(launch, programName);
        }
        return process;
    }

    @Override
    protected void setOutputPath(ILaunchConfiguration config, IPath outputPath)
            throws CoreException {
        if (!ValgrindTestsPlugin.GENERATE_FILES
                && ValgrindTestsPlugin.RUN_VALGRIND) {
            super.setOutputPath(config, outputPath);
        }
    }

}
