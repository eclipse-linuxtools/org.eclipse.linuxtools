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
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.RangeFilter;
import org.eclipse.swt.SWT;
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
		super("selectFilterOptions");
		setTitle(Localization.getString("RangeFilterWizardPage.CreateRangeFilter"));
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
		lblColumn.setText(Localization.getString("RangeFilterWizardPage.Column"));
		cboColumn = new Combo(cmpFilterOpts, SWT.DROP_DOWN);
		cboColumn.addSelectionListener(selectionListener);
		for(int i=0; i<wizard.series.length; i++)
			cboColumn.add(wizard.series[i]);

		new Label(cmpFilterOpts, SWT.NONE);	//Spacer

		//Low
		Label lblLow = new Label(cmpFilterOpts, SWT.NONE);
		lblLow.setText(Localization.getString("RangeFilterWizardPage.LowerBound"));
		txtLow = new Text(cmpFilterOpts, SWT.BORDER);
		txtLow.addModifyListener(modifyListener);
		txtLow.addKeyListener(numberKeyListener);
		
		new Label(cmpFilterOpts, SWT.NONE);	//Spacer

		//High
		Label lblHigh = new Label(cmpFilterOpts, SWT.NONE);
		lblHigh.setText(Localization.getString("RangeFilterWizardPage.UpperBound"));
		txtHigh = new Text(cmpFilterOpts, SWT.BORDER);
		txtHigh.addModifyListener(modifyListener);
		txtHigh.addKeyListener(numberKeyListener);
		
		new Label(cmpFilterOpts, SWT.NONE);	//Spacer

		//Style
		radInside = new Button(cmpFilterOpts, SWT.RADIO);
		radInside.setText(Localization.getString("RangeFilterWizardPage.InsideBounds"));
		radInside.addSelectionListener(selectionListener);
		radInside.setSelection(true);
		radOutside = new Button(cmpFilterOpts, SWT.RADIO);
		radOutside.setText(Localization.getString("RangeFilterWizardPage.OutsideBounds"));
		radOutside.addSelectionListener(selectionListener);

		chkInclusive = new Button(cmpFilterOpts, SWT.CHECK);
		chkInclusive.setText(Localization.getString("RangeFilterWizardPage.Inclusive"));

		setControl(comp);
	}
	
	public boolean canFlipToNextPage() {
		return false;
	}
	
	protected void createFilter() {
		int selected = cboColumn.getSelectionIndex();
		
		try {
			double high = Double.parseDouble(txtHigh.getText().trim());
			double low = Double.parseDouble(txtLow.getText().trim());
			int style = (radInside.getSelection() ? RangeFilter.INSIDE_BOUNDS : RangeFilter.OUTSIDE_BOUNDS);
			if(chkInclusive.getSelection())
				style |= RangeFilter.INCLUSIVE;
			
			if(selected >=0 && selected < cboColumn.getItemCount()) {
				if(low <= high)
					wizard.filter = new RangeFilter(selected, new Double(low), new Double(high), style);
			}
		} catch(NumberFormatException nfe) {}
	}

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
	
	private final KeyListener numberKeyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if((e.character >= ' ' && e.character <= '~')
				&& (e.character > '9' || e.character < '0'))
				e.doit = false;
			if(e.character == '.' && !((Text)e.widget).getText().contains("."))
				e.doit = true;
		}
		
		public void keyReleased(KeyEvent e) {}
	};

	private Combo cboColumn;
	private Text txtLow, txtHigh;
	private Button chkInclusive;
	private Button radInside, radOutside;
}