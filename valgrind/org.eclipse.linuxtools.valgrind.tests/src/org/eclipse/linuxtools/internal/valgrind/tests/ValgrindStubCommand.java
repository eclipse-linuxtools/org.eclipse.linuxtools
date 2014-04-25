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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;

public class ValgrindStubCommand extends ValgrindCommand {

    @Override
    public String whichVersion(IProject project) {
        return "valgrind-3.4.0"; //$NON-NLS-1$
    }

    @Override
    public void execute(String[] commandArray, Object env, File wd, boolean usePty, IProject project) {
        args = commandArray;
    }

    @Override
    public Process getProcess() {
        return null;
    }
}
