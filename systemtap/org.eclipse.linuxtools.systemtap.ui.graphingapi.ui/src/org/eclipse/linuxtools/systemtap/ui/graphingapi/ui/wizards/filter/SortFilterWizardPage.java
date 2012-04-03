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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.filter;

import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.SortFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ColumnLayout;



public class SortFilterWizardPage extends FilterWizardPage {
	public SortFilterWizardPage() {
		super("selectFilterOptions");
		setTitle(Localization.getString("SortFilterWizardPage.CreateSortFilter"));
	}
	
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
		lblColumn.setText(Localization.getString("SortFilterWizardPage.Column"));
		cboColumn = new Combo(cmpFilterOpts, SWT.DROP_DOWN);
		cboColumn.addSelectionListener(selectionListener);
		for(int i=0; i<wizard.series.length; i++)
			cboColumn.add(wizard.series[i]);

		new Label(cmpFilterOpts, SWT.NONE);	//Spacer
		
		//Style
		radAscending = new Button(cmpFilterOpts, SWT.RADIO);
		radAscending.setText(Localization.getString("SortFilterWizardPage.Ascending"));
		radAscending.addSelectionListener(selectionListener);
		radAscending.setSelection(true);
		radDescending = new Button(cmpFilterOpts, SWT.RADIO);
		radDescending.setText(Localization.getString("SortFilterWizardPage.Descending"));
		radDescending.addSelectionListener(selectionListener);

		setControl(comp);
	}
	
	public boolean canFlipToNextPage() {
		return false;
	}

	protected void createFilter() {
		int selected = cboColumn.getSelectionIndex();
		int style = (radAscending.getSelection() ? SortFilter.ASCENDING : SortFilter.DESCENDING);
		if(selected >=0 && selected < cboColumn.getItemCount())
			wizard.filter = new SortFilter(selected, style);
	}

	public void dispose() {
		if(null != cboColumn) {
			cboColumn.removeSelectionListener(selectionListener);
			cboColumn.dispose();
			cboColumn = null;
		}

		if(null != radAscending) {
			radAscending.removeSelectionListener(selectionListener);
			radAscending.dispose();
			radAscending = null;
		}

		if(null != radDescending) {
			radDescending.removeSelectionListener(selectionListener);
			radDescending.dispose();
			radDescending = null;
		}

		super.dispose();
	}

	private Combo cboColumn;
	private Button radAscending, radDescending;
}