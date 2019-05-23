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
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SystemTapTextParser extends SystemTapParser{

    protected String contents;

    @Override
    public IStatus nonRealTimeParsing() {
        contents = Helper.readFile(sourcePath);
        return Status.OK_STATUS;
    }

    @Override
    protected void initialize() {
        // Empty
    }

    @Override
    public IStatus realTimeParsing() {
        BufferedReader buff = internalData;
        StringBuilder text = new StringBuilder();

        String line;
        try {
            while ((line = buff.readLine()) != null) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                text.append(line + "\n"); //$NON-NLS-1$
            }
            setData(text.toString());
            view.update();
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }

        return Status.OK_STATUS;
    }

}
