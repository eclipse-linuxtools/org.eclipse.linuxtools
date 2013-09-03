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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ColumnLayout;

/**
 * @deprecated See {@link SelectGraphAndSeriesWizardPage}
 */
@Deprecated
public class SelectGraphWizardPage extends WizardPage implements Listener {
	public SelectGraphWizardPage() {
		super("selectGraph"); //$NON-NLS-1$
		setTitle(Localization.getString("SelectGraphWizardPage.SelectGraph")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		wizard = (SelectGraphWizard)getWizard();

		Composite cmpGraphOpts = new Composite(parent, SWT.NONE);
		ColumnLayout colLayout = new ColumnLayout();
		colLayout.maxNumColumns = 1;
		cmpGraphOpts.setLayout(colLayout);

		String[] graphIDs = GraphFactory.getAvailableGraphs(wizard.model.getDataSet());
		btnGraphs = new Button[graphIDs.length];
		for(int i=0; i<btnGraphs.length; i++) {
			btnGraphs[i] = new Button(cmpGraphOpts, SWT.TOGGLE);
			btnGraphs[i].setImage(GraphFactory.getGraphImage(graphIDs[i]));
			btnGraphs[i].addListener(SWT.Selection, this);
			btnGraphs[i].setData(graphIDs[i]);
			btnGraphs[i].setToolTipText(GraphFactory.getGraphName(btnGraphs[i].getData().toString()) + "\n\n" + //$NON-NLS-1$
					GraphFactory.getGraphDescription(btnGraphs[i].getData().toString()));
			if (wizard.isEditing() && graphIDs[i].equals(wizard.model.getGraphID())) {
				btnGraphs[i].setSelection(true);
			}
		}
		setControl(cmpGraphOpts);
	}

	@Override
	public void handleEvent(Event event) {
		if(event.widget instanceof Button) {
			Button target = (Button)event.widget;
			for(int i=0; i<btnGraphs.length; i++)
				if(target == btnGraphs[i]) {
					saveDataToModel(btnGraphs[i].getData().toString());
					wizard.getContainer().updateButtons();
				} else {
					btnGraphs[i].setSelection(false);
				}
		}
	}

	@Override
	public IWizardPage getNextPage() {
		return wizard.selectSeriesPage;
	}

	@Override
	public boolean canFlipToNextPage() {
		return wizard.model.isGraphSet();
	}

	private void saveDataToModel(String selected) {
		GraphModel model = wizard.model;
		model.setGraph(selected);
		wizard.selectSeriesPage.setKeyEnablement(
				GraphFactory.isKeyRequired(model.getGraphID(), model.getDataSet()),
				GraphFactory.isKeyOptional(model.getGraphID(), model.getDataSet()));
	}

	@Override
	public void dispose() {
		super.dispose();
		if(null != btnGraphs)
			for(int i=0; i<btnGraphs.length; i++)
				btnGraphs[i] = null;
		btnGraphs = null;
	}

	private Button[] btnGraphs;
	private SelectGraphWizard wizard;
}