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
package org.eclipse.linuxtools.internal.valgrind.launch;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider;

public class ValgrindOutputDirectoryProvider implements IValgrindOutputDirectoryProvider {
    private IPath outputPath;

    public ValgrindOutputDirectoryProvider() {
        outputPath = ValgrindLaunchPlugin.getDefault().getStateLocation();
    }

    @Override
    public IPath getOutputPath() {
        return outputPath;
    }

}
