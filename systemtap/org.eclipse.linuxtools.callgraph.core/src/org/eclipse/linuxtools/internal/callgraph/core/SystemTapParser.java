/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class SystemTapParser extends Job {
	protected IProgressMonitor monitor;
	protected String sourcePath;
	protected String viewID;
	protected SystemTapView view;
	protected boolean realTime = false;
	protected Object data;
	protected Object internalData;
	private String secondaryID = ""; //$NON-NLS-1$

	public boolean done;

	public SystemTapParser() {
		super("Parsing data"); //$NON-NLS-1$
		this.sourcePath = PluginConstants.getDefaultIOPath();
		this.viewID = null;
		initialize();
		done = false;

		//PURELY FOR TESTING
		if (monitor == null){
			monitor = new NullProgressMonitor();
		}
	}


	public SystemTapParser(String name, String filePath) {
		super(name);
		// BY DEFAULT READ/WRITE FROM HERE
		if (filePath != null) {
			this.sourcePath = filePath;
		} else {
			this.sourcePath = PluginConstants.getDefaultIOPath();
		}
		this.viewID = null;
		initialize();
	}


	/**
	 * Initialize will be called in the constructors for this class. Use this
	 * method to initialize variables.
	 */
	protected abstract void initialize();


	/**
	 * Implement this method to execute parsing. The return from
	 * executeParsing() will be the return value of the run command.
	 *
	 * SystemTapParser will call executeParsing() within its run method. (i.e.
	 * will execute in a separate, non-UI thread)
	 *
	 * @return
	 */
	public abstract IStatus nonRealTimeParsing();


	/**
	 * Implement this method if your parser is to execute in realtime. This method
	 * will be called as part of a while loop in a separate Job. Use the setInternalData
	 * method to initialize some data object for use in realTimeParsing. The default
	 * setInternalMethod method will set internalData to a BufferedReader
	 * <br> <br>
	 * After the isDone flag is set to true, the realTimeParsing() method will
	 * be run one more time to catch any stragglers.
	 */
	public abstract IStatus realTimeParsing();


	/**
	 * Cleans names of form 'name").return', returning just the name
	 *
	 * @param name
	 */
	protected String cleanFunctionName(String name) {
		return name.split("\"")[0]; //$NON-NLS-1$
	}

	/**
	 * Checks for quotations and brackets in the function name
	 *
	 * @param name
	 */
	protected boolean isFunctionNameClean(String name) {
		if (name.contains("\"") || name.contains(")")) //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		return true;
	}

	/**
	 * Creates a popup error dialog in a separate UI thread. Dialog title is
	 * 'Unexpected symbol,' name is 'ParseError' and body is the specified
	 * message.
	 *
	 * @param message
	 */
	protected void parsingError(String message) {
		SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
				Messages.getString("SystemTapParser.ParseErr"), //$NON-NLS-1$
				Messages.getString("SystemTapParser.ErrSymbol"), //$NON-NLS-1$
				message);
		mess.schedule();
	}

	/**
	 * Load the specified viewID by creating a StapUIJob. Does not return until the StapUIJob has.
	 * Returns true if the makeView was successful, false otherwise.
	 */
	protected boolean makeView() {
		// Create a UIJob to handle the rest
		if (viewID != null && viewID.length() > 0) {
			try {
				StapUIJob uijob = new StapUIJob(
						Messages.getString("StapGraphParser.JobName"), this, viewID); //$NON-NLS-1$
				uijob.schedule();
				uijob.join();
				view = uijob.getViewer();
				return true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Generate real-time job
		IStatus returnStatus = Status.CANCEL_STATUS;
		this.monitor = monitor;
		if (this.monitor == null) {
			this.monitor = new NullProgressMonitor();
		}

		makeView();
		if (realTime) {
        	try {
				setInternalData();
	            while (!done){
	            	returnStatus = realTimeParsing();
	            	if (monitor.isCanceled() || returnStatus == Status.CANCEL_STATUS) {
	            		done = true;
	            		return Status.CANCEL_STATUS;
	            	}

	            	Thread.sleep(500);
	            }
	            if (!monitor.isCanceled()) returnStatus = realTimeParsing();
	            done = true;
				return returnStatus;
        	} catch (InterruptedException e) {
        		SystemTapUIErrorMessages m = new SystemTapUIErrorMessages(
        				Messages.getString("SystemTapParser.InternalData"), //$NON-NLS-1$
        				Messages.getString("SystemTapParser.FailedToSetData"), //$NON-NLS-1$
        				Messages.getString("SystemTapParser.FailedToSetDataMessage")); //$NON-NLS-1$
        		m.schedule();
        		return Status.CANCEL_STATUS;
			} catch (FileNotFoundException e) {
        		SystemTapUIErrorMessages m = new SystemTapUIErrorMessages(
        				Messages.getString("SystemTapParser.InternalData"), //$NON-NLS-1$
        				Messages.getString("SystemTapParser.FailedToSetData"), //$NON-NLS-1$
        				Messages.getString("SystemTapParser.FailedToSetDataMessage")); //$NON-NLS-1$
        		m.schedule();
        		return Status.CANCEL_STATUS;
			}
		} else {
			returnStatus = nonRealTimeParsing();
			if (!returnStatus.isOK()){
				return returnStatus;
			}

			setData(this);
			return returnStatus;
		}

	}

	/**
	 * For easier JUnit testing only. Allows public access to run method without
	 * scheduling an extra job.
	 *
	 * @param m
	 * @return
	 */
	public IStatus testRun(IProgressMonitor m, boolean realTime) {
		try {
			internalData = new BufferedReader(new FileReader(new File(
					sourcePath)));
			if (realTime) {
				return realTimeParsing();
			} else {
				return nonRealTimeParsing();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return Status.CANCEL_STATUS;
	}


	public void launchFileErrorDialog() {
		SystemTapUIErrorMessages err = new SystemTapUIErrorMessages(Messages
				.getString("SystemTapParser.InvalidFile"), //$NON-NLS-1$
				Messages.getString("SystemTapParser.InvalidFile"), //$NON-NLS-1$
				Messages.getString("SystemTapParser.InvalidFileMsg1") + sourcePath + //$NON-NLS-1$
						Messages.getString("SystemTapParser.InvalidFileMsg2")); //$NON-NLS-1$
		err.schedule();
	}


	/**
	 * @return the Data object
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @return the internal data object
	 */
	public Object getInternalData(){
		return internalData;
	}


	/**
	 * Generic method for setting the internalData object. This will be called
	 * by a real-time-parser immediately before its main polling loop. By default,
	 * this method will attempt to create a bufferedReader around File(filePath)
	 * @throws FileNotFoundException
	 */
	protected void setInternalData() throws FileNotFoundException {
		File file = new File(sourcePath);
		internalData = new BufferedReader(new FileReader(file));
	}


	/**
	 * Returns the monitor
	 *
	 * @return
	 */
	public IProgressMonitor getMonitor() {
		return monitor;
	}


	/**
	 * Gets the file to read from
	 *
	 * @return
	 */
	public String getFile() {
		return sourcePath;
	}

	/**
	 * Sets the file to read from
	 *
	 * @param source
	 */
	public void setSourcePath(String source) {
		this.sourcePath = source;
	}

	/**
	 * Will terminate the parser at the next opportunity (~once every 0.5s)s
	 *
	 * @param val
	 */
	public void setDone(boolean val) {
		done = val;
	}

	public void setMonitor(IProgressMonitor m) {
		this.monitor = m;
	}

	/**
	 * Set whether or not this parser runs in real time. If viewID has already
	 * been set, this will also attempt to open the view.
	 */
	public void setRealTime(boolean val) {
		realTime = val;

	}

	/**
	 * Set the viewID to use for this parser -- see the callgraph.core view
	 * extension point. If realTime is set to true, this will also attempt to
	 * open the view.
	 */
	public void setViewID(String value) {
		viewID = value;
	}

	/**
	 * Called at the end of a non-realtime run.
	 * Feel free to override this method if using non-realtime functions.
	 * The setData method will be called after executeParsing() is run.
	 * The getData() method will be used by the SystemTapView to get the
	 * data associated with this parser.
	 * <br><br>
	 * Alternatively, you can cast the parser within SystemTapView to your
	 * own parser class and access its data structures that way.
	 */
	public void setData(Object obj) {
		data = obj;
	}

	/**
	 * Sends a message to cancel the job. Job may not terminate immediately.
	 */
	public void cancelJob() {
		done = true;
	}

	public boolean isDone() {
		return done;
	}

	public void setKillButtonEnabled(boolean val) {
		if (view != null) {
			view.setKillButtonEnabled(val);
		}
	}

	public boolean isRealTime() {
		return realTime;
	}

	public void setSecondaryID(String secondaryID) {
		this.secondaryID = secondaryID;
	}

	public String getSecondaryID() {
		return secondaryID;
	}
}
