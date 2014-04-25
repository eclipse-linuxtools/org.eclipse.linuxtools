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

package org.eclipse.linuxtools.systemtap.ui.consolelog.views;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ErrorTableDisplay;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;



/**
 * A view to list error messages generated from trying to run a script in the editor.
 * @author Ryan Morse
 */
public class ErrorView extends ViewPart {
    public ErrorView() {
        super();
    }

    /**
     * Creates a new table to contain all of the error messages.
     * @param parent The composite to draw all content to.
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite c = new Composite(parent, SWT.NONE);

        GridLayout grid = new GridLayout();
        c.setLayout(grid);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table = new ErrorTableDisplay(c, new String[] {"", Localization.getString("ErrorView.Type"), Localization.getString("ErrorView.Description"), Localization.getString("ErrorView.Saw"), Localization.getString("ErrorView.Line")}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        table.getControl().setLayoutData(gd);
    }

    /**
     * Adds the log details to the table of errors.
     * @param log The details for an error message to display in the table.
     */
    public void add(final String[] log) {
        table.getControl().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                table.addRow(log);

                try {
                    PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage().showView(ID);
                } catch(PartInitException pie) {
                }
            }
        });
    }

    /**
     * Clears the entire table of error messages.
     */
    public void clear() {
        if(null != table)
            table.clear();
    }

    @Override
    public void setFocus() {}

    /**
     * Disposes of everything in this class.
     */
    @Override
    public void dispose() {
        if(null != table)
            table.dispose();
        table = null;
        super.dispose();
    }

    private volatile ErrorTableDisplay table;
    public static final String ID = "org.eclipse.linuxtools.systemtap.ui.consolelog.views.ErrorView"; //$NON-NLS-1$
}
