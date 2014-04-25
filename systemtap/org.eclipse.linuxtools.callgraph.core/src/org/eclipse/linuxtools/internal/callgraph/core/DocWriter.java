/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.progress.UIJob;

public class DocWriter extends UIJob {
    private TextConsole console;
    private String message;
    private int length;
    private int start;

    /**
     * Initiate DocWriter class. DocWriter will append the given message
     * to the given console in a separate UI job. By default, DocWriter will
     * append to the end of the console and replace 0 characters. To change this,
     * see DocWriter's set methods.
     *
     *
     * @param name
     * @param console
     * @param message
     */
    public DocWriter(String name, TextConsole console, String message) {

        super(name);
        this.console = console;
        this.message = message;
        this.start=-1;
        this.length=-1;
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        if (console == null) {
            return Status.CANCEL_STATUS;
        }
        if (message == null) {
            return Status.OK_STATUS;
        }

        IDocument doc = console.getDocument();

        if (length < 0) {
            length = 0;
        }
        if (start < 0) {
            start = doc.getLength();
        }
        try {
            doc.replace(start, length, message);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return Status.OK_STATUS;
    }

}
