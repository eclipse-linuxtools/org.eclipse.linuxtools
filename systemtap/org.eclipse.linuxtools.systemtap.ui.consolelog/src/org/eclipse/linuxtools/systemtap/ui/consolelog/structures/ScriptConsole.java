/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *     Red Hat - ongoing maintenance
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.structures.runnable.Command;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ScpExec;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.views.ErrorView;
import org.eclipse.swt.widgets.Display;
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

    /**
     * The command that will run in this console.
     */
    private Command cmd = null;

    /**
     * A protocol for sending "stop" signals to cmd when it is forcably
     * stopped by a user action.
     */
    private StopCommand stopCommand;

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

    private ErrorStreamDaemon errorDaemon;
    private ConsoleStreamDaemon consoleDaemon;

    /**
     * @since 2.0
     */
    public interface ScriptConsoleObserver {
        /**
         * @since 3.0
         */
        void runningStateChanged(boolean started, boolean stopped);
    }

    private final List<ScriptConsoleObserver> activeConsoleObservers = new LinkedList<>();
    private List<ScriptConsoleObserver> inactiveConsoleObservers = new LinkedList<>();

    /**
     * Returns whether or not a ScriptConsole of the specified name exists and is running.
     * @param name The name of the console (likely a script name) to check.
     * @return <code>true</code> if a ScriptConsole of the given name both exists and is running,
     * or <code>false</code> otherwise.
     * @since 3.0
     */
    public static boolean instanceIsRunning(String name) {
        IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        if (ic != null) {
            for (IConsole consoleIterator : ic) {
                if (consoleIterator instanceof ScriptConsole) {
                    ScriptConsole activeConsole = (ScriptConsole) consoleIterator;
                    if (activeConsole.getName().endsWith(name) && activeConsole.isRunning()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method is used to create a reference to a new <code>ScriptConsole</code>.  If there
     * is already an console that has the same name as that provided it will be stopped,
     * cleared and returned to the caller to use.  If there is no console matching the
     * provided name then a new <code>ScriptConsole</code> will be created for use.
     * @param name The name of the console that should be created & returned.
     * @return A console of the specified name, or <code>null</code> if there is an error.
     */
    public synchronized static ScriptConsole getInstance(String name) {
        ScriptConsole console = null;
        try {
            IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();

            //Prevent running the same script twice
            if (ic != null) {
                ScriptConsole activeConsole;
                for (IConsole consoleIterator : ic) {
                    if (consoleIterator instanceof ScriptConsole) {
                        activeConsole = (ScriptConsole) consoleIterator;
                        if (activeConsole.getName().endsWith(name)) {
                            //Stop any script currently running.
                            if (activeConsole.onCmdStopThread != null && activeConsole.onCmdStopThread.isAlive()) {
                                activeConsole.onCmdStopThread.interrupt();
                                try {
                                    activeConsole.onCmdStopThread.join();
                                } catch (InterruptedException e) {}
                            }
                            if (activeConsole.isRunning()) {
                                activeConsole.stopAndDispose();
                            } else {
                                activeConsole.disposeCommand();
                            }
                            //Remove output from last run
                            activeConsole.clearConsole();
                            activeConsole.setName(name);
                            console = activeConsole;
                            break;
                        }
                    }
                }
            }

            if (console == null) {
                console = new ScriptConsole(name, null);
                ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
            }
        } catch (NullPointerException npe) {
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

        for (IConsole con : ic) {
            if (con instanceof ScriptConsole) {
                console = (ScriptConsole)con;
                if (console.isRunning()) {
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

        for (IConsole con : ic) {
            if (con instanceof ScriptConsole) {
                console = (ScriptConsole)con;
                console.stop();
            }
        }
    }

    ScriptConsole(String name, ImageDescriptor imageDescriptor) {
        super(name, imageDescriptor);
    }

    private class StopCommand implements Runnable {
        protected final Command stopcmd;
        private boolean disposeOnStop = false;

        private StopCommand(Command stopcmd) {
            this.stopcmd = stopcmd;
        }

        void makeDisposeOnStop() {
            disposeOnStop = true;
        }

        @Override
        public void run() {
            if (consoleDaemon != null) {
                stopcmd.removeInputStreamListener(consoleDaemon);
            }
            if (errorDaemon != null) {
                stopcmd.removeErrorStreamListener(errorDaemon);
            }
            if (!disposeOnStop) {
                stopcmd.stop();
            } else {
                stopcmd.dispose();
            }
        }

    }

    /**
     * Creates the <code>ConsoleStreamDaemon</code> for passing data from the
     * <code>LoggedCommand</code>'s InputStream to the Console.
     */
    private void createConsoleDaemon() {
        consoleDaemon = new ConsoleStreamDaemon(this);
    }

    /**
     * Creates the <code>ErrorStreamDaemon</code> for passing data from the
     * <code>LoggedCommand</code>'s ErrorStream to the Console and ErrorView.
     */
    private void createErrorDaemon(IErrorParser parser) {
        ErrorView errorView = null;
        IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ErrorView.ID);
        if (ivp instanceof ErrorView) {
            errorView = ((ErrorView)ivp);
        }
        errorDaemon = new ErrorStreamDaemon(this, errorView, parser);
    }

    private boolean waitingToStart() {
        return onCmdStartThread != null && onCmdStartThread.isAlive();
    }

    /**
     * Runs the provided command in this ScriptConsole instance.
     * @param command The command and arguments to run.
     * @param envVars The environment variables to use while running
     * @param remoteOptions The remote options (such as username and password) to run the script with.
     * @param errorParser The parser to handle error messages generated by the command
     * @since 3.0
     */
    public void run(String[] command, String[] envVars, final RemoteScriptOptions remoteOptions, IErrorParser errorParser) {
        // Don't start a new command if one is already waiting to be started.
        if (waitingToStart()) {
            return;
        }
        cmd = new ScpExec(command, remoteOptions, envVars);
        this.run(cmd, errorParser);
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
        if (waitingToStart()) {
            return;
        }
        cmd = new Command(command, envVars, project);
        this.run(cmd, errorParser);
    }

    private void run(final Command cmd, IErrorParser errorParser) {
        stopCommand = new StopCommand(cmd);
        final Runnable onCmdStop = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (cmd) {
                        while (cmd.isRunning()) {
                            cmd.wait();
                        }
                        onCmdStopActions();
                    }
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
                onCmdStopThread = new Thread(onCmdStop);
                onCmdStopThread.start();
                try {
                    cmd.start();
                } catch (final CoreException e) {
                    ExceptionErrorDialog.openError(
                            Localization.getString("ScriptConsole.ErrorRunningStapTitle"), //$NON-NLS-1$
                            Localization.getString("ScriptConsole.ErrorRunningStapMessage"), e);//$NON-NLS-1$
                    cmd.dispose();
                    return;
                }
                clearConsole();
                notifyConsoleObservers();
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

    private void onCmdStopActions() {
        notifyConsoleObservers();
        final String name = super.getName();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                setName(Localization.getString("ScriptConsole.Terminated") + name); //$NON-NLS-1$
            }
        });
    }

    private synchronized void notifyConsoleObservers() {
        boolean started = hasStarted();
        boolean running = isRunning();
        for (ScriptConsoleObserver observer : inactiveConsoleObservers) {
            activeConsoleObservers.remove(observer);
        }
        inactiveConsoleObservers = new LinkedList<>();
        for (ScriptConsoleObserver observer : activeConsoleObservers) {
            observer.runningStateChanged(started, !running);
        }
    }

    /**
     * @since 2.0
     */
    public synchronized void addScriptConsoleObserver(ScriptConsoleObserver observer) {
        activeConsoleObservers.add(observer);
        observer.runningStateChanged(hasStarted(), !isRunning());
    }

    /**
     * @since 3.0
     */
    public synchronized void removeScriptConsoleObserver(ScriptConsoleObserver observer) {
        if (activeConsoleObservers.contains(observer)) {
            inactiveConsoleObservers.add(observer);
        }
    }

    /**
     * Check to see if the Command has been set up
     * @return boolean representing if the command has started
     * @since 3.0
     */
    private boolean hasStarted() {
        return cmd == null ? false : cmd.hasStarted();
    }

    /**
     * Check to see if the Command is still running
     * @return boolean representing if the command is running
     */
    public boolean isRunning() {
        return cmd == null ? false : cmd.isRunning();
    }

    /**
     * Check to see if this class has already been disposed.
     * @return boolean representing whether or not the class has been disposed.
     */
    public boolean isDisposed() {
        // If there is no command it can be considered disposed
        if (cmd == null) {
            return true;
        }
        return cmd.isDisposed();
    }

    private void disposeCommand() {
        if (!isDisposed()) {
            cmd.dispose();
        }
    }

    /**
     * Method to allow the user to save the Commands output to a file for use latter.
     * Does not return a value indicating success of the operation; for that, use
     * {@link #saveStreamAndReturnResult(File)} instead.
     * @param file The new file to save the output to.
     */
    public void saveStream(File file) {
        saveStreamAndReturnResult(file);
    }

    /**
     * Method to allow the user to save the Commands output to a file for use later.
     * @param file The new file to save the output to.
     * @return <code>true</code> if the save result was successful, <code>false</code> otherwise.
     * Note that a failed save attempt will not interfere with an already-running log.
     * @since 3.1
     */
    public boolean saveStreamAndReturnResult(File file) {
        if (!cmd.saveLog(file)) {
            MessageDialog.openWarning(
                    PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell(),
                    Localization.getString("ScriptConsole.Problem"), Localization.getString("ScriptConsole.ErrorSavingLog")); //$NON-NLS-1$//$NON-NLS-2$
            return false;
        }
        return true;
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
     * @return The process associated with this console's script when it is run.
     * A <code>null</code> process indicates that the script has not yet started
     * (if {@link #isRunning} returns true) or failed to start (if {@link #isRunning} is false).
     * @since 3.0
     */
    public Process getProcess() {
        return cmd != null ? cmd.getProcess() : null;
    }

    /**
     * Stops and disposes the running command.
     */
    private synchronized void stopAndDispose() {
        stopCommand.makeDisposeOnStop();
        stop();
    }

    /**
     * Stops the running command and the associated listeners.
     */
    public synchronized void stop() {
        if (isRunning() && (stopCommandThread == null || !stopCommandThread.isAlive())) {
            // Stop the underlying stap process
            stopCommandThread = new Thread(stopCommand);
            stopCommandThread.start();
        }
    }

    /**
     * Disposes of all internal references in the class. No method should be called after this.
     */
    @Override
    public void dispose() {
        if (!isDisposed()) {
            if (cmd != null) {
                cmd.dispose();
            }
            cmd = null;
            if (errorDaemon != null) {
                errorDaemon.dispose();
            }
            errorDaemon = null;
            if (consoleDaemon != null) {
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
        if (ConsolePlugin.getDefault() != null) {
            ConsolePlugin.getDefault().getConsoleManager().refresh(this);
        }
    }
}