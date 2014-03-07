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

package org.eclipse.linuxtools.systemtap.graphing.ui.wizards.filter;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ColumnLayout;



public class SelectFilterWizardPage extends WizardPage {
	public SelectFilterWizardPage() {
		super("selectFilter"); //$NON-NLS-1$
		setTitle(Localization.getString("SelectFilterWizardPage.SelectFilter")); //$NON-NLS-1$
		filterID = ""; //$NON-NLS-1$
		btnFilters = null;
		buttonListener = new ButtonSelectionListener();
	}

	@Override
	public void createControl(Composite parent) {
		wizard = (SelectFilterWizard)super.getWizard();

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

		btnFilters = new Button[AvailableFilterTypes.filterIDs.length];
		for(int i=0; i<btnFilters.length; i++) {
			btnFilters[i] = new Button(cmpFilterOpts, SWT.NONE);
			btnFilters[i].setText(AvailableFilterTypes.getFilterName(AvailableFilterTypes.filterIDs[i]));
			btnFilters[i].addSelectionListener(buttonListener);
			btnFilters[i].setData(AvailableFilterTypes.filterIDs[i]);
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
	public IWizardPage getNextPage() {
		return AvailableFilterTypes.getFilterWizardPage(filterID);
	}

	@Override
	public boolean canFlipToNextPage() {
		return (filterID.length() > 0);
	}

	@Override
	public void dispose() {
		super.dispose();
		if(null != btnFilters) {
			for(int i=0; i<btnFilters.length; i++) {
				btnFilters[i].removeSelectionListener(buttonListener);
				btnFilters[i].dispose();
				btnFilters[i] = null;
			}
		}
		btnFilters = null;
		lblDesc = null;
	}

	private class ButtonSelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(e.widget instanceof Button) {
				Button target = (Button)e.widget;

				for(int i=0; i<btnFilters.length; i++) {
					if(target == btnFilters[i]) {
						filterID = btnFilters[i].getData().toString();
						lblDesc.setText(AvailableFilterTypes.getFilterName(filterID) + "\n\n" + //$NON-NLS-1$
										AvailableFilterTypes.getFilterDescription(filterID));
						wizard.getContainer().updateButtons();
					}
				}
			}
		}
	}

	private Button[] btnFilters;
	private Label lblDesc;
	private String filterID;
	private SelectFilterWizard wizard;
	private ButtonSelectionListener buttonListener;
}