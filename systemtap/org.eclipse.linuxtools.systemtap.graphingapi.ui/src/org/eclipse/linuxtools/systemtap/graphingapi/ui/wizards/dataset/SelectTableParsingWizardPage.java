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
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;



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

			if(0 != children[i].getString(IDataSetParser.XMLdataset).compareTo(TableDataSet.ID)) {
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
			txtDelim.setText(children[i].getChild(IDataSetParser.XMLDelimiter).getString(IDataSetParser.XMLparsingExpression));

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
		for(IMemento memento:children) {
			child = newMeta.createChild(IDataSetParser.XMLColumn);
			child.putString(IDataSetParser.XMLname, memento.getString(IDataSetParser.XMLname));
		}
		children = oldMeta.getChildren(IDataSetParser.XMLSeries);
		for(IMemento memento:children) {
			child = newMeta.createChild(IDataSetParser.XMLSeries);
			child.putString(IDataSetParser.XMLparsingExpression, memento.getString(IDataSetParser.XMLparsingExpression));
			child.putString(IDataSetParser.XMLparsingSpacer, memento.getString(IDataSetParser.XMLparsingSpacer));
		}
		newMeta.createChild(IDataSetParser.XMLDelimiter).putString(IDataSetParser.XMLparsingExpression, oldMeta.getChild(IDataSetParser.XMLDelimiter).getString(IDataSetParser.XMLparsingExpression));
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
