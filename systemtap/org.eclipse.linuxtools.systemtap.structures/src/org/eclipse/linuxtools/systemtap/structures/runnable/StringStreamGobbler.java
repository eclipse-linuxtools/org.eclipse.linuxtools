/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.runnable;

import java.io.InputStream;

/**
 * A {@link StreamGobbler} that reads the stream into a {@link StringBuilder}
 * instead of notifying observers.
 *
 */
public class StringStreamGobbler extends StreamGobbler {
    StringBuilder output;

    public StringStreamGobbler(InputStream is) {
        super(is);
        this.output = new StringBuilder();
    }

    @Override
    public void fireNewDataEvent(String line) {
        output.append(line);
    }

    public StringBuilder getOutput() {
        return output;
    }
}
