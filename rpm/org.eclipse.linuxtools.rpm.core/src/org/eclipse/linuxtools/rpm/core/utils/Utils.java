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
package org.eclipse.linuxtools.rpm.core.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
	
	/**
	 * Runs the given command and parameters.
	 * 
	 * @param command The command with all parameters.
	 * @return Stream containing the combined content of stderr and stdout. 
	 * @throws IOException
	 */
	public static BufferedInputStream runCommandToInputStream(String... command)
			throws IOException {
		BufferedInputStream in = null;
		ProcessBuilder pBuilder = new ProcessBuilder(command);
		pBuilder = pBuilder.redirectErrorStream(true);
		Process child = pBuilder.start();
		try {
			child.waitFor();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		in = new BufferedInputStream(child.getInputStream());
		return in;
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
	 * Reads the content of the given InputStream and returns its textual representation.
	 * @param stream The InputStream to read.
	 * @return Textual content of the stream.
	 * @throws IOException
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
