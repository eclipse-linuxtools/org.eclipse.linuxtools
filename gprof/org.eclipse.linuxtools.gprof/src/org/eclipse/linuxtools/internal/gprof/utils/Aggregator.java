/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Gives support to aggregate gmon files
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class Aggregator {

    /**
     * Aggregates the given gmon files in the given directory
     *
     * @param gprof2use
     *            typically gprof, but can be something else (cross-development tools like sh4gprof, st200gprof, ...)
     * @param binaryFile
     * @param gmons
     * @param directory
     * @return the location of generated gmon.sum
     */
    public static File aggregate(String gprof2use, String binaryFile, java.util.List<String> gmons, File directory) {
        String[] cmd = new String[gmons.size() + 3];
        cmd[0] = gprof2use;
        cmd[1] = "-s"; //$NON-NLS-1$
        cmd[2] = binaryFile;
        int i = 3;
        for (String string : gmons) {
            cmd[i++] = string;
        }
        String errorMessage = null;
        try {
            final Process p = ProcessFactory.getFactory().exec(cmd, null, directory);
            ProcessReader pr = new ProcessReader(p);
            pr.start();
            int ret = p.waitFor();
            if (ret != 0) {
                errorMessage = Messages.Aggregator_ERROR_COMMON_PREFIX;
                errorMessage += pr.errorMessage;

            }
        } catch (Exception _) {
            errorMessage = Messages.Aggregator_ERROR_COMMON_PREFIX;
            errorMessage += _.getMessage();
        }
        File ret = new File(directory, "gmon.sum"); //$NON-NLS-1$
        if (!ret.isFile() && errorMessage == null) {
            errorMessage = Messages.Aggregator_ERROR_COMMON_PREFIX;
            errorMessage += ret.getName();
            errorMessage += Messages.Aggregator_NOT_FOUND;
        }

        if (errorMessage != null) {
            final String finalErrorMessage = errorMessage;
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.Aggregator_GPROF_ERROR, finalErrorMessage);
                }
            });
            return null;
        }
        return ret;
    }

    final static class ProcessReader extends Thread {

        private final Process p;
        private String errorMessage = ""; //$NON-NLS-1$

        ProcessReader(Process p) {
            this.p = p;
        }

        @Override
        public void run() {
            try {
                LineNumberReader lnr = new LineNumberReader(new InputStreamReader(p.getErrorStream()));
                do {
                    String s = lnr.readLine();
                    if (s == null)
                        break;
                    errorMessage += s + "\n"; //$NON-NLS-1$
                } while (true);
            } catch (IOException e) {
                // do nothing
            }
        }
    }

}
