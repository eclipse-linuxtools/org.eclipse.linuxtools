/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *     Red Hat Inc - Copied from LoggedCommand removed all functions defined
 *     in LoggedCommand2 plus some small modifications
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog;

import java.io.IOException;

import org.eclipse.linuxtools.systemtap.ui.structures.IPasswordPrompt;
import org.eclipse.linuxtools.systemtap.ui.structures.runnable.StreamGobbler;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;

public class LocalLoggedCommand extends LoggedCommand2 {

	private String[] cmd;
	private String[] envVars;
	private Process process;

	public LocalLoggedCommand(String[] cmd, String[] envVars,
			IPasswordPrompt prompt, int monitorDelay, String moduleName) {
		super(cmd, envVars, prompt, monitorDelay, moduleName);
		this.envVars = envVars;
		this.cmd = cmd;
	}

	/**
	 * Starts up the process that will execute the provided command and registers
	 * the <code>StreamGobblers</code> with their respective streams.
	 */
	@Override
	protected boolean init() {
		try {
			process = RuntimeProcessFactory.getFactory().exec(cmd, envVars, null);

			errorGobbler = new StreamGobbler(process.getErrorStream());            
			inputGobbler = new StreamGobbler(process.getInputStream());

			this.transferListeners();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * This method handles checking the status of the running <code>Process</code>. It
	 * is called when the new Thread is created, and thus should never be called by
	 * any implementing program. To run call the <code>start</code> method.
	 */
	@Override
	public void run() {
		errorGobbler.start();
		inputGobbler.start();
		try {
			process.waitFor();
		} catch (InterruptedException e) {} 
		stop();
	}

	@Override
	public synchronized void stop() {
		if(!stopped) {
			if(null != errorGobbler)
				errorGobbler.stop();
			if(null != inputGobbler)
				inputGobbler.stop();
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				// This thread was interrupted while waiting for
				// the process to exit. Destroy the process just
				// to make sure it exits.
				process.destroy();
			}
			stopped = true;
		}
	}
}
