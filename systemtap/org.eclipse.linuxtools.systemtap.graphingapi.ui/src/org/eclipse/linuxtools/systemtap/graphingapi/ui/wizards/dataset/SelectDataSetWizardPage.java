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

package org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;



public class SelectDataSetWizardPage extends WizardPage {
	public SelectDataSetWizardPage() {
		super("selectDataSetType"); //$NON-NLS-1$
		setTitle(Localization.getString("SelectDataSetWizardPage.SelectDataSetType")); //$NON-NLS-1$
		dataSetID = ""; //$NON-NLS-1$
		btnDataSets = null;
	}

	@Override
	public void createControl(Composite parent) {
		wizard = (DataSetWizard)super.getWizard();

		Composite comp = new Composite(parent, SWT.NULL);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		GridLayout gridLayout = new GridLayout(2, false);
		comp.setLayout(gridLayout);

		String[] ids = DataSetFactory.getIDs();
		btnDataSets = new Button[ids.length];
		Label lblDesc;
		for(int i=0; i<btnDataSets.length; i++) {
			btnDataSets[i] = new Button(comp, SWT.RADIO);
			btnDataSets[i].setText(DataSetFactory.getName(ids[i]));
			btnDataSets[i].addSelectionListener(buttonListener);
			btnDataSets[i].setData(ids[i]);
			lblDesc = new Label(comp, SWT.WRAP);
			lblDesc.setText(DataSetFactory.getDescription(btnDataSets[i].getData().toString()));
			Label separator = new Label(comp, SWT.HORIZONTAL | SWT.SEPARATOR);
			GridData separatorData = new GridData(GridData.FILL_HORIZONTAL);
			separatorData.horizontalSpan=2;
		    separator.setLayoutData(separatorData);
		}

		setControl(comp);
	}

	@Override
	public boolean canFlipToNextPage() {
		return !dataSetID.isEmpty();
	}

	@Override
	public IWizardPage getNextPage() {
		return DataSetFactory.getParsingWizardPage(dataSetID);
	}

	@Override
	public void dispose() {
		super.dispose();
		if(null != btnDataSets) {
			for(int i=0; i<btnDataSets.length; i++) {
				btnDataSets[i].removeSelectionListener(buttonListener);
				btnDataSets[i].dispose();
				btnDataSets[i] = null;
			}
		}
		btnDataSets = null;
	}

	private SelectionListener buttonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if(e.widget instanceof Button) {
				Button target = (Button)e.widget;

				for(Button button: btnDataSets) {
					if(target == button) {
						dataSetID = button.getData().toString();
						wizard.getContainer().updateButtons();
					}
				}
			}
		}
	};

	private Button[] btnDataSets;
	private String dataSetID;
	private DataSetWizard wizard;
}
