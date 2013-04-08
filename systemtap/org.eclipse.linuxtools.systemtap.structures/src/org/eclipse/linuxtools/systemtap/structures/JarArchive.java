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

package org.eclipse.linuxtools.systemtap.structures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarArchive {
	public static void unjarFiles(String jarFileName, String destination) {
		unjarFiles(jarFileName, destination, null);
	}

	/**
	 * Un-jars a specified file to a specified directory using a specificed path filter.
	 *
	 * @param jarFileName The file to extract.
	 * @param destination Where to extract the files to.
	 * @param pathFilter The path filter to apply.
	 */
	public static void unjarFiles(String jarFileName, String destination, String pathFilter) {
		try {
			JarFile jf = new JarFile(jarFileName);

			for (Enumeration<?> entries = jf.entries(); entries.hasMoreElements();) {
				JarEntry jarEntry = (JarEntry)entries.nextElement();
				String jarEntryName = jarEntry.getName();

				if(null == pathFilter || jarEntryName.contains(pathFilter)) {
				int lastDirSep;
					if ( (lastDirSep = jarEntryName.lastIndexOf('/')) > 0 ) {
						String dirName = jarEntryName.substring(0, lastDirSep);
						(new File(destination + dirName)).mkdirs();
					}

					if (!jarEntryName.endsWith("/")) { //$NON-NLS-1$
						OutputStream out = new FileOutputStream(destination + jarEntryName);
						InputStream in = jf.getInputStream(jarEntry);

						transferData(in, out);

						out.close();
						in.close();
					}
				}
			}
		} catch (IOException e) {}
	}

	/**
	 * Transfer data from one stream to another.
	 *
	 * @param in The stream to transfer from.
	 * @param out The stream to transfer to.
	 */
	private static void transferData(InputStream in, OutputStream out) {
		try {
			byte[] buf = new byte[BUFFER_SIZE];
			int len;
			while((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
		} catch (IOException e) {}
	}

	private static final int BUFFER_SIZE = 1024;
}
