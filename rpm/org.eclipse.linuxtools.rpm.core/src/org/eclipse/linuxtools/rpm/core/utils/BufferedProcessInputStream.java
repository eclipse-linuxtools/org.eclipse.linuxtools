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
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.utils;

import java.io.BufferedInputStream;

/**
 * A BufferedInputStream implementation that produces output from a process. It
 * provides methods for destroying an getting the return status from the
 * process.
 */
public class BufferedProcessInputStream extends BufferedInputStream {

    private Process process;
    /**
     * Create the input stream from a process.
     * @param process The process that outputs to this input stream.
     */
    public BufferedProcessInputStream(Process process) {
        super(process.getInputStream());
        this.process = process;
    }
    /**
     * Destroys the process associated with this BufferedProcessInputStream.
     */
    public void destroyProcess(){
        process.destroy();
    }
    /**
     * Getter method for the exit value of the process associated with this
     * BufferedProcessInputStream.
     * @throws InterruptedException If the process got interrupted.
     * @return The exit value of the process.
     */
    public int getExitValue() throws InterruptedException {
        process.waitFor();
        return process.exitValue();
    }
}
