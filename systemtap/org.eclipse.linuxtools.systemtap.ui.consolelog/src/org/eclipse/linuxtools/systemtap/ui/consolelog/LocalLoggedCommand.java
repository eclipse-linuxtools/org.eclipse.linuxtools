/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - Copied from LoggedCommand and modified
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
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
	protected IStatus init() {
		try {
			process = RuntimeProcessFactory.getFactory().exec(cmd, envVars, null);

			errorGobbler = new StreamGobbler(process.getErrorStream());            
			inputGobbler = new StreamGobbler(process.getInputStream());

			this.transferListeners();
			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, ConsoleLogPlugin.PLUGIN_ID, e.getMessage(), e);
		}
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
