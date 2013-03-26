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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets;

import org.eclipse.ui.IMemento;


public interface IDataSetParser {
	public IDataEntry parse(StringBuilder entry);
	public boolean saveXML(IMemento target);

	public static final String XMLDataSetSettings = "DataSetSettings"; //$NON-NLS-1$
	public static final String XMLparsingExpression = "parsingExpression"; //$NON-NLS-1$
	public static final String XMLparsingSpacer = "parsingSpacer"; //$NON-NLS-1$
	public static final String XMLColumn = "Column"; //$NON-NLS-1$
	public static final String XMLdataset = "dataset"; //$NON-NLS-1$
	public static final String XMLFile = "File"; //$NON-NLS-1$
	public static final String XMLSeries = "Series"; //$NON-NLS-1$
	public static final String XMLname = "name"; //$NON-NLS-1$
	public static final String XMLDelimiter = "Delimiter"; //$NON-NLS-1$
}
