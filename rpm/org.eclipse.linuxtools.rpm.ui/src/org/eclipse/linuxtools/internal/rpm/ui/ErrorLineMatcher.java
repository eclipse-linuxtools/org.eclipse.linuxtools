/*******************************************************************************
 * Copyright (c) 2010, 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Matcher adding hyperlink to navigate to line in the spec file whenever
 * rpmbuild reports the line in error.
 *
 */
public class ErrorLineMatcher implements IPatternMatchListenerDelegate {

    private static final String LINE = "line"; //$NON-NLS-1$
    private RpmConsole console;

    @Override
    public void connect(TextConsole console) {
        this.console = (RpmConsole) console;
    }

    @Override
    public void disconnect() {
        this.console = null;
    }

    @Override
    public void matchFound(PatternMatchEvent event) {
        String line = null;
        try {
            line = console.getDocument().get(event.getOffset(),
                    event.getLength());
            int lineNumber = Integer.parseInt(line.substring(12,
                    line.indexOf(':', line.indexOf(LINE))).trim());
            FileLink fileLink = new FileLink(
                    console.getSpecfile().getAdapter(IFile.class),
                    "org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor", -1, -1, lineNumber); //$NON-NLS-1$
            console.addHyperlink(fileLink, 7,
                    line.indexOf(':', line.indexOf(LINE)) - 7);
        } catch (BadLocationException e1) {
            return;
        }

    }

}
