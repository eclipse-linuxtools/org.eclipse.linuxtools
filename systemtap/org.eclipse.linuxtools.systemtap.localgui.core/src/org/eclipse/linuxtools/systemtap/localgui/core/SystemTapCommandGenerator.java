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

package org.eclipse.linuxtools.systemtap.localgui.core;

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
	
	/**
	 * The launch point of this class. Executes the script using the given parameters.
	 * Call this from another class to execute scripts. 
	 * 
	 * The only mandatory String is scrPath, all other Strings could be null. Be
	 * sure to match the need____ boolean to its corresponding String. I.E. if 
	 * binPath is blank, set needBinary to false.
	 * 
	 */
	
	/*public void run(String scrPath, String binPath, String cmds, boolean needBinary, boolean needsArgs, String arg, boolean useColour) {
		needsToSendCommand = needBinary;
		needsArguments = needsArgs;
		binaryPath = binPath;
		scriptPath = scrPath;
		isGuru = false;
		arguments = arg;
		commands = cmds;
		this.useColour = useColour;
		
		completeRun();
	}*/
	
	public String generateCommand(String scrPath, String binPath, String cmds, boolean needBinary, boolean needsArgs, String arg, boolean useColour) {
		needsToSendCommand = needBinary;
		needsArguments = needsArgs;
		binaryPath = binPath;
		scriptPath = scrPath;
		isGuru = false;
		arguments = arg;
		commands = cmds;
		
		String[] script = buildScript();
		
		String cmd = ""; //$NON-NLS-1$
		for (int i = 0; i < script.length-1; i++)
			cmd = cmd + script[i] + " "; //$NON-NLS-1$
		cmd = cmd + script[script.length-1];

		this.executeCommand = cmd;
		return cmd;
	}
	
	
	
	/**
	 * Finish the run process. Separated from run(lots of arguments) in case any future
	 * implementations of run are created. Needs the protected variables to be set.
	 */
	/*public void completeRun() {
		//System.out.println("Running from completeRun() in RunSystemTapAction"); //$NON-NLS-1$
				
		String[] script = buildScript();
		
				
		String cmd = ""; //$NON-NLS-1$
		for (int i = 0; i < script.length-1; i++)
			cmd = cmd + script[i] + " "; //$NON-NLS-1$
		cmd = cmd + script[script.length-1];

		// RUN THE COMMAND
		this.executeCommand = cmd;
		SystemTapCommandParser stapcmd = new SystemTapCommandParser(Messages.getString("RunSystemTapAction.0"), cmd, stapview, useColour, false); //$NON-NLS-1$
		stapcmd.schedule();
		
	}*/
	

	/**
	 * Called by the constructor of this class
	 * @return An array of strings to be executed by the shell
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
		if (needsToSendCommand)
			cmdList.add("-c '" + binaryPath + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		if (needsArguments) {
			script = new String[cmdList.size() + 3];
			script[script.length-2] = scriptPath;
			script[script.length-1] = arguments; 
		} else {
			script = new String[cmdList.size() + 2];
			script[script.length-1] = scriptPath;
		}
		
		//TODO: Make this based on the install.sh file
		script[0] = PluginConstants.STAP_PATH; //$NON-NLS-1$

		for(int i=0; i< cmdList.size(); i++) {
			if (cmdList.get(i) != null)
				script[i +1] = cmdList.get(i).toString();
			else script[i + 1] = ""; //$NON-NLS-1$
				
		}
		return script;
		
	}
	
	/**
	 * The command line argument generation method used by <code>RunScriptAction</code>. This generates
	 * a stap command line that includes the tapsets specified in user preferences, a guru mode flag
	 * if necessary, and the path to the script on disk.
	 * @return The command to invoke to start the script running in stap.
	 */
	/*protected String[] buildStandardScript() {
	//TODO: Take care of this in the next release. For now only the guru mode is sent
		ArrayList<String> cmdList = new ArrayList<String>();
		String[] script;

		//getImportedTapsets(cmdList);
		if (commands.length() > 0){
			cmdList.add(commands);	
		}
		
		//Execute a binary
		if (needsToSendCommand)
			cmdList.add("-c '" + binaryPath + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		
		script = finalizeScript(cmdList);
		
		return script;
	}*/

	/**
	 * Produces a <code>String[]</code> from the <code>ArrayList</code> passed in with stap inserted
	 * as the first entry, and the filename as the last entry. Used to convert the arguments generated
	 * earlier in <code>buildStandardScript</code> such as tapset locations and guru mode into an actual
	 * command line argument array that can be passed to <code>Runtime.exec</code>.
	 * @param cmdList The list of arguments for stap for this scriptprest
	 * @return An array suitable to pass to <code>Runtime.exec</code> to start stap on this file.
	 */
	/*protected String[] finalizeScript(ArrayList<String> cmdList) {
		String[] script;

		if (needsArguments) {
			script = new String[cmdList.size() + 3];
			script[script.length-2] = scriptPath;
			script[script.length-1] = arguments; 
		} 
		else {
			script = new String[cmdList.size() + 2];
			script[script.length-1] = scriptPath;
		}
		
		//TODO: Make this based on the install.sh file
		script[0] = PluginConstants.STAP_PATH; //$NON-NLS-1$

		for(int i=0; i< cmdList.size(); i++) {
			if (cmdList.get(i) != null)
				script[i +1] = cmdList.get(i).toString();
			else script[i + 1] = ""; //$NON-NLS-1$
				
		}
		return script;
	}*/

	
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


/**
 * Returns the path of the current editor in the window this action is associated with.
 * @return The string representation of the path of the current file.
 */
//protected String getFilePath() {
//	return ((IPathEditorInput)ed.getEditorInput()).getPath().toString();
//}


/**
 * Checks if the current editor is operating on a file that actually exists and can be 
 * used as an argument to stap (as opposed to an unsaved buffer).
 * @return True if the file is valid.
 */
//protected boolean isValid() {
//	IEditorPart ed = fWindow.getActivePage().getActiveEditor();
//	if (ed == null) return true;
//	if(isValidFile(ed)){
//		
//		String ret = getFilePath();
//		
//		if(isValidDirectory(ret))
//			return true;
//	}
//	return true;
//}

//private boolean isValidFile(IEditorPart editor) {
//	if(null == editor) {
//		String msg = MessageFormat.format("No script file is selected", (Object[])null);
//		//LogManager.logInfo("Initializing", MessageDialog.class);
//		MessageDialog.openWarning(fWindow.getShell(), "Problem running SystemTap script - invalid script", msg);
//		//LogManager.logInfo("Disposing", MessageDialog.class);
//		return false;
//	}
//	
//	if(editor.isDirty())
//		editor.doSave(new ProgressMonitorPart(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new FillLayout()));
//	
//	return true;
//}


/*private boolean isValidDirectory(String fileName) {
	this.fileName = fileName;
	
	if(0 == IDESessionSettings.tapsetLocation.trim().length())
		TapsetLibrary.getTapsetLocation(IDEPlugin.getDefault().getPreferenceStore());
	if(fileName.contains(IDESessionSettings.tapsetLocation)) {
		String msg = MessageFormat.format(Localization.getString("RunScriptAction.TapsetDirectoryRun"), (Object[])null);
		MessageDialog.openWarning(fWindow.getShell(), Localization.getString("RunScriptAction.Error"), msg);
		return false;
	}
	return true;
}*/

//protected Subscription getSubscription()
//{
//	return subscription;
//}
//

//private void buildEnablementChecks() {
//if(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof STPEditor)
//	setEnablement(true);
//}
//

//
//protected String[] getEnvironmentVariables() {
//	return EnvironmentVariablesPreferencePage.getEnvironmentVariables();
//}

//
//protected boolean createClientSession()
//{
//	if (!ClientSession.isConnected())
//	{
//			new SelectServerDialog(fWindow.getShell()).open();
//	}
//	if((ConsoleLogPlugin.getDefault().getPluginPreferences().getBoolean(ConsoleLogPreferenceConstants.CANCELLED))!=true)
//	{
//	subscription = new Subscription(fileName,isGuru());
//	if (ClientSession.isConnected())		
//	{
//	console = ScriptConsole.getInstance(fileName, subscription);
//    console.run();
//	}
//	}		
//	return true;
//}
//
