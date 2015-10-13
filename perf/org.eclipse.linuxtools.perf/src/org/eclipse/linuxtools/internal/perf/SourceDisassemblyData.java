/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * This class handles the execution of the source disassembly command
 * and stores the resulting data.
 */
public class SourceDisassemblyData extends AbstractDataManipulator {

    public SourceDisassemblyData(String title, IPath workingDir, IProject project) {
        super(title, workingDir, project);
    }

    public SourceDisassemblyData(String title, IPath workingDir) {
        super(title, workingDir);
    }

    @Override
    public void parse() {
        URI workingDirURI = null;
        try {
            workingDirURI = new URI(getWorkDir().toOSString());
        } catch (URISyntaxException e) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
        }
        String [] cmd = getCommand(workingDirURI.getPath());
        // perf annotate prints the data to standard output
        performCommand(cmd, 1);
    }

    protected String [] getCommand(String workingDir) {
        /*
         * Some versions of perf annotate hangs waiting for some input that never comes.
         * Redirecting an empty file or /dev/null to its input will avoid this.
         */
        return new String[] { "sh", "-c", "perf annotate -i " + workingDir + "perf.data" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                       " < /dev/null" }; //$NON-NLS-1$
    }

}