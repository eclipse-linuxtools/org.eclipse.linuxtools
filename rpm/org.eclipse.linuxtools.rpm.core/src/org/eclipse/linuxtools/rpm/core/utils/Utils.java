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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

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
	public static BufferedInputStream runCommandToInputStream(String... command)
			throws IOException {
		BufferedInputStream in = null;
		ProcessBuilder pBuilder = new ProcessBuilder(command);
		pBuilder = pBuilder.redirectErrorStream(true);
		Process child = pBuilder.start();
		in = new BufferedInputStream(child.getInputStream());
		return in;
	}

	/**
	 * Runs the given command and parameters.
	 * 
	 * @param outStream
	 *            The stream to write the output to.
	 * 
	 * @param command
	 *            The command with all parameters.
	 * @throws IOException If an IOException occurs.
	 */
	public static void runCommand(final OutputStream outStream,
			String... command) throws IOException {
		ProcessBuilder pBuilder = new ProcessBuilder(command);
		pBuilder = pBuilder.redirectErrorStream(true);
		Process child = pBuilder.start();
		final BufferedInputStream in = new BufferedInputStream(child
				.getInputStream());
		Job readinJob = new Job("") { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					int i;
					while ((i = in.read()) != -1) {
						outStream.write(i);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}

		};
		readinJob.schedule();
		try {
			child.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public static String runCommandToString(String... command)
			throws IOException {
		BufferedInputStream in = runCommandToInputStream(command);
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
	public static String inputStreamToString(InputStream stream)
			throws IOException {
		String retStr = ""; //$NON-NLS-1$
		int c;
		while ((c = stream.read()) != -1) {
			retStr += ((char) c);
		}
		stream.close();
		return retStr;
	}
}
