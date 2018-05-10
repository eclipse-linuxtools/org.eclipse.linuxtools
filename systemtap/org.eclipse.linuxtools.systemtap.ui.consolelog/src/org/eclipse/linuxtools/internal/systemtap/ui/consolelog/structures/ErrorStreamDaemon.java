/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *     Red Hat Inc. - ongoing maintenance
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.structures;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ConsoleStreamDaemon;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;

/**
 * A class push data to both the <code>ScriptConsole</code> and the ErrorView
 * @author Ryan Morse
 */
public class ErrorStreamDaemon extends ConsoleStreamDaemon {
    public ErrorStreamDaemon(ScriptConsole console) {
        super(console);

        outputData = new StringBuilder();
    }

    /**
     * Prints out the new output data to the console and parses it and sends it to the
     * ErrorView.
     */
    @Override
    protected void pushData() {
        if(output.startsWith(Localization.getString("ErrorStreamDaemon.Password"))) { //$NON-NLS-1$
            output = output.substring(Localization.getString("ErrorStreamDaemon.Password").length()); //$NON-NLS-1$
        }

        super.pushData();

        outputData.append(output);

    }

    /**
     * Disposes of all internal references in the class. No method should be called after this.
     */
    @Override
    public void dispose() {
        if(!isDisposed()) {
            super.dispose();
            outputData.delete(0, outputData.length());
            outputData = null;
        }
    }

    private StringBuilder outputData;
}
