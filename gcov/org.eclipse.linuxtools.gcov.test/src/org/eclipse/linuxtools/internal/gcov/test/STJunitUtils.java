/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

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
    public static void compareIgnoreEOL(String dumpFile, String refFile, boolean deleteDumpFileIfOk) {
        String message = "Comparing ref file (" + refFile + ") and dump file ("
                + dumpFile + ")";
        try {
            assertEquals(message, readFile(refFile), readFile(dumpFile));

            // delete dump only for successful tests
            if (deleteDumpFileIfOk) {
                new File(dumpFile).delete();
            }
        } catch (Exception e) {
            fail(message + ": exception raised ... FAILED");
        }
    }

    private static String readFile(String file) throws IOException {
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = lnr.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            fail("FAILED: file " + file + " does not exist");
            return "";
        }
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
            assertNotNull(e, "Problem locating " + relativeName + " in" + pluginId);
        }
        String filename = url.getFile();
        return filename;
    }
}
