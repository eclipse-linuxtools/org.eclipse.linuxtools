/*******************************************************************************
 * Copyright (c) 2009-2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.channels.FileChannel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.FrameworkUtil;
/**
 * Utilities for calling system executables.
 *
 */
public class Utils {

    /**
     * Runs the given command and parameters.
     *
     * @param command
     *            The command with all parameters.
     * @return Stream containing the combined content of stderr and stdout.
     * @throws IOException
     *             If IOException occurs.
     */
    public static BufferedProcessInputStream runCommandToInputStream(String... command)
            throws IOException {
        return runCommandToInputStream(null, command);
    }

    /**
     * Runs the given command and parameters.
     * @param project rpm project
     *
     * @param command
     *            The command with all parameters.
     * @return Stream containing the combined content of stderr and stdout.
     * @throws IOException
     *             If IOException occurs.
     * @since 2.1
     */
    private static BufferedProcessInputStream runCommandToInputStream(IProject project, String... command)
            throws IOException {
        Process p = RuntimeProcessFactory.getFactory().exec(command, project);
        return new BufferedProcessInputStream(p);
    }

    /**
     * Runs the given command and parameters.
     *
     * @param outStream
     *            The stream to write the output to.
     * @param project
     *               The project which is executing this command.
     * @param command
     *            The command with all parameters.
     * @return int The return value of the command.
     * @throws IOException If an IOException occurs.
     * @since 1.1
     */
    public static IStatus runCommand(final OutputStream outStream, IProject project,
            String... command) throws IOException {
        Process child = RuntimeProcessFactory.getFactory().exec(command, project);

        final BufferedInputStream in = new BufferedInputStream(
                new SequenceInputStream(child.getInputStream(),
                        child.getErrorStream()));

        Job readinJob = new Job("") { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    int i;
                    while ((i = in.read()) != -1) {
                        outStream.write(i);
                    }
                    outStream.flush();
                    outStream.close();
                    in.close();
                } catch (IOException e) {
                    return Status.CANCEL_STATUS;
                }
                return Status.OK_STATUS;
            }

        };
        readinJob.schedule();

        try {
            child.waitFor();
            readinJob.join();
        } catch (InterruptedException e) {
            child.destroy();
            readinJob.cancel();
        }
        IStatus result;
        if (child.exitValue() != 0){
            result = new Status(
                    IStatus.ERROR,
                    FrameworkUtil.getBundle(Utils.class).getSymbolicName(),
                    NLS.bind(
                            Messages.Utils_NON_ZERO_RETURN_CODE, child.exitValue()), null);
        } else{
            result = Status.OK_STATUS;
        }
        return result;
    }

    /**
     * Run a command and return its output.
     * @param command The command to execute.
     * @return The output of the executed command.
     * @throws IOException If an I/O exception occurred.
     */
    public static String runCommandToString(String... command)
            throws IOException {
        return runCommandToString(null, command);
    }

    /**
     * Run a command and return its output.
     * @param project rpm Project
     * @param command The command to execute.
     * @return The output of the executed command.
     * @throws IOException If an I/O exception occurred.
     * @since 2.1
     */
    public static String runCommandToString(IProject project, String... command)
            throws IOException {
        BufferedInputStream in = runCommandToInputStream(project, command);
        return inputStreamToString(in);
    }
    /**
     * Reads the content of the given InputStream and returns its textual
     * representation.
     *
     * @param stream
     *            The InputStream to read.
     * @return Textual content of the stream.
     * @throws IOException If an IOException occurs.
     */
    private static String inputStreamToString(InputStream stream)
            throws IOException {
        StringBuilder retStr = new StringBuilder();
        int c;
        while ((c = stream.read()) != -1) {
            retStr.append((char) c);
        }
        stream.close();
        return retStr.toString();
    }

    /**
     * Checks whether a file exists.
     *
     * @param cmdPath The file path to be checked.
     * @return <code>true</code> if the file exists, <code>false</code> otherwise.
     */
    public static boolean fileExist(String cmdPath) {
        return new File(cmdPath).exists();
    }

    /**
     * Copy file from one destination to another.
     * @param in The source file.
     * @param out The destination.
     * @throws IOException If an I/O exception occurs.
     */
    public static void copyFile(File in, File out) throws IOException {
        try (FileInputStream fin = new FileInputStream(in);
                FileChannel inChannel = fin.getChannel();
                FileOutputStream fos = new FileOutputStream(out);
                FileChannel outChannel = fos.getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }
}
