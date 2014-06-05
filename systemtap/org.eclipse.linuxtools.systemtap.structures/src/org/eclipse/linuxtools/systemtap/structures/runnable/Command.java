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

package org.eclipse.linuxtools.systemtap.structures.runnable;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.systemtap.structures.StructuresPlugin;
import org.eclipse.linuxtools.systemtap.structures.LoggingStreamDaemon;
import org.eclipse.linuxtools.systemtap.structures.listeners.IGobblerListener;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;

/**
 * A class to spawn a separate thread to run a <code>Process</code>.
 * @author Ryan Morse
 * @since 2.0
 */
public class Command implements Runnable {
    /*
     * Bug in the exec command prevents using a single string.  Forced
     * to use a workaround in order to run commands with spaces.
     *
     * http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4365120
     * http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4109888
     */

    /**
     * @since 2.0
     */
    protected boolean stopped = false;
    private boolean started = false;
    /**
     * @since 2.0
     */
    protected StreamGobbler inputGobbler = null;
    /**
     * @since 2.0
     */
    protected StreamGobbler errorGobbler = null;

    private boolean disposed = false;
    private List<IGobblerListener> inputListeners = new ArrayList<>();    //Only used to allow adding listeners before creating the StreamGobbler
    private List<IGobblerListener> errorListeners = new ArrayList<>();    //Only used to allow adding listeners before creating the StreamGobbler
    private int returnVal = Integer.MAX_VALUE;

    /**
     * @since 3.0
     */
    protected final String[] cmd;
    /**
     * @since 3.0
     */
    protected final String[] envVars;

    protected Process process;
    /**
     * @since 2.1
     */
    protected final IProject project;
    private final LoggingStreamDaemon logger;

    public static final int ERROR_STREAM = 0;
    public static final int INPUT_STREAM = 1;

    /**
     * Spawns the new thread that this class will run in.  From the Runnable
     * interface spawning the new thread automatically calls the run() method.
     * This must be called by the implementing class in order to start the
     * StreamGobbler.
     * @param cmd The entire command to run
     * @param envVars List of all environment variables to use
     * @since 2.0
     */
    public Command(String[] cmd, String[] envVars) {
        this(cmd, envVars, null);
    }

    /**
     * Spawns the new thread that this class will run in.  From the Runnable
     * interface spawning the new thread automatically calls the run() method.
     * This must be called by the implementing class in order to start the
     * StreamGobbler.
     * @param cmd The entire command to run
     * @param envVars List of all environment variables to use, or <code>null</code> if none
     * @param project The project this script belongs to, or <code>null</code> if projectless
     * @since 2.1
     */
    public Command(String[] cmd, String[] envVars, IProject project) {
        this.cmd = cmd != null ? Arrays.copyOf(cmd, cmd.length) : null;
        this.envVars = envVars != null ? Arrays.copyOf(envVars, envVars.length) : null;
        this.project = project;
        logger = new LoggingStreamDaemon();
        addInputStreamListener(logger);
    }

    /**
     * Starts the <code>Thread</code> that the new <code>Process</code> will run in.
     * This must be called in order to get the process to start running.
     * @throws CoreException
     */
    public void start() throws CoreException {
        IStatus status = init();
        if (status.isOK()) {
            Thread t = new Thread(this, cmd[0]);
            t.start();
            started = true;
        } else {
            stop();
            returnVal = Integer.MIN_VALUE;
            throw new CoreException(status);
        }
    }

    /**
     * Starts up the process that will execute the provided command and registers
     * the <code>StreamGobblers</code> with their respective streams.
     * @since 2.0
     */
    protected IStatus init() {
        try {
            process = RuntimeProcessFactory.getFactory().exec(cmd, envVars, project);

            if (process == null) {
                return new Status(IStatus.ERROR, StructuresPlugin.PLUGIN_ID, Messages.Command_failedToRunSystemtap);
            }

            errorGobbler = new StreamGobbler(process.getErrorStream());
            inputGobbler = new StreamGobbler(process.getInputStream());

            transferListeners();
            return Status.OK_STATUS;
        } catch (IOException e) {
            return new Status(IStatus.ERROR, StructuresPlugin.PLUGIN_ID, e.getMessage(), e);
        }
    }

    /**
     * This transfers any listeners which may have been added
     * to the command before the process has been constructed
     * properly to the process itself.
     * @since 2.0
     */
    protected void transferListeners() {
        for (IGobblerListener listener : inputListeners) {
            inputGobbler.addDataListener(listener);
        }
        for (IGobblerListener listener : errorListeners) {
            errorGobbler.addDataListener(listener);
        }
    }

    /**
     * This method handles checking the status of the running <code>Process</code>. It
     * is called when the new Thread is created, and thus should never be called by
     * any implementing program. To run call the {@link #start} method.
     */
    @Override
    public void run() {
        errorGobbler.start();
        inputGobbler.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {}
        stop();
    }

    /**
     * Stops the process from running and stops the <code>StreamGobblers</code> from monitering
     * the dead process and unregisters the StreamListener. Also wakes up any threads waiting
     * on this command.
     */
    public synchronized void stop() {
        if (!stopped) {
            if (errorGobbler != null) {
                errorGobbler.stop();
            }
            if (inputGobbler != null) {
                inputGobbler.stop();
            }
            try {
                if (process != null) {
                    process.waitFor();
                }
            } catch (InterruptedException e) {
                // This thread was interrupted while waiting for
                // the process to exit. Destroy the process just
                // to make sure it exits.
                process.destroy();
            }
            removeInputStreamListener(logger);
            stopped = true;
            notifyAll(); // Wake up threads waiting for this command to stop.
        }
    }

    /**
     * Method to check whether or not the process is running.
     * @return The execution status.
     */
    public boolean isRunning() {
        return !stopped;
    }

    /**
     * Method to check whether or not the process has began to run.
     * @return <code>false</code> before the process begins to run or
     * if initialization of the process has failed; <code>true</code> otherwise.
     * @since 3.0
     */
    public boolean hasStarted() {
        return started;
    }

    /**
     * Method to check if this class has already been disposed.
     * @return Status of the class.
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * The return value of the process.
     * 2^231-1 if the process is still running.
     * -2^231 if there was an error creating the process
     * @return The return value generated from running the provided command.
     */
    public int getReturnValue() {
        return returnVal;
    }

    /**
     * Registers the provided <code>IGobblerListener</code> with the InputStream
     * @param listener A listener to monitor the InputStream from the Process
     */
    public void addInputStreamListener(IGobblerListener listener) {
        if (inputGobbler != null) {
            inputGobbler.addDataListener(listener);
        } else {
            inputListeners.add(listener);
        }
    }

    /**
     * Registers the provided <code>IGobblerListener</code> with the ErrorStream
     * @param listener A listener to monitor the ErrorStream from the Process
     */
    public void addErrorStreamListener(IGobblerListener listener) {
        if (errorGobbler != null) {
            errorGobbler.addDataListener(listener);
        } else {
            errorListeners.add(listener);
        }
    }

    /**
     * Removes the provided listener from those monitoring the InputStream.
     * @param listener An </code>IGobblerListener</code> that is monitoring the stream.
     */
    public void removeInputStreamListener(IGobblerListener listener) {
        if (inputGobbler != null) {
            inputGobbler.removeDataListener(listener);
        } else {
            inputListeners.remove(listener);
        }
    }

    /**
     * Removes the provided listener from those monitoring the ErrorStream.
     * @param listener An </code>IGobblerListener</code> that is monitoring the stream.
     */
    public void removeErrorStreamListener(IGobblerListener listener) {
        if (errorGobbler != null) {
            errorGobbler.removeDataListener(listener);
        } else {
            errorListeners.remove(listener);
        }
    }

    /**
     * Saves the input stream data to a permanent file. Any new data on the
     * stream will automatically be saved to the file.
     * @param file The file to save the InputStream to.
     */
    public boolean saveLog(File file) {
        return logger.saveLog(file);
    }

    /**
     * Disposes of all internal components of this class. Nothing in the class should be
     * referenced after this is called.
     */
    public synchronized void dispose() {
        if (!disposed) {
            stop();
            disposed = true;

            inputListeners.clear();
            errorListeners.clear();

            inputListeners = null;
            errorListeners = null;

            if (inputGobbler != null) {
                inputGobbler.dispose();
            }
            inputGobbler = null;

            if (errorGobbler != null) {
                errorGobbler.dispose();
            }
            errorGobbler = null;
            logger.dispose();
        }
    }

    /**
     * @return The process of this command.
     * @since 3.0
     */
    public Process getProcess() {
        return process;
    }

}
