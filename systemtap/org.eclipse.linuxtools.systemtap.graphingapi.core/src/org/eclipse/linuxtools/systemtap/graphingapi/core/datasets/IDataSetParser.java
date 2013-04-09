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

package org.eclipse.linuxtools.systemtap.graphingapi.core.datasets;

import org.eclipse.ui.IMemento;


public interface IDataSetParser {
	IDataEntry parse(StringBuilder entry);
	boolean saveXML(IMemento target);

	String XMLDataSetSettings = "DataSetSettings"; //$NON-NLS-1$
	String XMLparsingExpression = "parsingExpression"; //$NON-NLS-1$
	String XMLparsingSpacer = "parsingSpacer"; //$NON-NLS-1$
	String XMLColumn = "Column"; //$NON-NLS-1$
	String XMLdataset = "dataset"; //$NON-NLS-1$
	String XMLFile = "File"; //$NON-NLS-1$
	String XMLSeries = "Series"; //$NON-NLS-1$
	String XMLname = "name"; //$NON-NLS-1$
	String XMLDelimiter = "Delimiter"; //$NON-NLS-1$
}
