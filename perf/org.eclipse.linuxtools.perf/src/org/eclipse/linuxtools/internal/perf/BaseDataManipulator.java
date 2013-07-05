/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base class providing basic functionality for stream processing.
 */
public class BaseDataManipulator {

	/**
	 * Get contents of specified BufferedReader as a String
	 *
	 * @param buffer BufferedReader to read from.
	 * @return String contents of BufferedReader.
	 */
	public String getBufferContents(BufferedReader buffer) {
		if (buffer == null) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder fileStr = new StringBuilder();
		try {
			String line;

			while ((line = buffer.readLine()) != null) {
				fileStr.append(line);
				fileStr.append("\n"); //$NON-NLS-1$
			}

		} catch (IOException e) {
			PerfPlugin.getDefault().openError(e, ""); //$NON-NLS-1$
		}

		return fileStr.toString();
	}

	/**
	 * Copy contents of File src into Files dest
	 *
	 * @param src File source file to copy from.
	 * @param dest File destination files to copy to.
	 */
	public void copyFile(File src, File dest) {
		InputStream destInput = null;
		OutputStream srcOutput = null;
		try {
			destInput = new FileInputStream(src);
			srcOutput = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = destInput.read(buffer)) != -1) {
				srcOutput.write(buffer, 0, length);
			}
		} catch (FileNotFoundException e) {
			PerfPlugin.getDefault().openError(e, ""); //$NON-NLS-1$
		} catch (IOException e) {
			PerfPlugin.getDefault().openError(e, ""); //$NON-NLS-1$
		} finally {
			closeResource(destInput);
			closeResource(srcOutput);
		}

	}

	/**
	 * Utility method to get all file contents as a String.
	 * @param file File to read.
	 * @return String file contents.
	 */
	public String fileToString(File file) {
		if (file == null | !file.exists()) {
			return ""; //$NON-NLS-1$
		}
		BufferedReader fileReader = null;
		String result = ""; //$NON-NLS-1$
		try {
			fileReader = new BufferedReader(new FileReader(file));
			result = getBufferContents(fileReader);
		} catch (FileNotFoundException e) {
		} finally {
			closeResource(fileReader);
		}
		return result;
	}

	/**
	 * Close specified resource
	 *
	 * @param resrc resource to close
	 */
	public void closeResource(Closeable resrc) {
		if (resrc != null) {
			try {
				resrc.close();
			} catch (IOException e) {
				PerfPlugin.getDefault().openError(e, ""); //$NON-NLS-1$
			}
		}
	}
}
