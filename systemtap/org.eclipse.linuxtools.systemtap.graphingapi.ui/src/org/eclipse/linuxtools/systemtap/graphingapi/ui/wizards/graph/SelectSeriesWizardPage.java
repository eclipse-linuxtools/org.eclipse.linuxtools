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

package org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ColumnLayout;



/**
 * @deprecated See {@link SelectGraphAndSeriesWizardPage}
 */
@Deprecated
public class SelectSeriesWizardPage extends WizardPage {
	public SelectSeriesWizardPage() {
		super("selectSeries"); //$NON-NLS-1$
		setTitle(Localization.getString("SelectSeriesWizardPage.SelectSeries")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		edit = ((SelectGraphWizard)super.getWizard()).isEditing();
		model = ((SelectGraphWizard)super.getWizard()).model;

		//Set the layout data
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new FormLayout());
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0, 0);
		data1.top = new FormAttachment(0, 0);
		data1.right = new FormAttachment(40, 0);
		data1.bottom = new FormAttachment(100, 0);

		//Add the title wigets
		Label lblTitle = new Label(comp, SWT.NONE);
		lblTitle.setText(Localization.getString("SelectSeriesWizardPage.Title")); //$NON-NLS-1$
		txtTitle = new Text(comp, SWT.BORDER);
		if (edit) {
			txtTitle.setText(model.getGraphData().title);
		}
		txtTitle.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getWizard().getContainer().updateButtons();
				if(txtTitle.getText().length() == 0) {
					setErrorMessage(Localization.getString("SelectSeriesWizardPage.TitleNotSet")); //$NON-NLS-1$
					setMessage(null);
				} else {
					setErrorMessage(null);
					setMessage(""); //$NON-NLS-1$
				}
			}
		});
		data1 = new FormData();
		data1.left = new FormAttachment(0,0);
		data1.top = new FormAttachment(0,0);
		data1.width = 200;
		lblTitle.setLayoutData(data1);

		data1 = new FormData();
		data1.left = new FormAttachment(0,0);
		data1.top = new FormAttachment(lblTitle,0);
		data1.width = 200;
		txtTitle.setLayoutData(data1);


		//Add the data series widgets
		data1 = new FormData();
		data1.left = new FormAttachment(0, 0);
		data1.top = new FormAttachment(txtTitle, 20);
		data1.right = new FormAttachment(40, 0);
		data1.bottom = new FormAttachment(100, 0);

		Composite cmpGraphOpts = new Composite(comp, SWT.NONE);
		cmpGraphOpts.setLayoutData(data1);
		ColumnLayout colLayout = new ColumnLayout();
		colLayout.maxNumColumns = 1;
		cmpGraphOpts.setLayout(colLayout);

		String[] labels = model.getSeries();

		cboYItems = new Combo[labels.length];
		lblYItems = new Label[cboYItems.length];

		Label lblXItem = new Label(cmpGraphOpts, SWT.NONE);
		lblXItem.setText(Localization.getString("SelectSeriesWizardPage.XSeries")); //$NON-NLS-1$
		cboXItem = new Combo(cmpGraphOpts, SWT.DROP_DOWN);
		cboXItem.addSelectionListener(new ComboSelectionListener());
		cboXItem.add(Localization.getString("SelectSeriesWizardPage.RowID")); //$NON-NLS-1$

		new Label(cmpGraphOpts, SWT.NONE);	//Spacer

		for(int i=0; i<cboYItems.length; i++) {
			lblYItems[i] = new Label(cmpGraphOpts, SWT.NONE);
			lblYItems[i].setText(Localization.getString("SelectSeriesWizardPage.YSeries") + i + ":"); //$NON-NLS-1$ //$NON-NLS-2$
			cboYItems[i] = new Combo(cmpGraphOpts, SWT.DROP_DOWN);
			cboYItems[i].addSelectionListener(new ComboSelectionListener());

			if(i>0) {
				cboYItems[i].add(Localization.getString("SelectSeriesWizardPage.NA")); //$NON-NLS-1$
				cboYItems[i].setVisible(false);
				lblYItems[i].setVisible(false);
			}
		}

		for(int j,i=0; i<labels.length; i++) {
			cboXItem.add(labels[i]);
			for(j=0; j<lblYItems.length; j++)
				cboYItems[j].add(labels[i]);
		}

		cboXItem.select(edit ? model.getXSeries() + 1 : 0);
		boolean cvisible = edit;
		if (edit) {
			cboYItems[0].select(model.getYSeries()[0]);
		}
		for(int i=1; i<cboYItems.length; i++) {
			int index = edit && model.getYSeries().length > i ? model.getYSeries()[i] + 1 : 0;
			cboYItems[i].select(index);
			cboYItems[i].setVisible(cvisible);
			lblYItems[i].setVisible(cvisible);
			cvisible = (index > 0);
		}

		//Add the key filter wigets
		btnKey = new Button(comp, SWT.CHECK);
		btnKey.setText(Localization.getString("SelectSeriesWizardPage.UseHistoricalData")); //$NON-NLS-1$
		btnKey.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtKey.setEnabled(btnKey.getSelection());
			}
		});

		data1 = new FormData();
		data1.left = new FormAttachment(cmpGraphOpts, 20);
		data1.top = new FormAttachment(txtTitle, 0);
		data1.right = new FormAttachment(100, 0);
		btnKey.setLayoutData(data1);

		lblKey = new Label(comp, SWT.NONE);
		lblKey.setText(Localization.getString("SelectSeriesWizardPage.KeyFilter")); //$NON-NLS-1$
		txtKey = new Text(comp, SWT.BORDER);
		if (edit && model.getGraphData().key != null) {
			txtKey.setText(model.getGraphData().key);
		}

		if(null != txtKey) {
			txtKey.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					getWizard().getContainer().updateButtons();
					if(txtTitle.getText().length() == 0) {
						setErrorMessage(Localization.getString("SelectSeriesWizardPage.KeyNotSet")); //$NON-NLS-1$
						setMessage(null);
					} else {
						setErrorMessage(null);
						setMessage(""); //$NON-NLS-1$
					}
				}
			});
		}

		data1 = new FormData();
		data1.left = new FormAttachment(cmpGraphOpts, 20);
		data1.top = new FormAttachment(btnKey, 0);
		data1.right = new FormAttachment(80, 0);
		lblKey.setLayoutData(data1);

		data1 = new FormData();
		data1.left = new FormAttachment(cmpGraphOpts, 20);
		data1.top = new FormAttachment(lblKey, 2);
		data1.right = new FormAttachment(80, 0);
		txtKey.setLayoutData(data1);

		if (edit) {
			setKeyEnablement(GraphFactory.isKeyRequired(model.getGraphID(), model.getDataSet()),
							 GraphFactory.isKeyOptional(model.getGraphID(), model.getDataSet()));
		}

		//Make comp visible
		setControl(comp);
	}

	public void setKeyEnablement(boolean required, boolean optional) {
		btnKey.setVisible(optional);
		txtKey.setVisible(required || optional);
		lblKey.setVisible(required || optional);
		txtKey.setEnabled(required);
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public boolean isPageComplete() {
		return saveDataToModel();
	}

	private boolean saveDataToModel() {
		if(isSeriesUnique()) {
			model.setTitle(txtTitle.getText());

			if(null != txtKey && txtKey.isEnabled())
				model.setKey(txtKey.getText());
			else
				model.setKey(null);

			model.setXSeries(cboXItem.getSelectionIndex()-1);

			int i, count;
			for(i=1, count=1; i<cboYItems.length; i++)
				if(cboYItems[i].isVisible() && 0 != cboYItems[i].getSelectionIndex())
					count++;

			int[] ySeries = new int[count];
			ySeries[0] = cboYItems[0].getSelectionIndex();
			for(i=1; i<count; i++)
				ySeries[i] = cboYItems[i].getSelectionIndex()-1;
			model.setYSeries(ySeries);
			return true;
		}
		return false;
	}

	private boolean isSeriesUnique() {
		if("".equals(txtTitle.getText().trim())) //$NON-NLS-1$
			return false;
		if(null != txtKey && txtKey.isEnabled() && txtKey.getText().length() <= 0)
			return false;

		for(int j,i=0; i<cboYItems.length; i++) {
			if(cboYItems[i].isVisible()) {
				for(j=i+1; j<cboYItems.length; j++) {
					if(cboYItems[j].isVisible()) {
						if(0 > cboYItems[i].getSelectionIndex())
							return false;
						else if(0 > cboYItems[j].getSelectionIndex())
							return false;
						else if(cboYItems[i].getItem(cboYItems[i].getSelectionIndex()).equals(cboYItems[j].getItem(cboYItems[j].getSelectionIndex()))) {
							return false;
						}
					}
				}
				if(0 > cboYItems[i].getSelectionIndex())
					return false;
				else if(0 > cboXItem.getSelectionIndex())
					return false;
				else if(cboYItems[i].getItem(cboYItems[i].getSelectionIndex()).equals(cboXItem.getItem(cboXItem.getSelectionIndex()))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
		if(null != txtTitle)
			txtTitle.dispose();
		txtTitle = null;

		if(null != txtKey)
			txtKey.dispose();
		txtKey = null;
		if(null != btnKey)
			btnKey.dispose();
		btnKey = null;
		if(null != lblKey)
			lblKey.dispose();
		lblKey = null;

		if(null != cboXItem)
			cboXItem.dispose();
		cboXItem = null;
		if(null != cboYItems) {
			for(int i=0; i<cboYItems.length; i++) {
				if(null != cboYItems[i])
					cboYItems[i].dispose();
				cboYItems[i] = null;
				if(null != lblYItems[i])
					lblYItems[i].dispose();
				lblYItems[i] = null;
			}
		}
		cboYItems = null;
		lblYItems = null;
		model = null;
	}

	private class ComboSelectionListener implements SelectionListener {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(!cboXItem.equals(e.getSource())) {
				boolean setVisible = true;
				if(GraphFactory.isMultiGraph(model.getGraphID())) {
					for(int i=1; i<cboYItems.length; i++) {
						cboYItems[i].setVisible(setVisible);
						lblYItems[i].setVisible(setVisible);
						if(cboYItems[i].getSelectionIndex() > 0 && cboYItems[i].isVisible())
							setVisible = true;
						else
							setVisible = false;
					}
				}
			}

			if(!isSeriesUnique()) {
				setErrorMessage(Localization.getString("SelectSeriesWizardPage.SeriesNotSelected")); //$NON-NLS-1$
				setMessage(null);
			} else {
				setErrorMessage(null);
				setMessage(""); //$NON-NLS-1$
			}
			if(txtTitle.getText().length() == 0) {
				setErrorMessage(Localization.getString("SelectSeriesWizardPage.TitleNotSet")); //$NON-NLS-1$
				setMessage(null);
			}

			getWizard().getContainer().updateButtons();
		}
	}

	private Text txtTitle;		//TODO: Move this to another page once graphs get more detail
	private Text txtKey;
	private Button btnKey;
	private Label lblKey;
	private Combo cboXItem;
	private Combo[] cboYItems;
	private Label[] lblYItems;
	private GraphModel model;
	private boolean edit;
}