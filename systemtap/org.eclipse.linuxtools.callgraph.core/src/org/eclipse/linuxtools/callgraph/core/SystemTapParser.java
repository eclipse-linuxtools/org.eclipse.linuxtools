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
package org.eclipse.linuxtools.callgraph.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

public abstract class SystemTapParser extends Job {
	protected IProgressMonitor monitor;
	protected String filePath;
	protected String viewID;
	protected SystemTapView view;
	protected boolean realTime = false;
	protected Object data;

	public boolean isDone;
	public StringBuffer text;

	public SystemTapParser() {
		super("New_SystemTapParser_Job"); //$NON-NLS-1$
		this.filePath = PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH;
		this.viewID = null;
		initialize();
		isDone = false;
	}
	

	public SystemTapParser(String name, String filePath) {
		super(name);
		// BY DEFAULT READ/WRITE FROM HERE
		if (filePath != null)
			this.filePath = filePath;
		else
			this.filePath = PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH;
		this.viewID = null;
		initialize();
	}



	/**
	 * Initialize will be called in the constructors for this class. Use this
	 * method to initialize variables.
	 */
	protected abstract void initialize();
	
	/**
	 * Called at the end of a non-realtime run. 
	 * Implement this method if using non-realtime functions.
	 * The setFinalData method will be called after executeParsing() is run. The getFinalData() method
	 * will be used by the SystemTapView to get the final data associated with this parser.
	 * <br><br>
	 * Alternatively, you can cast the parser within SystemTapView to your own parser class and access
	 * its data structures that way. 
	 */
	protected abstract void setData();

	/**
	 * Implement this method to execute parsing. The return from
	 * executeParsing() will be the return value of the run command.
	 * 
	 * SystemTapParser will call executeParsing() within its run method. (i.e.
	 * will execute in a separate, non-UI thread)
	 * 
	 * @return
	 */
	public abstract IStatus executeParsing();

	/**
	 * Implement this method to save data in whichever format your program
	 * needs. Keep in mind that the filePath variable should contain the
	 * filePath of the most recently opened file.
	 * 
	 * @param filePath
	 */
	public abstract void saveData(String targetFile);
	
	
	
	/**
	 * Implement this method if your parser is to execute in realtime. This
	 * will form the body of a run method executed in a separate UIJob. To stop
	 * real time parsing, return Status.CANCEL_STATUS.
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
				Messages.getString("SystemTapParser.0"), Messages.getString("SystemTapParser.1"), message); //$NON-NLS-1$ //$NON-NLS-2$
		mess.schedule();
	}

	/**
	 * Specify what to do after executeParsing is run
	 */
	protected void postProcessing() {
		// Create a UIJob to handle the rest
		GraphUIJob uijob = new GraphUIJob(Messages
				.getString("StapGraphParser.5"), this, viewID); //$NON-NLS-1$
		uijob.schedule();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Generate real-time job
		if (realTime && viewID != null) {
			try {
				if (realTime) {
					GraphUIJob job = new GraphUIJob("RealTimeUIJob", this,
							this.viewID);
					job.schedule();
					job.join();
					view = job.getViewer();
				}
				RunTimeJob job = new RunTimeJob("RealTimeParser");
				job.schedule();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}

		IStatus returnStatus = executeParsing();
		setData();
		postProcessing();
		return returnStatus;
	}
	
	public void printArrayListMap(HashMap<Integer, ArrayList<Integer>> blah) {
		int amt = 0;
		for (int a : blah.keySet()) {
			amt++;
			MP.print(a + " ::> "); //$NON-NLS-1$
			for (int c : blah.get(a)) {
				System.out.print(c + " "); //$NON-NLS-1$
			}
			MP.println(""); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("unchecked")
	public void printMap(Map blah) {
		int amt = 0;
		for (Object a : blah.keySet()) {
			amt++;
			MP.println(a + " ::> " + blah.get(a)); //$NON-NLS-1$
		}
	}


	/**
	 * For easier JUnit testing only. Allows public access to run method without
	 * scheduling an extra job.
	 * 
	 * @param m
	 * @return
	 */
	public IStatus testRun(IProgressMonitor m) {
		return run(m);
	}

	public void launchFileErrorDialog() {
		SystemTapUIErrorMessages err = new SystemTapUIErrorMessages(Messages
				.getString("SystemTapParser.2"), //$NON-NLS-1$
				Messages.getString("SystemTapParser.3"), //$NON-NLS-1$
				Messages.getString("SystemTapParser.4") + filePath + //$NON-NLS-1$
						Messages.getString("SystemTapParser.5")); //$NON-NLS-1$
		err.schedule();
	}



	
	private class RunTimeJob extends Job {
		public RunTimeJob(String name) {
			super(name);
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			return realTimeParsing();
		}
		
	}
	
	/**
	 * Return the Data object.
	 * @return
	 */
	public Object getData() {
		return data;
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
		return filePath;
	}
	
	
	/**
	 * Sets the file to read from
	 * 
	 * @param filePath
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	

	public void setDone(boolean val) {
		isDone = val;
	}

	
	public void setMonitor(IProgressMonitor m) {
		this.monitor = m;
	}
	
	/**
	 * Set whether or not this parser runs in real time. If viewID has already
	 * been set, this will also attempt to open the view.
	 * 
	 * @throws InterruptedException
	 */
	public void setRealTime(boolean val) throws InterruptedException {
		realTime = val;

	}
	
	/**
	 * Set the viewID to use for this parser -- see the callgraph.core view
	 * extension point. If realTime is set to true, this will also attempt to
	 * open the view.
	 * 
	 * @throws InterruptedException
	 */
	public void setViewID(String value) throws InterruptedException {
		viewID = value;
	}

}
