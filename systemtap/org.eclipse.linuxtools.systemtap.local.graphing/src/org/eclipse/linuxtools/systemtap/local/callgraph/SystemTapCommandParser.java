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

package org.eclipse.linuxtools.systemtap.local.callgraph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.systemtap.local.core.Messages;


public class SystemTapCommandParser extends Job {

	private String filePath;
	private String returnText;
	private boolean printIsDone;
	public CallgraphView stapview;
	private String testOutput;
	public boolean useColours;
	private boolean graphingMode;
	private boolean processFinished;


	public boolean isProcessFinished() {
		return processFinished;
	}


	public void setProcessFinished(boolean processFinished) {
		this.processFinished = processFinished;
	}


	public SystemTapCommandParser(String name, String filePath, CallgraphView sview, 
			boolean useColours,
			boolean scheduleGraph, String configName) {
		super(name);
		this.filePath = filePath;
		this.stapview = sview;
		this.useColours = useColours;
		this.graphingMode = scheduleGraph;
		this.processFinished=false;
	}

	
	public String getCommand() {
		return filePath;
	}

	/**
	 * Convenience method to set this.filePath.
	 * Currently not used.
	 * 
	 * @param filePath - filePath to be set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
			if (graphingMode) {
				//Delegate to graphing parser instead of this one
				StapGraphParser p = new StapGraphParser(Messages.getString("SystemTapCommandParser.0"), filePath); //$NON-NLS-1$
				p.schedule();
				
//				String text = Helper.getMainConsoleTextByName(configName);
//				returnText = "       " + configName+"\n"; //$NON-NLS-1$ //$NON-NLS-2$
//				returnText += dashes() + "\n\n"; //$NON-NLS-1$
//				setText(text);
//
//				
//				SystemTapUIJob uijob = new SystemTapUIJob("SystemTapUIJob", this, this.useColours); //$NON-NLS-1$
//				uijob.schedule();
			
				return Status.OK_STATUS;
			}

		return Status.OK_STATUS;
	}

	public synchronized void setText(String text) {
		returnText += text;
	}

	public synchronized String getText() {
		String tmp = returnText;
		returnText = ""; //$NON-NLS-1$
		return tmp;
	}

	public synchronized void setDone() {
		printIsDone = true;
	}
	
	public synchronized void clearDone() {
		printIsDone = false;
	}
	
	public synchronized boolean checkDone() {
		return printIsDone;
	}
	
	public String getTestOutput() {
		return testOutput;
	}
	
	public IStatus testRun(IProgressMonitor m) {
		return run(m);
	}

	
}
