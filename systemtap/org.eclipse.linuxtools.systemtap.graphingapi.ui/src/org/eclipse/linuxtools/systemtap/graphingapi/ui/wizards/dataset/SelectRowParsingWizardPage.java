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

import java.util.regex.PatternSyntaxException;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


/**
 * @deprecated The means of creating/editing graph configurations has
 * deviated significantly from this implementation.
 * TODO remove in 3.0
 */
@Deprecated
public class SelectRowParsingWizardPage extends ParsingWizardPage {
	public SelectRowParsingWizardPage() {
		super("selectRowDataSetParsing"); //$NON-NLS-1$
		setTitle(Localization.getString("SelectRowParsingWizardPage.SelectRowDataSetParsing")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		Composite comp = new Composite(parent, SWT.NONE);
		createColumnSelector(comp);

		setControl(comp);
	}

	@Override
	public boolean checkComplete() {
		if(super.checkComplete()) {
			try {
				wizard.parser = new RowParser(regEx);
				wizard.dataSet = DataSetFactory.createDataSet(RowDataSet.ID, labels);
				return true;
			} catch(PatternSyntaxException pse) {}
		}
		wizard.parser = null;
		wizard.dataSet = null;
		return false;
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
