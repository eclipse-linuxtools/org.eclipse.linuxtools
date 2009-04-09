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

package org.eclipse.linuxtools.systemtapgui.consolelog.structures;

import org.eclipse.linuxtools.systemtapgui.structures.listeners.IGobblerListener;
import org.eclipse.ui.console.MessageConsoleStream;



/**
 * A class push data to a ScriptConsole.
 * @author Ryan Morse
 */
public class ConsoleStreamDaemon implements IGobblerListener {
	public ConsoleStreamDaemon(ScriptConsole console) {
		this.console = console;
		if(null != console)
			msgConsole = console.newMessageStream();
		disposed = false;
	}
	
	/**
	 * Prints out the new output data to the console
	 */
	protected void pushData() {
		if(null != msgConsole)
			msgConsole.print(output);
	}

	/**
	 * Captures data events and pushes the data to the console
	 */
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
			msgConsole = null;
		}
	}

	protected String output;
	protected ScriptConsole console;
	protected MessageConsoleStream msgConsole;
	private boolean disposed;
}
