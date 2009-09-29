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
import org.eclipse.core.runtime.jobs.Job;

public abstract class SystemTapParser extends Job {
	protected IProgressMonitor monitor;
	protected String filePath;
	protected String viewID;
	 

	public SystemTapParser() {
		super("New_Job"); //$NON-NLS-1$
		this.filePath = PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH;
		initialize();
		setViewID();
	}

	/**
	 * Initialize will be called in the constructors for this class. Use this
	 * method to initialize variables.
	 */
	protected abstract void initialize();
	
	/**
	 * Set the viewID to use for this parser -- see the callgraph.core view extension point.
	 * <br>
	 * This method should be of the form viewID = view_id_string, where view_id_string is
	 * some constant defined by your parser. 
	 */
	protected abstract void setViewID();

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

	public abstract void saveData(String filePath);

	public SystemTapParser(String name, String filePath) {
		super(name);
		// BY DEFAULT READ/WRITE FROM HERE
		if (filePath != null)
			this.filePath = filePath;
		else
			this.filePath = PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH;
		initialize();
		setViewID();
	}

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
		this.monitor = monitor;
		IStatus returnStatus = executeParsing();
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
	 * Returns the monitor
	 * 
	 * @return
	 */
	public IProgressMonitor getMonitor() {
		return monitor;
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

	public void launchFileDialogError() {
		SystemTapUIErrorMessages err = new SystemTapUIErrorMessages(Messages
				.getString("SystemTapParser.2"), //$NON-NLS-1$
				Messages.getString("SystemTapParser.3"), //$NON-NLS-1$
				Messages.getString("SystemTapParser.4") + filePath + //$NON-NLS-1$
						Messages.getString("SystemTapParser.5")); //$NON-NLS-1$
		err.schedule();
	}

	/**
	 * Sets the file to read from
	 * 
	 * @param filePath
	 */
	public void setFile(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Gets the file to read from
	 * 
	 * @return
	 */
	public String getFile() {
		return filePath;
	}

}
