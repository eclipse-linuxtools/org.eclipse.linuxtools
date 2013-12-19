/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;


/**
 * This class only contains some tools to facilitate tests
 * (compare)
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class STJunitUtils {

	/**
	 * Utility method to compare files
	 * @param dumpFile
	 * @param refFile
	 * @return
	 */
	public static boolean compareIgnoreEOL(String dumpFile, String refFile, boolean deleteDumpFileIfOk) {
		String message = "Comparing ref file (" + refFile + ")and dump file ("
				+ dumpFile + ")";
		boolean equals = false;
		try (LineNumberReader is1 = new LineNumberReader(new FileReader(
				dumpFile));
				LineNumberReader is2 = new LineNumberReader(new FileReader(
						refFile))) {
			do {
				String line1 = is1.readLine();
				String line2 = is2.readLine();
				if (line1 == null) {
					if (line2 == null) {
						equals = true;
					}
					break;
				} else if (line2 == null || !line1.equals(line2)) {
					break;
				}
			} while (true);

			if (!equals) {
				assertEquals(message + ": not correspond ", true, false);
			}

			// delete dump only for successful tests
			if (equals && deleteDumpFileIfOk) {
				new File(dumpFile).delete();
			}
		} catch (FileNotFoundException _) {
			message += "... FAILED: One of these files may not exist";
			assertNull(message, _);
		} catch (Exception e) {
			message += ": exception raised ... FAILED";
			assertNull(message, e);
		}
		return equals;
	}

	/**
	 * Gets the absolute path of a resource in the given plugin
	 * @param pluginId
	 * @param relativeName
	 * @return an absolute path to a file
	 */
	public static String getAbsolutePath(String pluginId, String relativeName) {
		Bundle b = Platform.getBundle(pluginId);
		URL url = FileLocator.find(b, new Path(relativeName), null);
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			assertNotNull("Problem locating " + relativeName + " in" + pluginId,e);
		}
		String filename = url.getFile();
		return filename;
	}
}
