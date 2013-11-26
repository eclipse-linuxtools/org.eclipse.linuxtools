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

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @deprecated The means of creating/editing graph configurations has
 * deviated significantly from this implementation.
 * TODO remove in 3.0
 */
@Deprecated
public class DataSetWizard extends Wizard implements INewWizard {
	public DataSetWizard(File metaFile, String scriptFile) {
		this.metaFile = metaFile;
		this.scriptFile = scriptFile;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {}

	@Override
	public void addPages() {
		setWindowTitle(Localization.getString("DataSetWizard.CreateDataSet")); //$NON-NLS-1$
		dataSetPage = new SelectDataSetWizardPage();
		addPage(dataSetPage);

		String[] ids = DataSetFactory.getIDs();
		parsingPages = new ParsingWizardPage[ids.length];
		for(int i=0; i<ids.length; i++) {
			parsingPages[i] = DataSetFactory.getParsingWizardPage(ids[i]);
			addPage(parsingPages[i]);
		}

		((WizardDialog)getContainer()).addPageChangedListener(pageListener);
	}

	@Override
	public boolean canFinish() {
		IWizardPage page = this.getContainer().getCurrentPage();
		if((null != dataSet) && (null != parser) && (page instanceof ParsingWizardPage)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean performCancel() {
		parser = null;
		dataSet = null;
		return true;
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public IDataSetParser getParser() {
		return parser;
	}

	public IDataSet getDataSet() {
		return dataSet;
	}

	protected boolean openFile() {
		try {
			if (!metaFile.exists()) {
				metaFile.createNewFile();
			}
		} catch(IOException ioe) {
			return false;
		}

		return true;
	}

	@Override
	public void dispose() {
		if(null != getContainer()) {
			((WizardDialog)getContainer()).removePageChangedListener(pageListener);
		}
		if(null != dataSetPage) {
			dataSetPage.dispose();
		}
		if(null != parsingPages) {
			for(int i=0; i<parsingPages.length; i++) {
				if(null != parsingPages[i]) {
					parsingPages[i].dispose();
					parsingPages[i] = null;
				}
			}
			parsingPages = null;
		}
	}

	private IPageChangedListener pageListener = new IPageChangedListener() {
		@Override
		public void pageChanged(PageChangedEvent e) {
			if(e.getSelectedPage() instanceof ParsingWizardPage) {
				((ParsingWizardPage)e.getSelectedPage()).checkComplete();
				getContainer().updateButtons();
			}
		}
	};

	private SelectDataSetWizardPage dataSetPage;

	private ParsingWizardPage[] parsingPages;
	public String scriptFile;
	public File metaFile;
	public IDataSet dataSet;
	public IDataSetParser parser;
}
