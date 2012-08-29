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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.dataset;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ColumnLayout;



public class SelectDataSetWizardPage extends WizardPage {
	public SelectDataSetWizardPage() {
		super("selectDataSetType");
		setTitle(Localization.getString("SelectDataSetWizardPage.SelectDataSetType"));
		dataSetID = "";
		btnDataSets = null;
	}
	
	public void createControl(Composite parent) {
		wizard = (DataSetWizard)super.getWizard();
		
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
		
		String[] ids = DataSetFactory.getIDs();
		btnDataSets = new Button[ids.length];
		for(int i=0; i<btnDataSets.length; i++) {
			btnDataSets[i] = new Button(cmpFilterOpts, SWT.NONE);
			btnDataSets[i].setText(DataSetFactory.getName(ids[i]));
			btnDataSets[i].addSelectionListener(buttonListener);
			btnDataSets[i].setData(ids[i]);
		}
		
		FormData data2 = new FormData();
		data2.left = new FormAttachment(cmpFilterOpts);
		data2.top = new FormAttachment(0, 0);
		data2.right = new FormAttachment(100, 0);
		data2.bottom = new FormAttachment(100, 0);
		
		lblDesc = new Label(comp, SWT.WRAP);
		lblDesc.setLayoutData(data2);
		
		setControl(comp);
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return (dataSetID.length() > 0);
	}
	
	@Override
	public IWizardPage getNextPage() {
		return DataSetFactory.getParsingWizardPage(dataSetID);
	}

	@Override
	public void dispose() {
		super.dispose();
		if(null != btnDataSets)
			for(int i=0; i<btnDataSets.length; i++) {
				btnDataSets[i].removeSelectionListener(buttonListener);
				btnDataSets[i].dispose();
				btnDataSets[i] = null;
			}
		btnDataSets = null;
		lblDesc = null;
	}

	private SelectionListener buttonListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {}
		
		public void widgetSelected(SelectionEvent e) {
			if(e.widget instanceof Button) {
				Button target = (Button)e.widget;

				for(int i=0; i<btnDataSets.length; i++) {
					if(target == btnDataSets[i]) {
						dataSetID = btnDataSets[i].getData().toString();
						lblDesc.setText(DataSetFactory.getName(dataSetID) + "\n\n" +
										DataSetFactory.getDescription(dataSetID));
						wizard.getContainer().updateButtons();
					}
				}
			}
		}
	};
	
	private Button[] btnDataSets;
	private Label lblDesc;
	private String dataSetID;
	private DataSetWizard wizard;
}
