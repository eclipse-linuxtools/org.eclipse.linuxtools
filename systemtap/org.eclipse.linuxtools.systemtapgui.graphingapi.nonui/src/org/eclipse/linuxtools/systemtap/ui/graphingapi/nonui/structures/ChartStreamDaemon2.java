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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures;


import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSetParser;


/**
 * Listens for data events and populates the internal <code>DataSet</code> with the data
 * that was received and matched the regExpr
 * @author Ryan Morse
 */
public class ChartStreamDaemon2 extends ChartStreamDaemon {
	public ChartStreamDaemon2(ScriptConsole console, IDataSet d, IDataSetParser p) {
		super(d, p);
		
	}

	/**
	 * Changes the internal <code>DataSet</code> and parsing expression.
	 * @param d The new <code>DataSet</code> to store the parsed output.
	 * @param regExpr String[] containing the RegEx patterns to match against.
	 */
	public void setParser(IDataSet d, IDataSetParser p) {
		data = d;
		parser = p;
		if(0 != outputData.length())
			outputData.delete(0, outputData.length()-1);
		outputData.append(console.getOutput());
	}
	
	/**
	 * Disposes of all the internal data.
	 */
	public void dispose() {
		if(!isDisposed()) {
			super.dispose();
			console = null;
		}
	}

	private ScriptConsole console;
}
