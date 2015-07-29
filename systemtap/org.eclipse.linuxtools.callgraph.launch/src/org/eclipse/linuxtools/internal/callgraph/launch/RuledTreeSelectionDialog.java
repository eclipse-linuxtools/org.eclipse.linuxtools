/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *         Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *             activated and used by other components.
 *      Lubomir Marinov <lubomir.marinov@gmail.com> - Fix for bug 182122 -[Dialogs]
 *          CheckedTreeSelectionDialog#createSelectionButtons(Composite) fails to
 *          align the selection buttons to the right
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.launch;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

/**
 * A class to select elements out of a tree structure.
 *
 * @since 2.0
 */
public class RuledTreeSelectionDialog extends CheckedTreeSelectionDialog {

    public RuledTreeSelectionDialog(Shell parent, ILabelProvider labelProvider,
            ITreeContentProvider contentProvider) {
        super(parent, labelProvider, contentProvider);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL
                | SWT.BOLD);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        line.setLayoutData(gridData);
        GridLayout lay = (GridLayout) composite.getLayout();
        lay.marginHeight=0;
        composite.setLayout(lay);

        return composite;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();

        layout.marginHeight = 0;
        layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setFont(font);

        /*
         * Create the rest of the button bar, but tell it not to
         * create a help button (we've already created it).
         */
        boolean helpAvailable = isHelpAvailable();
        setHelpAvailable(false);
        Composite c = (Composite) super.createButtonBar(composite);
        GridLayout lay = (GridLayout) c.getLayout();
        lay.marginHeight=0;
        c.setLayout(lay);
        composite.setLayout(layout);

        setHelpAvailable(helpAvailable);
        return composite;
    }

    @Override
    protected Label createMessageArea(Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        if (this.getMessage() != null) {
            label.setText(this.getMessage());
        }
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalIndent=10;
        label.setLayoutData(gd);

        label.setFont(composite.getFont());
        return label;
    }
}
