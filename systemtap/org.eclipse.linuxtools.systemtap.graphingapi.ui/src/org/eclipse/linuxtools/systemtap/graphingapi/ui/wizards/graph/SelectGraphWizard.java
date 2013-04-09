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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;



public class SelectGraphWizard extends Wizard implements INewWizard {
	public SelectGraphWizard(IDataSet data) {
		model = new GraphModel(data);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		setWindowTitle(Localization.getString("SelectGraphWizard.CreateGraph")); //$NON-NLS-1$
		selectGraphPage = new SelectGraphWizardPage();
		addPage(selectGraphPage);
		selectSeriesPage = new SelectSeriesWizardPage();
		addPage(selectSeriesPage);
	}

	@Override
	public boolean canFinish() {
		if (this.getContainer().getCurrentPage() == selectSeriesPage &&
			selectSeriesPage.isPageComplete())
			return true;
		return false;
	}

	@Override
	public boolean performCancel() {
		model.destroy();
		return true;
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public GraphData getGraphData() {
		return model.getGraphData();
	}

	@Override
	public void dispose() {
		if(null != selectGraphPage)
			selectGraphPage.dispose();
		if(null != selectSeriesPage)
			selectSeriesPage.dispose();
		super.dispose();
	}

	public SelectGraphWizardPage selectGraphPage;
	public SelectSeriesWizardPage selectSeriesPage;
	public GraphModel model;
}