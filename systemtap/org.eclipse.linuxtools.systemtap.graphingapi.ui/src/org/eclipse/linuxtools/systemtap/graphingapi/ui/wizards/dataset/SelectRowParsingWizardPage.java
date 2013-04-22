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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.PatternSyntaxException;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;


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
	protected boolean readParsingExpression() {
		if(null == wizard.metaFile && !wizard.openFile()) {
			return false;
		}

		try {
			FileReader reader = new FileReader(wizard.metaFile);

			if(!reader.ready()) {
				reader.close();
				return false;
			}

			XMLMemento data = XMLMemento.createReadRoot(reader, IDataSetParser.XMLDataSetSettings);

			IMemento[] children = data.getChildren(IDataSetParser.XMLFile);
			int i;
			for(i=0; i<children.length; i++) {
				if(children[i].getID().equals(wizard.scriptFile)) {
					break;
				}
			}

			if(i>=children.length) {
				return false;
			}

			if(0 != children[i].getString(IDataSetParser.XMLdataset).compareTo(RowDataSet.ID)) {
				return false;
			}

			IMemento[] children2 = children[i].getChildren(IDataSetParser.XMLColumn);
			txtSeries.setText("" + children2.length); //$NON-NLS-1$
			for(int j=0; j<children2.length; j++) {
				txtRegExpr[j*COLUMNS].setText(children2[j].getString(IDataSetParser.XMLname));
			}

			children2 = children[i].getChildren(IDataSetParser.XMLSeries);
			txtSeries.setText("" + children2.length); //$NON-NLS-1$
			for(int j=0; j<children2.length; j++) {
				txtRegExpr[j*COLUMNS+1].setText(children2[j].getString(IDataSetParser.XMLparsingExpression));
				txtRegExpr[j*COLUMNS+2].setText(children2[j].getString(IDataSetParser.XMLparsingSpacer));
			}

			reader.close();
		} catch(FileNotFoundException fnfe) {
			return false;
		} catch(WorkbenchException we) {
			return false;
		} catch(IOException ioe) {
			return false;
		}

		return true;
	}

	@Override
	protected void copyExisting(IMemento oldMeta, IMemento newMeta) {
		IMemento[] children = oldMeta.getChildren(IDataSetParser.XMLColumn);
		IMemento child;
		for(IMemento memento :children) {
			child = newMeta.createChild(IDataSetParser.XMLColumn);
			child.putString(IDataSetParser.XMLname, memento.getString(IDataSetParser.XMLname));
		}
		children = oldMeta.getChildren(IDataSetParser.XMLSeries);
		for(IMemento memento :children) {
			child = newMeta.createChild(IDataSetParser.XMLSeries);
			child.putString(IDataSetParser.XMLparsingExpression, memento.getString(IDataSetParser.XMLparsingExpression));
			child.putString(IDataSetParser.XMLparsingSpacer, memento.getString(IDataSetParser.XMLparsingSpacer));
		}
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
