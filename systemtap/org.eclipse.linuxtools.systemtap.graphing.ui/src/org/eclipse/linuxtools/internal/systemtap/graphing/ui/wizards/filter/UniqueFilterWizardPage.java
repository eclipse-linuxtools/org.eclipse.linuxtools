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
import org.eclipse.linuxtools.systemtap.graphing.core.filters.UniqueFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ColumnLayout;



public class UniqueFilterWizardPage extends FilterWizardPage {
    public UniqueFilterWizardPage() {
        super("selectFilterOptions"); //$NON-NLS-1$
        setTitle(Localization.getString("UniqueFilterWizardPage.CreateUniqueFilter")); //$NON-NLS-1$
        aggregateID = ""; //$NON-NLS-1$
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
        lblColumn.setText(Localization.getString("UniqueFilterWizardPage.Column")); //$NON-NLS-1$
        cboColumn = new Combo(cmpFilterOpts, SWT.DROP_DOWN | SWT.READ_ONLY);
        cboColumn.addSelectionListener(selectionListener);
        for (String series : wizard.series) {
            cboColumn.add(series);
        }

        new Label(cmpFilterOpts, SWT.NONE);    //Spacer

        //String to compare to
        Label lblAggregate = new Label(cmpFilterOpts, SWT.NONE);
        lblAggregate.setText(Localization.getString("UniqueFilterWizardPage.Aggregate")); //$NON-NLS-1$

        btnAggregates = new Button[AggregateFactory.AGGREGATES.length];
        for(int i=0; i<btnAggregates.length; i++) {
            btnAggregates[i] = new Button(cmpFilterOpts, SWT.RADIO);
            btnAggregates[i].setText(AggregateFactory.getAggregateName(AggregateFactory.AGGREGATES[i]));
            btnAggregates[i].addSelectionListener(btnSelectionListener);
            btnAggregates[i].setData(AggregateFactory.AGGREGATES[i]);
        }

        FormData data2 = new FormData();
        data2.left = new FormAttachment(cmpFilterOpts);
        data2.top = new FormAttachment(0, 0);
        data2.right = new FormAttachment(100, 0);
        data2.bottom = new FormAttachment(100, 0);

        lblDesc = new Label(comp, SWT.WRAP);
        lblDesc.setLayoutData(data2);

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
        int selected = cboColumn.getSelectionIndex();
        if(selected >=0 && selected < cboColumn.getItemCount()) {
            if(null != aggregateID && !aggregateID.isEmpty()) {
                filter = new UniqueFilter(
                        selected,
                        AggregateFactory.createAggregate(aggregateID));
            }
        }
    }

    @Override
    public void dispose() {
        if(null != cboColumn) {
            cboColumn.removeSelectionListener(selectionListener);
            cboColumn.dispose();
            cboColumn = null;
        }

        if(null != lblDesc) {
            lblDesc.dispose();
        }
        lblDesc = null;

        if(null != btnAggregates) {
            for(int i=0; i<btnAggregates.length; i++) {
                btnAggregates[i].removeSelectionListener(btnSelectionListener);
                btnAggregates[i].dispose();
                btnAggregates[i] = null;
            }
            btnAggregates = null;
        }

        aggregateID = null;

        super.dispose();
    }

    private final SelectionListener btnSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if(e.widget instanceof Button) {
                Button target = (Button)e.widget;

                for (Button button : btnAggregates) {
                    if(target == button) {
                        lblDesc.setText(AggregateFactory.getAggregateName(button.getData().toString()) + "\n\n" + //$NON-NLS-1$
                                AggregateFactory.getAggregateDescription(button.getData().toString()));
                        aggregateID = button.getData().toString();
                        createFilter();
                        wizard.getContainer().updateButtons();
                    }
                }
            }
        }
    };

    private Combo cboColumn;
    private Button[] btnAggregates;
    private Label lblDesc;
    private String aggregateID;
}
