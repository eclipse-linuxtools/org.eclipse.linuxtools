/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.views.ErrorView;

/**
 * A class push data to both the </code>ScriptConsole</code> and the ErrorView
 * @author Ryan Morse
 */
public class ErrorStreamDaemon extends ConsoleStreamDaemon {
    public ErrorStreamDaemon(ScriptConsole console, ErrorView errorWindow, IErrorParser parser) {
        super(console);

        outputData = new StringBuilder();
        this.parser = parser;
        if (null != errorWindow) {
            errorView = errorWindow;
            errorView.clear();
        }
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

        /* Since we never know when the last set of data is comming we don't clear the
         * errorStream in the hope of getting a more complete error message. As a result
         * the parser will always return what we already had.  Clear removes anything
         * that was added before.
         */
        if(null != errorView) {
            String[][] errors = parser.parseOutput(outputData.toString());

            if(null != errors) {
                errorView.clear();
                for(String[] error :errors) {
                    errorView.add(error);
                }
            }
        }
    }

    /**
     * Disposes of all internal references in the class. No method should be called after this.
     */
    @Override
    public void dispose() {
        if(!isDisposed()) {
            super.dispose();
            errorView = null;
            outputData.delete(0, outputData.length());
            outputData = null;
            parser = null;
        }
    }

    private ErrorView errorView;
    private StringBuilder outputData;
    private IErrorParser parser;
}
