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
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;



public class SelectTableParsingWizardPage extends ParsingWizardPage {
	public SelectTableParsingWizardPage() {
		super("selectTableDataSetParsing"); //$NON-NLS-1$
		setTitle(Localization.getString("SelectTableParsingWizardPage.SelectTableDataSetParsing")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		Composite comp = new Composite(parent, SWT.NONE);
		Label l = new Label(comp, SWT.NONE);
		l.setText(Localization.getString("SelectTableParsingWizardPage.TableDelimiter")); //$NON-NLS-1$
		l.setBounds(5, 350, 150, 25);
		txtDelim = new Text(comp, SWT.SINGLE | SWT.BORDER);

		createColumnSelector(comp);

		txtDelim.setBounds(160, 350, 200, 25);
		txtDelim.addModifyListener(textListener);

		setControl(comp);
	}

	@Override
	public boolean checkComplete() {
		if(super.checkComplete() && txtDelim.getText().length() > 0) {
			try {
				wizard.parser = new TableParser(regEx, txtDelim.getText());
				wizard.dataSet = DataSetFactory.createDataSet(TableDataSet.ID, labels);
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
		if(null != txtDelim) {
			txtDelim.removeModifyListener(textListener);
			txtDelim.dispose();
			txtDelim = null;
		}
	}

	protected Text txtDelim;
}
