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

package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;


import java.io.IOException;

import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.structures.Messages;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.listeners.IGobblerListener;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * A class push data to a ScriptConsole.
 * @author Ryan Morse
 */
public class ConsoleStreamDaemon implements IGobblerListener {
	public ConsoleStreamDaemon(ScriptConsole console) {
		this.console = console;
		if(null != console) {
			ioConsole = console.newOutputStream();
		}
		disposed = false;
	}

	/**
	 * Prints out the new output data to the console
	 */
	protected void pushData() {
		if(null != ioConsole) {
			try {
				ioConsole.write(output);
			} catch (IOException e) {
				ExceptionErrorDialog.openError(Messages.ConsoleStreamDaemon_errorWritingToConsole, e);
			}
		}
	}

	/**
	 * Captures data events and pushes the data to the console
	 */
	@Override
	public void handleDataEvent(String line) {
		output = line;
		this.pushData();
	}

	/**
	 * Checks to see if the class has been disposed already
	 * @return boolean representing whether or not the class has been disposed
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * Disposes of all internal references in the class. No method should be called after this.
	 */
	public void dispose() {
		if(!disposed) {
			disposed = true;
			output = null;
			console = null;
			ioConsole = null;
		}
	}

	protected String output;
	protected ScriptConsole console;
	protected IOConsoleOutputStream ioConsole;
	private boolean disposed;
}
