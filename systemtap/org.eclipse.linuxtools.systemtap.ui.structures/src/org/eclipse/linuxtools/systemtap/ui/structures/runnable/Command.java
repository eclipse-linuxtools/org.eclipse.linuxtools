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


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.linuxtools.internal.systemtap.ui.structures.Localization;
import org.eclipse.linuxtools.systemtap.ui.structures.IPasswordPrompt;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IGobblerListener;

import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;



/**
 * A class to spawn a separate thread to run a <code>Process</code>.
 * @author Ryan Morse
 */
public class Command implements Runnable {
	/*
	 * Bug in the exec command prevents using a single string.  Forced
	 * to use a workaround in order to run commands with spaces.
	 * 
	 * http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4365120
	 * http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4109888
	 */

	/**
	 * Spawns the new thread that this class will run in.  From the Runnable
	 * interface spawning the new thread automatically calls the run() method.
	 * This must be called by the implementing class in order to start the
	 * StreamGobbler.
	 * @param cmd The entire command to run
	 * @param envVars List of all environment variables to use
	 * @param prompt The password promt for allowing the user to enter their password.
	 * @param monitorDelay The time in MS to wait between checking whether the <code>Process</code> has finished.
	 */
	public Command(String[] cmd, String[] envVars, IPasswordPrompt prompt, int monitorDelay) {
		this.cmd = cmd;
		this.envVars = envVars;
		this.prompt = prompt;
		this.monitorDelay = monitorDelay;
	}
	
	/**
	 * Spawns the new thread that this class will run in.  From the Runnable
	 * interface spawning the new thread automatically calls the run() method.
	 * This must be called by the implementing class in order to start the
	 * StreamGobbler.
	 * @param cmd The entire command to run
	 * @param envVars List of all environment variables to use
	 * @param prompt The password promt for allowing the user to enter their password.
	 */
	public Command(String[] cmd, String[] envVars, IPasswordPrompt prompt) {
		this(cmd, envVars, prompt, 100);
	}
	
	/**
	 * Starts the <code>Thread</code> that the new <code>Process</code> will run in.
	 * This must be called in order to get the process to start running.
	 */
	public void start() {
		if(init()) {
			Thread t = new Thread(this, cmd[0]);
			t.start();
		} else {
			stop();
			returnVal = Integer.MIN_VALUE;
		}
	}
	
	/**
	 * Starts up the process that will execute the provided command and registers
	 * the <code>StreamGobblers</code> with their respective streams.
	 */
	protected boolean init() {
		try {
			process = RuntimeProcessFactory.getFactory().exec(cmd, envVars, null);

			errorGobbler = new StreamGobbler(process.getErrorStream());            
			inputGobbler = new StreamGobbler(process.getInputStream());
			
			int i;
			for(i=0; i<inputListeners.size(); i++)
				inputGobbler.addDataListener(inputListeners.get(i));
			for(i=0; i<errorListeners.size(); i++)
				errorGobbler.addDataListener(errorListeners.get(i));
			return true;
		} catch(IOException ioe) {}
		return false;
	}
	
	/**
	 * This method handles checking the status of the running <code>Process</code>. It
	 * is called when the new Thread is created, and thus should never be called by
	 * any implementing program. To run call the <code>start</code> method.
	 */
	public void run() {
		errorGobbler.start();
		inputGobbler.start();
		
		try {
			while(!stopped) {
				try {
					if(null != errorGobbler && errorGobbler.readLine().endsWith(Localization.getString("Command.Password"))) {
						PrintWriter writer = new PrintWriter(process.getOutputStream(), true);
						writer.println(prompt.getPassword());
					}
					
					returnVal = process.exitValue();	//Dont care what the value is, just whether it throws an exception
					stop();	//Above line will throw an exception if not finished
				} catch(IllegalThreadStateException itse) {}
				
				if(0 < monitorDelay)
					Thread.sleep(monitorDelay);
			}
		} catch(InterruptedException ie) {
		} catch(NullPointerException npe) {}
	}
	
	/**
	 * Stops the process from running and stops the <code>StreamGobblers</code> from monitering
	 * the dead process.
	 */
	public synchronized void stop() {
		if(!stopped) {
			stopped = true;
			if(null != errorGobbler)
				errorGobbler.stop();
			if(null != inputGobbler)
				inputGobbler.stop();
			if(null != process)
				process.destroy();
		}
	}
	
	/**
	 * Method to check whether or not the process in running.
	 * @return The execution status.
	 */
	public boolean isRunning() {
		return !stopped;
	}
	
	/**
	 * Method to check if this class has already been disposed.
	 * @return Status of the class.
	 */
	public boolean isDisposed() {
		return disposed;
	}
	
	/**
	 * The return value of the process. 
	 * 2^231-1 if the process is still running.
	 * -2^231 if there was an error creating the process
	 * @return The return value generated from running the provided command.
	 */
	public int getReturnValue() {
		return returnVal;
	}

	/**
	 * Registers the provided <code>IGobblerListener</code> with the InputStream
	 * @param listener A listener to monitor the InputStream from the Process
	 */
	public void addInputStreamListener(IGobblerListener listener) {
		if(null != inputGobbler)
			inputGobbler.addDataListener(listener);
		else
			inputListeners.add(listener);
	}
	
	/**
	 * Registers the provided <code>IGobblerListener</code> with the ErrorStream
	 * @param listener A listener to monitor the ErrorStream from the Process
	 */
	public void addErrorStreamListener(IGobblerListener listener) {
		if(null != errorGobbler)
			errorGobbler.addDataListener(listener);
		else
			errorListeners.add(listener);
	}
	
	/**
	 * Returns the list of everything that is listening the the InputStream
	 * @return List of all <code>IGobblerListeners</code> that are monitoring the stream.
	 */
	
	public ArrayList<IGobblerListener> getInputStreamListeners() {
		if(null != inputGobbler)
			return inputListeners;
		else {
			ArrayList<IGobblerListener> dataListeners = inputGobbler.getDataListeners();
			return dataListeners;
		}
	}
	
	/**
	 * Returns the list of everything that is listening the the ErrorStream
	 * @return List of all <code>IGobblerListeners</code> that are monitoring the stream.
	 */
	
	public ArrayList<IGobblerListener> getErrorStreamListeners() {
		if(null != errorGobbler)
			return errorListeners;
		else
			return errorGobbler.getDataListeners();
	}
	
	/**
	 * Removes the provided listener from those monitoring the InputStream.
	 * @param listener An </code>IGobblerListener</code> that is monitoring the stream.
	 */
	public void removeInputStreamListener(IGobblerListener listener) {
		if(null != inputGobbler)
			inputGobbler.removeDataListener(listener);
		else
			inputListeners.remove(listener);
	}
	
	/**
	 * Removes the provided listener from those monitoring the ErrorStream.
	 * @param listener An </code>IGobblerListener</code> that is monitoring the stream.
	 */
	public void removeErrorStreamListener(IGobblerListener listener) {
		if(null != errorGobbler)
			errorGobbler.removeDataListener(listener);
		else
			errorListeners.remove(listener);
	}
	
	/**
	 * Disposes of all internal components of this class. Nothing in the class should be
	 * referenced after this is called.
	 */
	public void dispose() {
		if(!disposed) {
			stop();
			disposed = true;
			inputListeners = null;
			errorListeners = null;

			if(null != inputGobbler)
				inputGobbler.dispose();
			inputGobbler = null;
			
			if(null != errorGobbler)
				errorGobbler.dispose();
			errorGobbler = null;
		}
	}
	
	private boolean stopped = false;
	private boolean disposed = false;
	private StreamGobbler inputGobbler = null;
	private StreamGobbler errorGobbler = null;
	private ArrayList<IGobblerListener> inputListeners = new ArrayList<IGobblerListener>();	//Only used to allow adding listeners before creating the StreamGobbler
	private ArrayList<IGobblerListener> errorListeners = new ArrayList<IGobblerListener>();	//Only used to allow adding listeners before creating the StreamGobbler
	private int returnVal = Integer.MAX_VALUE;

	private String[] cmd;
	private String[] envVars;
	private IPasswordPrompt prompt;
	private int monitorDelay;
	protected Process process;
	
	public static final int ERROR_STREAM = 0;
	public static final int INPUT_STREAM = 1;
}