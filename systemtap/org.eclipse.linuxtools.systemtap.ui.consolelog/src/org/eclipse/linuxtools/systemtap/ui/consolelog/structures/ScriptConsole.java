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
import java.io.IOException;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.runnable.Command;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ScpExec;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.views.ErrorView;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;



/**
 * This class serves as a pane in the ConsoleView.  It is used to create a new Command that,
 * through ConsoleDaemons, will print all the output the the console.  In order to stop the
 * running Command <code>StopScriptAction</code> should be used to stop this console from
 * running.
 * @author Ryan Morse
 */
public class ScriptConsole extends IOConsole {
	private static final long RETRY_STOP_TIME = 500;

	/**
	 * The command that will run in this console.
	 */
	private Command cmd;

	/**
	 * A protocol for sending "stop" signals to cmd when it is forcably
	 * stopped by a user action.
	 */
	private Runnable stopCommand;

	/**
	 * A thread in which to asynchronously run stopCommand.
	 */
	private Thread stopCommandThread;

	/**
	 * A thread used for notifying the console when cmd has successfully stopped.
	 */
	private Thread onCmdStopThread;

	/**
	 * A thread used for starting a new run of cmd. It starts a new run only
	 * once a previous run of cmd has successfully stopped.
	 */
	private Thread onCmdStartThread;

	private String moduleName;

	private ErrorStreamDaemon errorDaemon;
	private ConsoleStreamDaemon consoleDaemon;

	/**
	 * @since 2.0
	 */
	public static interface ScriptConsoleObserver {
		public void runningStateChanged(boolean running);
	}

	private final LinkedList<ScriptConsoleObserver> activeConsoleObservers
	= new LinkedList<ScriptConsoleObserver>();

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
				for (IConsole consoleIterator: ic) {
					if (consoleIterator instanceof ScriptConsole){
						activeConsole = (ScriptConsole) consoleIterator;
						if(activeConsole.getName().endsWith(name)) {
							//Stop any script currently running, and terminate stream listeners.
							if (activeConsole.isRunning()) {
								activeConsole.onCmdStopThread.interrupt();
								activeConsole.stop();
								if (activeConsole.errorDaemon != null) {
									activeConsole.cmd.removeErrorStreamListener(activeConsole.errorDaemon);
								}
								activeConsole.cmd.removeInputStreamListener(activeConsole.consoleDaemon);
							}
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

	/**
	 * This method will check to see if any scripts are currently running.
	 * @return - boolean indicating whether any scripts are running
	 * @since 2.0
	 */
	public static boolean anyRunning() {
		IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		ScriptConsole console;

		for(IConsole con : ic) {
			if (con instanceof ScriptConsole){
				console = (ScriptConsole)con;
				if(console.isRunning()){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method will stop all consoles that are running.
	 * @since 2.0
	 */
	public static void stopAll() {
		IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		ScriptConsole console;

		for(IConsole con : ic) {
			if (con instanceof ScriptConsole){
				console = (ScriptConsole)con;
				console.stop();
			}
		}
	}

	ScriptConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
		cmd = null;
	}

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
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ErrorView.ID);
		if(ivp instanceof ErrorView) {
			errorView = ((ErrorView)ivp);
		}
		errorDaemon = new ErrorStreamDaemon(this, errorView, parser);
	}

	/**
	 * Runs the provided command in this ScriptConsole instance.
	 * @param command The command and arguments to run.
	 * @param envVars The environment variables to use while running
	 * @param errorParser The parser to handle error messages generated by the command
	 * @since 2.0
	 */
	public void run(String[] command, String[] envVars, IErrorParser errorParser) {
		// Don't start a new command if one is already waiting to be started.
		if (onCmdStartThread != null && onCmdStartThread.isAlive()) {
			return;
		}
		cmd = new ScpExec(command);

		this.stopCommand = new Runnable() {
			private final Command stopcmd = cmd;
			private final String stopString = getStopString();

			@Override
			public void run() {
				ScpExec stop = new ScpExec(new String[]{stopString});
				try {
					do {
						stop.start();
						synchronized (stopcmd) {
							stopcmd.wait(RETRY_STOP_TIME);
						}
					} while (stopcmd.isRunning());
				} catch (CoreException e) {
					// Failed to start the 'stop' process. Ignore.
				} catch (InterruptedException e) {
					// Wait was interrupted. Exit.
				}
			}
		};
	    this.run(cmd, errorParser);
	}

	/**
	 * Runs the provided command in this ScriptConsole instance on the current
	 * host.
	 * @param command The command and arguments to run.
	 * @param envVars The environment variables to use while running
	 * @param errorParser The parser to handle error messages generated by the command
	 * @since 2.0
	 */
	public void runLocally(String[] command, String[] envVars, IErrorParser errorParser) {
		runLocally(command, envVars, errorParser, null);
	}
	
	/**
	 * Runs the provided command in this ScriptConsole instance on the current
	 * host.
	 * @param command The command and arguments to run.
	 * @param envVars The environment variables to use while running
	 * @param errorParser The parser to handle error messages generated by the command
	 * @param project The project that command belongs to or null
	 * @since 2.1
	 */
	public void runLocally(String[] command, String[] envVars, IErrorParser errorParser, IProject project) {
		// Don't start a new command if one is already waiting to be started.
		if (onCmdStartThread != null && onCmdStartThread.isAlive()) {
			return;
		}
		cmd = new Command(command, envVars, project);
		final IProject proj = project;

		this.stopCommand = new Runnable() {
			private final Command stopcmd = cmd;
			String stopString = getStopString();

			@Override
			public void run() {
				try {
					do {
						RuntimeProcessFactory.getFactory().exec(stopString, null, proj);
						synchronized (stopcmd) {
							stopcmd.wait(RETRY_STOP_TIME);
						}
					} while (stopcmd.isRunning());
				} catch (IOException e) {
					ExceptionErrorDialog.openError(Localization.getString("ScriptConsole.ErrorKillingStap"), e); //$NON-NLS-1$
				} catch (InterruptedException e) {
					//Wait was interrupted. Exit.
				}
			}
		};
		this.run(cmd, errorParser);
	}

	private void run(final Command cmd, IErrorParser errorParser){
		final Runnable onCmdStop = new Runnable() {
			@Override
			public void run() {
				try {
					synchronized (cmd) {
						cmd.wait();
					}
					onCmdStopActions();
				} catch (InterruptedException e) {
					return;
				}
			}
		};
		Runnable onCmdStart = new Runnable() {
			@Override
			public void run() {
				if (stopCommandThread != null && stopCommandThread.isAlive()) {
					try {
						stopCommandThread.join();
					} catch (InterruptedException e) {
						return;
					}
				}
				createConsoleDaemon();
				if (errorDaemon != null) {
					cmd.addErrorStreamListener(errorDaemon);
				}
				cmd.addInputStreamListener(consoleDaemon);
				try {
					cmd.start();
				} catch (CoreException e) {
					ExceptionErrorDialog.openError(e.getMessage(), e);
					notifyConsoleObservers(false);
					cmd.dispose();
					return;
				}
				notifyConsoleObservers(true);
				onCmdStopThread = new Thread(onCmdStop);
				onCmdStopThread.start();
			}
		};

        if (errorParser != null) {
			createErrorDaemon(errorParser);
		}
		activate();
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);

		onCmdStartThread = new Thread(onCmdStart);
        onCmdStartThread.start();
	}

	private final void onCmdStopActions() {
		notifyConsoleObservers(false);
	}

	void notifyConsoleObservers(boolean running){
		for (ScriptConsoleObserver observer : activeConsoleObservers) {
			observer.runningStateChanged(running);
		}
	}

	/**
	 * @since 2.0
	 */
	public void addScriptConsoleObserver (ScriptConsoleObserver observer){
		activeConsoleObservers.add(observer);
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
	 * @return boolean representing whether or not the class has been disposed.
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
		if (isRunning()) {
			if (!cmd.saveLog(file)) {
				MessageDialog
						.openWarning(
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getShell(),
								Localization.getString("ScriptConsole.Problem"), Localization.getString("ScriptConsole.ErrorSavingLog")); //$NON-NLS-1$//$NON-NLS-2$

			}
		}
	}

	/**
	 * Gets the command that is running in this console, or null if there is no running command.
	 * @return The <code>LoggedCommand</code> that is running in this console.
	 * @since 2.0
	 */
	public Command getCommand() {
		return cmd;
	}

	/**
	 * Stops the running command and the associated listeners.
	 */
	public synchronized void stop() {
		if (isRunning() && (stopCommandThread == null || !stopCommandThread.isAlive())) {
			// Stop the underlying stap process
			stopCommandThread = new Thread(this.stopCommand);
			stopCommandThread.start();
			setName(Localization.getString("ScriptConsole.Terminated") + super.getName()); //$NON-NLS-1$
		}
	}

	private String getModuleName(){
		if(this.moduleName == null){
			moduleName = this.getName();
			int lastSlash = moduleName.lastIndexOf('/')+1;
			if (lastSlash < 0) {
				lastSlash = 0;
			}
			int lastDot = moduleName.indexOf(".stp"); //$NON-NLS-1$
			if (lastDot > 0) {
				moduleName = moduleName.substring(lastSlash, lastDot);
			}
		}
		return this.moduleName;
	}

	private String getStopString(){
		  return "pkill -SIGINT -f stapio.*"+ getModuleName();  //$NON-NLS-1$
	}
	/**
	 * Disposes of all internal references in the class. No method should be called after this.
	 */
	@Override
	public void dispose() {
		if(!isDisposed()) {
			if(null != cmd) {
				cmd.dispose();
			}
			cmd = null;
			if(null != errorDaemon) {
				errorDaemon.dispose();
			}
			errorDaemon = null;
			if(null != consoleDaemon) {
				consoleDaemon.dispose();
			}
			consoleDaemon = null;
		}
	}

	/**
	 * Changes the name displayed on this console.
	 * @param name The new name to display on the console.
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		if(null != ConsolePlugin.getDefault()) {
			ConsolePlugin.getDefault().getConsoleManager().refresh(this);
		}
	}
}