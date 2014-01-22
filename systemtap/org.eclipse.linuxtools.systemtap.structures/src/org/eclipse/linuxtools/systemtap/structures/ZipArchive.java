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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipArchive {

	/**
	 * Zips files.
	 *
	 * @param zipFileName The name of the zipped file you wish to make.
	 * @param files The collection of files to zip.
	 * @param names The names of the files you wish to zip.
	 */
	public static void zipFiles(String zipFileName, String[] files, String[] names) {
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName))) {
			for (int i=0; i<files.length; i++) {
				try (FileInputStream in = new FileInputStream(files[i])) {

					out.putNextEntry(new ZipEntry(names[i]));

					transferData(in, out);

					out.closeEntry();
				}
			}
		} catch (IOException e) {}
	}

	/**
	 * Unzips files.
	 *
	 * @param zipFileName The name of the file you wish to unzip.
	 * @param destination The location you wish to unzip files to.
	 */
	public static void unzipFiles(String zipFileName, String destination) {
		try (ZipFile zf = new ZipFile(zipFileName)) {
			for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
				ZipEntry zipEntry = (ZipEntry)entries.nextElement();
				String zipEntryName = zipEntry.getName();

				int lastDirSep;
				if ( (lastDirSep = zipEntryName.lastIndexOf('/')) > 0 ) {
					String dirName = zipEntryName.substring(0, lastDirSep);
					(new File(dirName)).mkdirs();
				}

				if (!zipEntryName.endsWith("/")) { //$NON-NLS-1$
					try (OutputStream out = new FileOutputStream(destination
							+ zipEntryName);
							InputStream in = zf.getInputStream(zipEntry)) {
						transferData(in, out);
					}
				}
			}
		} catch (IOException e) {}
	}

	/**
	 * Compresses files.
	 *
	 * @param outFileName The new compressed file you wish to create.
	 * @param inFileName The file you wish to compress.
	 */
	public static void compressFile(String outFileName, String inFileName) {
		try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(
				outFileName));
				FileInputStream in = new FileInputStream(inFileName)) {
			transferData(in, out);

			out.finish();
		} catch (IOException ioe) {
		}
	}

	/**
	 * Uncompresses files.
	 *
	 * @param outFileName The new uncompressed file you wish to create.
	 * @param inFileName The file you wish to uncompress.
	 */
	public static void uncompressFile(String outFileName, String inFileName) {
		try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(
				inFileName));
				FileOutputStream out = new FileOutputStream(outFileName)) {
			transferData(in, out);
		} catch (IOException e) {
		}
	}

	/**
	 * Transfers data from one stream to another.
	 *
	 * @param in The source stream.
	 * @param out The export stream.
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
