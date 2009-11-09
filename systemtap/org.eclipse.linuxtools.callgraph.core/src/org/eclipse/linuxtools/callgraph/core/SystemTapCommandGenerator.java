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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


/**
 * This <code>Action</code> is used to run a SystemTap script that is currently open in the editor.
 * @author Ryan Morse
 */
public class SystemTapCommandGenerator extends Action implements IWorkbenchWindowActionDelegate {	
	
	private boolean needsToSendCommand;
	private boolean needsArguments;
	protected String arguments;
	protected String scriptPath;
	protected String commands;
	protected boolean isGuru;
	private String binaryPath = null;
	protected IWorkbenchWindow actionWindow = null;
	private IAction act;
	private String executeCommand;
	private String binaryArguments;

	
	public SystemTapCommandGenerator() {		
		super();
	}

	public void dispose() {
		actionWindow= null;
	}

	public void init(IWorkbenchWindow window) {
		actionWindow= window;
	}

	public void run(IAction action) {
		System.out.println("Not implemented"); //$NON-NLS-1$
	}

	public void run() {
		System.out.println("Calling run() without parameters not implemented"); //$NON-NLS-1$
	}
	
	public String generateCommand(String scrPath, String binPath, String cmds, boolean needBinary, boolean needsArgs, String arg, String binArguments) {
		needsToSendCommand = needBinary;
		needsArguments = needsArgs;
		binaryPath = binPath;
		scriptPath = scrPath;
		isGuru = false;
		arguments = arg;
		commands = cmds;
		binaryArguments = binArguments;
		
		String[] script = buildScript();
		
		String cmd = ""; //$NON-NLS-1$
		for (int i = 0; i < script.length-1; i++)
			cmd = cmd + script[i] + " "; //$NON-NLS-1$
		cmd = cmd + script[script.length-1];

		this.executeCommand = cmd;
		return cmd;
	}
	

	/**
	 * Parses the data created from generateCommand
	 * @return An array of strings to be joined and executed by the shell
	 */
	protected String[] buildScript() {
		//TODO: Take care of this in the next release. For now only the guru mode is sent
		ArrayList<String> cmdList = new ArrayList<String>();
		String[] script;

		//getImportedTapsets(cmdList);
		if (commands.length() > 0){
			cmdList.add(commands);	
		}
		
		//Execute a binary
		if (needsToSendCommand){
			if (binaryArguments.length() < 1){	
				cmdList.add("-c '" + binaryPath + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}else{				
				cmdList.add("-c '" + binaryPath + " " + binaryArguments +"'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
		
		if (needsArguments) {
			script = new String[cmdList.size() + 3];
			script[script.length-2] = scriptPath;
			script[script.length-1] = arguments; 
		} else {
			script = new String[cmdList.size() + 2];
			script[script.length-1] = scriptPath;
		}
		
		script[0] = PluginConstants.STAP_PATH; //$NON-NLS-1$

		for(int i=0; i< cmdList.size(); i++) {
			if (cmdList.get(i) != null)
				script[i +1] = cmdList.get(i).toString();
			else script[i + 1] = ""; //$NON-NLS-1$
		}
		return script;
		
	}

	
	public void selectionChanged(IAction act, ISelection select) {
		this.act = act;
		setEnablement(false);
		//buildEnablementChecks();
	}

	private void setEnablement(boolean enabled) {
		act.setEnabled(enabled);
	}
	
	public String getExecuteCommand(){
		return this.executeCommand;
	}

	
	/**
	 * Convenience method to return the current window
	 */
	public IWorkbenchWindow getWindow() {
		return actionWindow;
	}
	
}
