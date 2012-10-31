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

package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.systemtap.ui.consolelog.LocalLoggedCommand;
import org.eclipse.linuxtools.systemtap.ui.consolelog.LoggedCommand2;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ScpExec;
import org.eclipse.linuxtools.systemtap.ui.consolelog.actions.StopScriptAction;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.views.ErrorView;
import org.eclipse.linuxtools.systemtap.ui.structures.IPasswordPrompt;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;



/**
 * This class serves as a pain in the ConsoleView.  It is used to create a new Command that,
 * through ConsoleDaemons will print all the output the the console.  In order to stop the
 * running Command <code>StopScriptAction</code> should be used to stop this console from
 * running.
 * @author Ryan Morse
 */
public class ScriptConsole extends IOConsole {
	/**
	 * This method is used to get a reference to a <code>ScriptConsole</code>.  If there
	 * is already an console that has the same name as that provided it will be stopped, 
	 * cleared and returned to the caller to use.  If there is no console matching the 
	 * provided name then a new <code>ScriptConsole</code> will be created for use.
	 * @param name The name of the console that should be returned if available.
	 * @return The console with the provided name, or a new instance if none exist.
	 */
	public static ScriptConsole getInstance(String name) {
		ScriptConsole console = null;
		try {
			IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
	
			//Prevent running the same script twice
			if(null != ic) {
				ScriptConsole activeConsole;
				StopScriptAction ssa = new StopScriptAction();
				ssa.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				for(int i=0; i<ic.length; i++) {
					if (ic[i] instanceof ScriptConsole){
						activeConsole = (ScriptConsole)ic[i];
						if(activeConsole.getName().endsWith(name)) {
							//Stop any script currently running
							ssa.run(i);
							//Remove output from last run
							activeConsole.clearConsole();
							activeConsole.setName(name);
							console = activeConsole;
						}
					}
				}
			}
			
			if(null == console) {
				console = new ScriptConsole(name, null);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
			}
		} catch(NullPointerException npe) {
			console = null;
		}
		return console;
	}
	
	/*public static ScriptConsole getInstance(String name) {
		ScriptConsole console = null;
		try {
			IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
	
			//Prevent running the same script twice
			if(null != ic) {
				ScriptConsole activeConsole;
				StopScriptAction ssa = new StopScriptAction();
				ssa.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				for(int i=0; i<ic.length; i++) {
					if (ic[i] instanceof ScriptConsole) {
						activeConsole = (ScriptConsole)ic[i];
						if(activeConsole.getName().endsWith(name)) {
							//Stop any script currently running
							ssa.run(i);
				
							//Remove output from last run
							activeConsole.clearConsole();
							activeConsole.setName(name);
							console = activeConsole;
						}
					}
				}
			}
			
			if(null == console) {
				console = new ScriptConsole(name, null, sub);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
			}
		} catch(NullPointerException npe) {
			console = null;
		}
		return console;
	}*/
	private ScriptConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
		cmd = null;
	}
	
	/*private ScriptConsole(String name, ImageDescriptor imageDescriptor, Subscription sub) {
		super(name, imageDescriptor);
		this.subscription = sub;
		cmd = null;
	}*/
	
	/**
	 * Creates the <code>ConsoleStreamDaemon</code> for passing data from the 
	 * <code>LoggedCommand</code>'s InputStream to the Console.
	 */
	protected void createConsoleDaemon() {
		consoleDaemon = new ConsoleStreamDaemon(this);
	}
	
	/**
	 * Creates the <code>ErrorStreamDaemon</code> for passing data from the 
	 * <code>LoggedCommand</code>'s ErrorStream to the Console and ErrorView.
	 */
	protected void createErrorDaemon(IErrorParser parser) {
		ErrorView errorView = null;
		try {
			IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ErrorView.ID);
			if(null != ivp && ivp instanceof ErrorView)
				errorView = ((ErrorView)ivp);
		} catch(Exception e) {e.printStackTrace();}
		errorDaemon = new ErrorStreamDaemon(this, errorView, parser);
	}
	
	/**
	 * Runs the provided command in this ScriptConsole instance.
	 * @param command The command and arguments to run.
	 * @param envVars The environment variables to use while running
	 * @param prompt The prompt to get the users password if needed.
	 * @param errorParser The parser to handle error messages generated by the command
	 */
	public void run(String[] command, String[] envVars, IPasswordPrompt prompt, IErrorParser errorParser) {
	    cmd = new LoggedCommand2(command, envVars, prompt, 100,this.getName());
	    this.run(cmd, errorParser);
	} 
	
	/**
	 * Runs the provided command in this ScriptConsole instance on the current
	 * host.
	 * @param command The command and arguments to run.
	 * @param envVars The environment variables to use while running
	 * @param prompt The prompt to get the users password if needed.
	 * @param errorParser The parser to handle error messages generated by the command
	 * @since 1.2
	 */
	public void runLocally(String[] command, String[] envVars, IPasswordPrompt prompt, IErrorParser errorParser) {
		cmd = new LocalLoggedCommand(command, envVars, prompt, 100, this.getName());
		this.run(cmd, errorParser);
	} 
	
	private void run(LoggedCommand2 cmd, IErrorParser errorParser){
		createConsoleDaemon();
		if (errorParser != null)
			createErrorDaemon(errorParser);
	    if (errorDaemon != null)
	    	cmd.addErrorStreamListener(errorDaemon);
        cmd.addInputStreamListener(consoleDaemon);
        cmd.start();
        activate();
        ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);		
	}
	
	/**
	 * Check to see if the Command is still running
	 * @return boolean representing if the command is running
	 */
	public boolean isRunning() {
		// If there is no command there is nothing running
		if (null == cmd) {
			return false;
		}
		return cmd.isRunning();
	}
	
	/**
	 * Check to see if this class has already been disposed.
	 * @return boolean represneting whether or not the class has been disposed.
	 */
	public boolean isDisposed() {
		// If there is no command it can be considered disposed
		if (null == cmd) {
			return true;
		}
		return cmd.isDisposed();
	}
	
	/**
	 * Method to allow the user to save the Commands output to a file for use latter.
	 * @param file The new file to save the output to.
	 */
	public void saveStream(File file) {
		if(isRunning())
		//	if(!subscription.saveLog(file))
		//		MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Localization.getString("ScriptConsole.Problem"), Localization.getString("ScriptConsole.ErrorSavingLog"));
			if(!cmd.saveLog(file))
                MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Localization.getString("ScriptConsole.Problem"), Localization.getString("ScriptConsole.ErrorSavingLog"));

	}
	
	/**
	 * Gets the command that is running in this console, or null if there is no running command.
	 * @return The <code>LoggedCommand</code> that is running in this console.
	 */
	public LoggedCommand2 getCommand() {
		return cmd;
	}
	
	/*public String getOutput() {
		return subscription.getOutput();
	}*/
	
	/**
	 * Stops the running command and the associated listeners.
	 */
	public synchronized void stop() {
		  if(isRunning()) {
			  String command[] = new String[1];
				String moduleName = this.getName();
				moduleName = moduleName.substring(0,moduleName.indexOf('.'));
				if (moduleName.indexOf('-') != -1)
					moduleName = moduleName.substring(0, moduleName.indexOf('-'));
			
				command[0] = "ps -ef | grep " + moduleName + " | grep stapio | awk '{print $2}' | xargs kill -SIGINT";
				ScpExec stop = new ScpExec(command, moduleName);
		        stop.start();
              cmd.stop();
              cmd.removeErrorStreamListener(errorDaemon);
              cmd.removeInputStreamListener(consoleDaemon);
              setName(Localization.getString("ScriptConsole.Terminated") + super.getName());
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {this});
		}
	}
	
	/**
	 * Disposes of all internal references in the class. No method should be called after this.
	 */
	@Override
	public void dispose() {
		if(!isDisposed()) {
			if(null != cmd)
				cmd.dispose();
			cmd = null;
			if(null != errorDaemon)
				errorDaemon.dispose();
			errorDaemon = null;
			if(null != consoleDaemon)
				consoleDaemon.dispose();
			consoleDaemon = null;
		}
	}

	/**
	 * Changes the name displayed on this console.
	 * @param name The new name to display on the console.
	 */
	@Override
	public void setName(String name) {
		try {
			super.setName(name);
			if(null != ConsolePlugin.getDefault())
				ConsolePlugin.getDefault().getConsoleManager().refresh(this);
		} catch(Exception e) {}
	}
	
	private LoggedCommand2 cmd;
	
	private ErrorStreamDaemon errorDaemon;
	private ConsoleStreamDaemon consoleDaemon;
//	private Subscription subscription;
}