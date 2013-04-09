/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog;

import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.ChartStreamDaemon;

/**
 * Listens for data events and populates the internal <code>DataSet</code> with the data
 * that was received and matched the regExpr
 * @author Ryan Morse
 * @since 2.0
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

		outputData.append(console.getCommand().getOutput());
	}

	/**
	 * Disposes of all the internal data.
	 */
	@Override
	public void dispose() {
		if(!isDisposed()) {
			super.dispose();
			console = null;
		}
	}

	private ScriptConsole console;
}
