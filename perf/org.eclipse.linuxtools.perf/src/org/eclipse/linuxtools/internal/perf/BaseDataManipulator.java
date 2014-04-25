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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

/**
 * Base class providing basic functionality for stream processing.
 */
public class BaseDataManipulator {

    /**
     * Get contents of specified BufferedReader as a String
     *
     * @param buffer BufferedReader to read from.
     * @return String contents of BufferedReader.
     */
    public String getBufferContents(BufferedReader buffer) {
        if (buffer == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuilder fileStr = new StringBuilder();
        try {
            String line;

            while ((line = buffer.readLine()) != null) {
                fileStr.append(line);
                fileStr.append("\n"); //$NON-NLS-1$
            }

        } catch (IOException e) {
            PerfPlugin.getDefault().openError(e, ""); //$NON-NLS-1$
        }

        return fileStr.toString();
    }

    /**
     * Close specified resource
     *
     * @param resrc resource to close
     */
    public void closeResource(Closeable resrc) {
        if (resrc != null) {
            try {
                resrc.close();
            } catch (IOException e) {
                PerfPlugin.getDefault().openError(e, ""); //$NON-NLS-1$
            }
        }
    }
}
