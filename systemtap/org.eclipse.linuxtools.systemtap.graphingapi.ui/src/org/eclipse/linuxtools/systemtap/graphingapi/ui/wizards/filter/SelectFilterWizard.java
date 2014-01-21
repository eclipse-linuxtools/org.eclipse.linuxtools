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

package org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.filter;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class SelectFilterWizard extends Wizard implements INewWizard {
	public SelectFilterWizard(String[] series) {
		filter = null;
		this.series = Arrays.copyOf(series, series.length);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		setWindowTitle(Localization.getString("SelectFilterWizard.CreateFilter")); //$NON-NLS-1$
		selectFilterPage = new SelectFilterWizardPage();
		addPage(selectFilterPage);

		filterOptionsPages = new FilterWizardPage[AvailableFilterTypes.filterIDs.length];
		for(int i=0; i<AvailableFilterTypes.filterIDs.length; i++) {
			filterOptionsPages[i] = AvailableFilterTypes.getFilterWizardPage(AvailableFilterTypes.filterIDs[i]);
			addPage(filterOptionsPages[i]);
		}
	}

	@Override
	public boolean canFinish() {
		return getCurrentFilter() != null;
	}

	private IDataSetFilter getCurrentFilter() {
		IWizardPage page = this.getContainer().getCurrentPage();
		return page instanceof FilterWizardPage ? ((FilterWizardPage) page).getFilter() : null;
	}

	@Override
	public boolean performFinish() {
		filter = getCurrentFilter();
		return true;
	}

	public IDataSetFilter getFilter() {
		return filter;
	}

	@Override
	public void dispose() {
		if(null != selectFilterPage) {
			selectFilterPage.dispose();
		}
		selectFilterPage = null;
		if(null != filterOptionsPages) {
			for(int i=0; i<filterOptionsPages.length; i++) {
				filterOptionsPages[i].dispose();
				filterOptionsPages[i] = null;
			}
		}
		filterOptionsPages = null;
		series = null;

		super.dispose();
	}

	private SelectFilterWizardPage selectFilterPage;
	private FilterWizardPage[] filterOptionsPages;
	public String[] series;

	public IDataSetFilter filter;
}