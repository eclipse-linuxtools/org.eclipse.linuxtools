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

package org.eclipse.linuxtools.systemtap.ui.structures.runnable;

import java.io.File;

import org.eclipse.linuxtools.systemtap.ui.structures.LoggingStreamDaemon;



/**
 * A class to spawn a separate thread to run a <code>Process</code> and to automatically
 * log everything using the <code>LoggingStreamDaemon</code>.
 * @author Ryan Morse
 */
public class LoggedCommand extends Command {

	/**
	 * Spawns the new thread that this class will run in.  From the Runnable
	 * interface spawning the new thread automatically calls the run() method.
	 * This must be called by the implementing class in order to start the
	 * StreamGobbler.
	 * @param cmd The entire command to run
	 * @param envVars List of all environment variables to use
	 * @param prompt The password prompt for allowing the user to enter their password.
	 * @param monitorDelay the time between checking to see if the process finished
	 * @since 2.0
	 */
	public LoggedCommand(String[] cmd, String[] envVars) {
		super(cmd, envVars);
		logger = new LoggingStreamDaemon();
		addInputStreamListener(logger);
	}

	/**
	 * Gets all of the output from the input stream.
	 * @return String containing the entire output from the input stream.
	 */
	public String getOutput() {
		if(!isDisposed())
			return logger.getOutput();
		else
			return null;
	}

	/**
	 * Saves the input stream data to a permanent file.  Any new data on the
	 * stream will automatically be saved to the file.
	 * @param file The file to save the InputStream to.
	 */
	public boolean saveLog(File file) {
		return logger.saveLog(file);
	}

	/**
	 * Stops the process from running and unregisters the StreamListener
	 */
	@Override
	public synchronized void stop() {
		if(isRunning()) {
			super.stop();
			removeInputStreamListener(logger);
		}
	}

	/**
	 * Disposes of all internal references in this class.  Nothing should be called
	 * after dispose.
	 */
	@Override
	public void dispose() {
		if(!isDisposed()) {
			super.dispose();	//Do this first to ensure logger reads everything possible
	    	logger.dispose();
		}
	}

	private LoggingStreamDaemon logger;
}
