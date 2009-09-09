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

package org.eclipse.linuxtools.systemtap.localgui.graphing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.systemtap.local.core.Messages;


public class SystemTapCommandParser extends Job {

	private String filePath;
	private String returnText;
	private boolean printIsDone;
	private boolean testMode;
	public SystemTapView stapview;
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


	public SystemTapCommandParser(String name, String filePath, SystemTapView sview, 
			boolean useColours,
			boolean scheduleGraph, String configName) {
		super(name);
		this.filePath = filePath;
		this.stapview = sview;
		this.testMode = false;			//By default, do not operate in testing mode
		this.useColours = useColours;
		this.graphingMode = scheduleGraph;
		this.processFinished=false;
	}

	public void setTestingMode() {
		testMode = true;
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


	private String dashes() {
		String dash = ""; //$NON-NLS-1$if (b)
		for (int i = 0; i < 20; i++)
			dash += "-"; //$NON-NLS-1$
		return dash;
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
			
			printIsDone = true;				//Used to synchronize UIjobs and avoid 
			//running many useless UIjobs simultaneously
			returnText = ""; //$NON-NLS-1$
			// SEND THE COMMAND
			try {
				FileInputStream readFile = new FileInputStream(filePath);
				BufferedReader buf = new BufferedReader(new InputStreamReader(readFile));				
				String line = ""; //$NON-NLS-1$
				
				if (monitor.isCanceled()) {
					buf.close();
					File f = new File(filePath);
					f.delete();
					return Status.CANCEL_STATUS;
				}


				testOutput = ""; //$NON-NLS-1$
				
				returnText += Messages.getString("SystemTapCommand.1"); //$NON-NLS-1$
				returnText += "\n\n"; //$NON-NLS-1$
				returnText += dashes();
				returnText += "\n"; //$NON-NLS-1$

				clearDone();
				SystemTapUIJob uijob = new SystemTapUIJob("SystemTapUIJob", this, this.useColours); //$NON-NLS-1$
				uijob.schedule();

				while (processFinished == false) {
						while ((line = buf.readLine()) != null) {
							if (monitor.isCanceled()) {
								buf.close();
								File f = new File(filePath);
								f.delete();
								return Status.CANCEL_STATUS;
							}
							
						/*	if (errorLine.contains("stapdev") && errorLine.contains("stapusr")) {//$NON-NLS-1$ 
								Syste				mTapUIErrorMessages message = new SystemTapUIErrorMessages("staperror", 
										"Insufficient privileges", "It seems you don't have enough privileges " +
												"to run SystemTap on this system. Please run the install.sh script " +
												"before continuing.");
								message.schedule();
							}
							
							if (errorLine.contains("___STAP_MARKER___")) {
								SystemTapUIErrorMessages message = new SystemTapUIErrorMessages("staperror", 
										"No markers detected", "No systemtap markers were found in the " +
												"selected program. Please execute using the parse_function_nomark.stp" +
												" script instead. (If you are using the launch shortcut, just click" +
												" cancel when prompted to use stap markers)");
								message.schedule();
							}
						*/
								
							
							
							if (line != null) {
								if (line.length() > 0) {
									if (testMode)
										testOutput += line;
									setText(line + "\n"); //$NON-NLS-1$
	
									if (checkDone()) {
										clearDone();
										uijob.schedule();
									}
	
								}
							}
	
					} 
				}
					setText("\n\n   Terminating SystemTap script"); //$NON-NLS-1$
					
					//Schedule one more just in case
					uijob.schedule(); 
					buf.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			File f = new File(filePath);
			f.delete();
			

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
