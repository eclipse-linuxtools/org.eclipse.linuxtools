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
import org.eclipse.linuxtools.systemtap.graphing.core.filters.RangeFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ColumnLayout;



public class RangeFilterWizardPage extends FilterWizardPage {
    public RangeFilterWizardPage() {
        super("selectFilterOptions"); //$NON-NLS-1$
        setTitle(Localization.getString("RangeFilterWizardPage.CreateRangeFilter")); //$NON-NLS-1$
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
        lblColumn.setText(Localization.getString("RangeFilterWizardPage.Column")); //$NON-NLS-1$
        cboColumn = new Combo(cmpFilterOpts, SWT.DROP_DOWN | SWT.READ_ONLY);
        cboColumn.addSelectionListener(selectionListener);
        for(int i=0; i<wizard.series.length; i++) {
            cboColumn.add(wizard.series[i]);
        }

        new Label(cmpFilterOpts, SWT.NONE);    //Spacer

        //Low
        Label lblLow = new Label(cmpFilterOpts, SWT.NONE);
        lblLow.setText(Localization.getString("RangeFilterWizardPage.LowerBound")); //$NON-NLS-1$
        txtLow = new Text(cmpFilterOpts, SWT.BORDER);
        txtLow.addModifyListener(modifyListener);
        txtLow.addKeyListener(numberKeyListener);

        //High
        Label lblHigh = new Label(cmpFilterOpts, SWT.NONE);
        lblHigh.setText(Localization.getString("RangeFilterWizardPage.UpperBound")); //$NON-NLS-1$
        txtHigh = new Text(cmpFilterOpts, SWT.BORDER);
        txtHigh.addModifyListener(modifyListener);
        txtHigh.addKeyListener(numberKeyListener);

        new Label(cmpFilterOpts, SWT.NONE);    //Spacer

        //Style
        radInside = new Button(cmpFilterOpts, SWT.RADIO);
        radInside.setText(Localization.getString("RangeFilterWizardPage.InsideBounds")); //$NON-NLS-1$
        radInside.addSelectionListener(selectionListener);
        radInside.setSelection(true);
        radOutside = new Button(cmpFilterOpts, SWT.RADIO);
        radOutside.setText(Localization.getString("RangeFilterWizardPage.OutsideBounds")); //$NON-NLS-1$
        radOutside.addSelectionListener(selectionListener);

        chkInclusive = new Button(cmpFilterOpts, SWT.CHECK);
        chkInclusive.setText(Localization.getString("RangeFilterWizardPage.Inclusive")); //$NON-NLS-1$
        chkInclusive.addSelectionListener(selectionListener);

        new Label(cmpFilterOpts, SWT.NONE);    //Spacer

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

        String highText = txtHigh.getText().trim();
        String lowText = txtLow.getText().trim();

        // Allow inside/outside bounds only if the range is bounded (has upper & lower bounds)
        boolean bounded = highText.length() * lowText.length() != 0;
        radInside.setEnabled(bounded);
        radOutside.setEnabled(bounded);

        if (highText.length() + lowText.length() == 0) {
            setErrorMessage(Localization.getString("RangeFilterWizardPage.EmptyError")); //$NON-NLS-1$
            return;
        }
        try {
            Double high = highText.length() > 0 ? Double.parseDouble(txtHigh.getText().trim()) : null;
            Double low = lowText.length() > 0 ? Double.parseDouble(txtLow.getText().trim()) : null;
            if (low != null && high != null && low >= high) {
                setErrorMessage(Localization.getString("RangeFilterWizardPage.BoundError")); //$NON-NLS-1$
                return;
            }

            // By default, if range is unbounded, force "inside" bounds to apply.
            int style = (!radInside.isEnabled() || radInside.getSelection() ? RangeFilter.INSIDE_BOUNDS : RangeFilter.OUTSIDE_BOUNDS);
            int selected = cboColumn.getSelectionIndex();
            if(chkInclusive.getSelection()) {
                style |= RangeFilter.INCLUSIVE;
            }
            filter = new RangeFilter(selected, low, high, style);
        } catch(NumberFormatException nfe) {}
    }

    @Override
    public void dispose() {
        super.dispose();
        if(null != cboColumn) {
            cboColumn.removeSelectionListener(selectionListener);
            cboColumn.dispose();
            cboColumn = null;
        }

        if(null != txtLow) {
            txtLow.removeModifyListener(modifyListener);
            txtLow.removeKeyListener(numberKeyListener);
            txtLow.dispose();
            txtLow = null;
        }

        if(null != txtHigh) {
            txtHigh.removeModifyListener(modifyListener);
            txtHigh.removeKeyListener(numberKeyListener);
            txtHigh.dispose();
            txtHigh = null;
        }

        if(null != chkInclusive) {
            chkInclusive.removeSelectionListener(selectionListener);
            chkInclusive.dispose();
            chkInclusive = null;
        }

        if(null != radInside) {
            radInside.removeSelectionListener(selectionListener);
            radInside.dispose();
            radInside = null;
        }

        if(null != radOutside) {
            radOutside.removeSelectionListener(selectionListener);
            radOutside.dispose();
            radOutside = null;
        }
    }

    private final KeyListener numberKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if((e.character >= ' ' && e.character <= '~')
                && (e.character > '9' || e.character < '0')) {
                e.doit = false;
            }
            if(e.character == '.' && !((Text)e.widget).getText().contains(".")) { //$NON-NLS-1$
                e.doit = true;
            }
        }

    };

    private Combo cboColumn;
    private Text txtLow, txtHigh;
    private Button chkInclusive;
    private Button radInside, radOutside;
}
