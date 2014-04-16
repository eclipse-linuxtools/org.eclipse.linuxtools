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

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.wizards.filter;

import org.eclipse.linuxtools.internal.systemtap.graphing.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphing.core.filters.MatchFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ColumnLayout;



public class MatchFilterWizardPage extends FilterWizardPage {
    public MatchFilterWizardPage() {
        super("selectFilterOptions"); //$NON-NLS-1$
        setTitle(Localization.getString("MatchFilterWizardPage.CreateMatchFilter")); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        Composite comp = new Composite(parent, SWT.NULL);
        comp.setLayout(new FormLayout());
        FormData data1 = new FormData();
        data1.left = new FormAttachment(0, 0);
        data1.top = new FormAttachment(0, 0);
        data1.right = new FormAttachment(40, 0);
        data1.bottom = new FormAttachment(100, 0);

        Composite cmpFilterOpts = new Composite(comp, SWT.NONE);
        cmpFilterOpts.setLayoutData(data1);
        ColumnLayout colLayout = new ColumnLayout();
        colLayout.maxNumColumns = 1;
        cmpFilterOpts.setLayout(colLayout);

        //Column
        Label lblColumn = new Label(cmpFilterOpts, SWT.NONE);
        lblColumn.setText(Localization.getString("MatchFilterWizardPage.Column")); //$NON-NLS-1$
        cboColumn = new Combo(cmpFilterOpts, SWT.DROP_DOWN | SWT.READ_ONLY);
        cboColumn.addSelectionListener(selectionListener);
        for(String serie : wizard.series) {
            cboColumn.add(serie);
        }

        new Label(cmpFilterOpts, SWT.NONE);	//Spacer

        //String to compare to
        Label lblCompare = new Label(cmpFilterOpts, SWT.NONE);
        lblCompare.setText(Localization.getString("MatchFilterWizardPage.CompareTo")); //$NON-NLS-1$
        txtCompare = new Text(cmpFilterOpts, SWT.BORDER);
        txtCompare.addModifyListener(modifyListener);

        new Label(cmpFilterOpts, SWT.NONE);	//Spacer

        //Style
        radKeepMatch = new Button(cmpFilterOpts, SWT.RADIO);
        radKeepMatch.setText(Localization.getString("MatchFilterWizardPage.KeepMatching")); //$NON-NLS-1$
        radKeepMatch.addSelectionListener(selectionListener);
        radKeepMatch.setSelection(true);
        radRemoveMatch = new Button(cmpFilterOpts, SWT.RADIO);
        radRemoveMatch.setText(Localization.getString("MatchFilterWizardPage.RemoveMatching")); //$NON-NLS-1$
        radRemoveMatch.addSelectionListener(selectionListener);

        cboColumn.select(0);
        createFilter();
        setControl(comp);
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    @Override
    protected void createFilter() {
        filter = null;
        setErrorMessage(null);
        String compare = txtCompare.getText().trim();
        if (compare.isEmpty()) {
            setErrorMessage(Localization.getString("MatchFilterWizardPage.EmptyError")); //$NON-NLS-1$
            return;
        }
        int selected = cboColumn.getSelectionIndex();
        int style = (radKeepMatch.getSelection() ? MatchFilter.KEEP_MATCHING : MatchFilter.REMOVE_MATCHING);
        filter = new MatchFilter(selected, compare, style);
    }

    @Override
    public void dispose() {
        if(null != txtCompare) {
            txtCompare.removeModifyListener(modifyListener);
            txtCompare.dispose();
            txtCompare = null;
        }

        if(null != cboColumn) {
            cboColumn.removeSelectionListener(selectionListener);
            cboColumn.dispose();
            cboColumn = null;
        }

        if(null != radKeepMatch) {
            radKeepMatch.removeSelectionListener(selectionListener);
            radKeepMatch.dispose();
            radKeepMatch = null;
        }

        if(null != radRemoveMatch) {
            radRemoveMatch.removeSelectionListener(selectionListener);
            radRemoveMatch.dispose();
            radRemoveMatch = null;
        }

        super.dispose();
    }

    private Combo cboColumn;
    private Text txtCompare;
    private Button radKeepMatch, radRemoveMatch;
}
