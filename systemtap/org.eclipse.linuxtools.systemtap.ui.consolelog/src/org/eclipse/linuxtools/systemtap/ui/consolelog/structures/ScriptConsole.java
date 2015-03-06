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
import java.util.HashSet;
import java.util.Set;

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
    private final StopCommand stopCommand = new StopCommand();

    /**
     * A thread in which to asynchronously run {@link stopCommand}.
     */
    private Thread stopCommandThread;

    /**
     * A thread used for notifying the console when {@link #cmd} has successfully stopped.
     */
    private Thread onCmdStopThread;

    /**
     * A thread used for starting a new run of {@link #cmd}. It starts a new run only
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
         * Notifying observer of state change.
         * @param started If the action is started or not.
         * @param stopped If the action is stopped or not.
         * @since 3.0
         */
        void runningStateChanged(boolean started, boolean stopped);
    }

    private final Set<ScriptConsoleObserver> activeConsoleObservers = new HashSet<>();
    private final Set<ScriptConsoleObserver> inactiveConsoleObservers = new HashSet<>();

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
                    if (activeConsole.getBaseName().equals(name) && activeConsole.isRunning()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method is used to create a reference to a new <code>ScriptConsole</code>.  If there
     * is already a console that has the same name as that provided it will be stopped,
     * cleared and returned to the caller to use.  If there is no console matching the
     * provided name then a new <code>ScriptConsole</code> will be created for use.
     * @param name The name of the console that should be created &amp; returned.
     * @return A console of the specified name.
     */
    public synchronized static ScriptConsole getInstance(String name) {
        IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        if (ic != null) {
            ScriptConsole activeConsole;
            for (IConsole consoleIterator : ic) {
                if (consoleIterator instanceof ScriptConsole) {
                    activeConsole = (ScriptConsole) consoleIterator;
                    if (activeConsole.getBaseName().equals(name)) {
                        activeConsole.reset();
                        return activeConsole;
                    }
                }
            }
        }

        // If no console with given name exists, make a new one
        ScriptConsole console = new ScriptConsole(name, null);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
        return console;
    }

    /**
     * This method will check to see if any scripts are currently running.
     * @return boolean indicating whether any scripts are running.
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

    /**
     * Runs the provided command in this ScriptConsole instance.
     * @param command The command and arguments to run.
     * @param envVars The environment variables to use while running.
     * @param remoteOptions The remote options (such as username and password) to run the script with.
     * @param errorParser The parser to handle error messages generated by the command.
     * @since 3.0
     */
    public synchronized void run(String[] command, String[] envVars, final RemoteScriptOptions remoteOptions, IErrorParser errorParser) {
        // Don't start a new command if one is already waiting to be started.
        if (waitingToStart()) {
            return;
        }
        cmd = new ScpExec(command, remoteOptions, envVars);
        internalRun(errorParser);
    }

    /**
     * Runs the provided command in this ScriptConsole instance on the current host.
     * @param command The command and arguments to run.
     * @param envVars The environment variables to use while running.
     * @param errorParser The parser to handle error messages generated by the command.
     * @param project The project that command belongs to or null.
     * @since 2.1
     */
    public synchronized void runLocally(String[] command, String[] envVars, IErrorParser errorParser, IProject project) {
        // Don't start a new command if one is already waiting to be started.
        if (waitingToStart()) {
            return;
        }
        cmd = new Command(command, envVars, project);
        internalRun(errorParser);
    }

    private void internalRun(IErrorParser errorParser) {
        if (hasStarted()) {
            reset();
        }
        if (errorParser != null) {
            createErrorDaemon(errorParser);
        }
        createConsoleDaemon();
        notifyConsoleObservers();
        activate();

        onCmdStartThread = new Thread(onCmdStart);
        onCmdStartThread.start();
    }

    private final Runnable onCmdStart = new Runnable() {
        @Override
        public void run() {
            // If stopping the previous command, wait for it to stop.
            if (isThreadAlive(stopCommandThread)
                    && stopCommand.stopcmd != cmd) {
                try {
                    stopCommandThread.join();
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (errorDaemon != null) {
                cmd.addErrorStreamListener(errorDaemon);
            }
            cmd.addInputStreamListener(consoleDaemon);
            onCmdStopThread = new Thread(onCmdStop);
            onCmdStopThread.start();
            clearConsole();
            try {
                cmd.start();
            } catch (final CoreException e) {
                ExceptionErrorDialog.openError(
                        Localization.getString("ScriptConsole.ErrorRunningStapTitle"), //$NON-NLS-1$
                        Localization.getString("ScriptConsole.ErrorRunningStapMessage"), e);//$NON-NLS-1$
                cmd.dispose();
                return;
            }
            notifyConsoleObservers();
        }
    };

    private class StopCommand implements Runnable {
        private Command stopcmd;
        private ErrorStreamDaemon stopErrorDaemon;
        private ConsoleStreamDaemon stopConsoleDaemon;
        private boolean disposeOnStop = false;

        /**
         * Prepares and begins the process (in a new thread) that will stop the provided command.
         * If there is already a running stop process, though, no action will be taken.
         * @param stopcmd The command to stop.
         * @param stopErrorDaemon The error stream gobbler of the command to stop.
         * @param stopConsoleDaemon The output stream gobbler of the command to stop.
         * @param disposeOnStop If <code>true</code>, the command will be disposed when it is stopped.
         */
        void start(Command stopcmd, ErrorStreamDaemon stopErrorDaemon,
                ConsoleStreamDaemon stopConsoleDaemon, boolean disposeOnStop) {
            if (isRunning() && !isThreadAlive(stopCommandThread)) {
                this.stopcmd = stopcmd;
                this.stopErrorDaemon = stopErrorDaemon;
                this.stopConsoleDaemon = stopConsoleDaemon;
                this.disposeOnStop = disposeOnStop;
                stopCommandThread = new Thread(this);
                stopCommandThread.start();
            }
        }

        @Override
        public void run() {
            // If the command to be stopped is still starting up, wait for it to start.
            if (isThreadAlive(onCmdStartThread)
                    && stopcmd == cmd) {
                try {
                    onCmdStartThread.join();
                } catch (InterruptedException e) {}
            }
            if (stopConsoleDaemon != null) {
                stopcmd.removeInputStreamListener(stopConsoleDaemon);
                stopConsoleDaemon.dispose();
            }
            if (stopErrorDaemon != null) {
                stopcmd.removeErrorStreamListener(stopErrorDaemon);
                stopErrorDaemon.dispose();
            }
            if (!disposeOnStop) {
                stopcmd.stop();
            } else {
                stopcmd.dispose();
            }
        }

        void dispose() {
            if (stopcmd != null) {
                stopcmd.dispose();
                stopcmd = null;
            }
            if (stopErrorDaemon != null) {
                stopErrorDaemon.dispose();
                stopErrorDaemon = null;
            }
            if (stopConsoleDaemon != null) {
                stopConsoleDaemon.dispose();
                stopConsoleDaemon = null;
            }
        }

    }

    private final Runnable onCmdStop = new Runnable() {
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

    /**
     * Stops the running command and the associated listeners.
     */
    public synchronized void stop() {
        stopCommand.start(cmd, errorDaemon, consoleDaemon, false);
    }

    /**
     * Stops and disposes the running command.
     */
    private synchronized void stopAndDispose() {
        stopCommand.start(cmd, errorDaemon, consoleDaemon, true);
    }

    /**
     * Stops the running command and immediately prepares the console for another command.
     */
    private void reset() {
        if (waitingToStart()) {
            try {
                onCmdStartThread.join();
            } catch (InterruptedException e) {}
        }
        if (isThreadAlive(onCmdStopThread)) {
            onCmdStopThread.interrupt();
            try {
                onCmdStopThread.join();
            } catch (InterruptedException e) {}
        }
        if (isThreadAlive(stopCommandThread)) {
            try {
                stopCommandThread.join();
            } catch (InterruptedException e) {}
            disposeCommand();
        } else if (isRunning()) {
            stopAndDispose();
        } else {
            disposeCommand();
        }
        clearConsole();
        setName(getBaseName());
    }

    private synchronized void notifyConsoleObservers() {
        boolean started = hasStarted();
        boolean running = isRunning();
        for (ScriptConsoleObserver observer : inactiveConsoleObservers) {
            activeConsoleObservers.remove(observer);
        }
        inactiveConsoleObservers.clear();
        for (ScriptConsoleObserver observer : activeConsoleObservers) {
            observer.runningStateChanged(started, !running);
        }
    }

    /**
     * Add observer to the script console.
     * 
     * @param observer The observer to be added.
     * @since 2.0
     */
    public synchronized void addScriptConsoleObserver(ScriptConsoleObserver observer) {
        activeConsoleObservers.add(observer);
        observer.runningStateChanged(hasStarted(), !isRunning());
    }

    /**
     * Removes observer from the script console.
     * @param observer The observer to be removed.
     * @since 3.0
     */
    public synchronized void removeScriptConsoleObserver(ScriptConsoleObserver observer) {
        inactiveConsoleObservers.add(observer);
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

    private boolean waitingToStart() {
        return isThreadAlive(onCmdStartThread);
    }

    /**
     * Check to see if the Command is still running.
     * @return boolean representing if the command is running.
     */
    public boolean isRunning() {
        return cmd == null ? false : cmd.isRunning();
    }

    /**
     * Check to see if the Command has been set up.
     * @return boolean representing if the command has started.
     */
    private boolean hasStarted() {
        return cmd == null ? false : cmd.hasStarted();
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
     * @return The name of this console with all status strings removed.
     */
    private String getBaseName() {
        return getName().replace(Localization.getString(
                "ScriptConsole.Terminated"), ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private boolean isThreadAlive(Thread thread) {
        return thread != null && thread.isAlive();
    }

    /**
     * Disposes of all internal references in the class. No method should be called after this.
     */
    @Override
    public synchronized void dispose() {
        if (!isDisposed()) {
            onCmdStartThread = null;
            onCmdStopThread = null;
            stopCommandThread = null;

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
            stopCommand.dispose();

            if (activeConsoleObservers != null) {
                activeConsoleObservers.clear();
            }
            if (inactiveConsoleObservers != null) {
                inactiveConsoleObservers.clear();
            }
        }
    }

}