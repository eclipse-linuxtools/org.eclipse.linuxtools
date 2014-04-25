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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Class for handling launch of perf diff command and storing of
 * the resulting data.
 */
public class ReportComparisonData extends AbstractDataManipulator {

    private IPath oldFile;
    private IPath newFile;

    public ReportComparisonData(String title, IPath oldFile, IPath newFile, IProject project) {
        super(title, newFile.removeLastSegments(1), project);
        this.oldFile = oldFile;
        this.newFile = newFile;
    }

    @Override
    public void parse() {
        performCommand(getCommand(), 1);
    }

    /**
     * Get perf diff command to execute.
     *
     * @return String array representing command to execute.
     */
    protected String[] getCommand() {
        return new String[] { PerfPlugin.PERF_COMMAND,
                "diff", //$NON-NLS-1$
                oldFile.toOSString(),
                newFile.toOSString() };
    }

}
