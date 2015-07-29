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
package org.eclipse.linuxtools.internal.gprof.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersCSVExporter;
import org.osgi.framework.Bundle;


/**
 * This class only contains some tools to facilitate tests
 * (compare)
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class STJunitUtils {

    public static final String BINARY_FILE = "a.out";
    public static final String OUTPUT_FILE = "gmon.out";
    public static final String DIRECTORY_SUFFIX = "_gprof_input";


    /**
     * Test CSV export of the given view
     * @param view
     * @param dumpFullFileName
     * @param refFullFileName
     */
    public static boolean testCSVExport(AbstractSTDataView view, String dumpFullFileName, String refFullFileName) {
        STDataViewersCSVExporter exporter = new STDataViewersCSVExporter(view.getSTViewer());
        exporter.exportTo(dumpFullFileName, new NullProgressMonitor());
        // compare with ref
        return compareCSVIgnoreEOL(dumpFullFileName, refFullFileName, true);
    }

    /**
     * Utility method to compare files
     * @param dumpFile
     * @param refFile
     * @return
     */
    public static boolean compareIgnoreEOL(String dumpFile, String refFile, boolean deleteDumpFileIfOk) {
        String message = "Comparing ref file ("+refFile+ ")and dump file (" +
          dumpFile+")";
        boolean equals = false;
        try (LineNumberReader is1 = new LineNumberReader(new FileReader(dumpFile));
        LineNumberReader is2 = new LineNumberReader(new FileReader(refFile))){
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

            is1.close();
            is2.close();
            // delete dump only for successful tests
            if (equals && deleteDumpFileIfOk) {
                new File(dumpFile).delete();
            }
        }catch (FileNotFoundException fnfe) {
            message += "... FAILED: One of these files may not exist";
            assertNull(message, fnfe);
        }
        catch (Exception e) {
            message += ": exception raised ... FAILED";
            assertNull(message, e);
        }
        return equals;
    }

    /**
     * Utility method to compare exported CSV files
     * @param dumpFile
     * @param refFile
     * @return
     */
    public static boolean compareCSVIgnoreEOL(String dumpFile, String refFile, boolean deleteDumpFileIfOk) {
        String message = "Comparing ref file ("+refFile+ ")and dump file (" +
          dumpFile+")";
        boolean equals = false;
        String str = "[in-charge]"; // this string can be dumped according to binutils version installed on local machine

        try (LineNumberReader is1 = new LineNumberReader(new FileReader(dumpFile));
        LineNumberReader is2 = new LineNumberReader(new FileReader(refFile))){
            do {
                String line1 = is1.readLine();
                String line2 = is2.readLine();
                int length = str.length();
                if (line1 == null) {
                    if (line2 == null) {
                        equals = true;
                    }
                    break;
                } else if (line1.contains(str)){
                    int idx = line1.indexOf("[in-charge]");
                    char c = line1.charAt(idx -1);
                    if (c == ' ' ){
                        idx--;
                        length++;
                    }
                    line1 = line1.substring(0, idx) + line1.substring(idx+length, line1.length());
                    if (!line1.equals(line2))
                        break;
                } else if (line2 == null || !line1.equals(line2)) {
                    break;
                }
            } while (true);

            is1.close();
            is2.close();
            if (!equals) {
                StringBuffer msg = new StringBuffer(message + ": not correspond ");
                 msg.append("\n========= begin dump file =========\n");
                try (FileReader fr = new FileReader(dumpFile)) {
                    int c;
                    while ((c = fr.read()) != -1) {
                        msg.append((char) c);
                    }
                }
                 msg.append("\n=========  end dump file  =========\n");
                 assertEquals(msg.toString(), true, false);
            }

            // delete dump only for successful tests
            if (equals && deleteDumpFileIfOk) {
                new File(dumpFile).delete();
            }
        }catch (FileNotFoundException e) {
            message += "... FAILED: One of these files may not exist";
            assertNull(message, e);
        }
        catch (Exception e) {
            message += ": exception raised ... FAILED";
            assertNull(message, e);
        }
        return equals;
    }

    /**
     * Utility method to compare Input streams
     * @param ISdump
     * @param ISref
     * @return
     * @throws IOException
     */
    public static boolean compare(InputStream ISdump, InputStream ISref) throws IOException {
        try {
            boolean equals = false;
            do {
                int char1 = ISdump.read();
                int char2 = ISref.read();
                if (char1 != char2)
                    break;
                if (char1 == -1) {
                    equals = true;
                    break;
                }
            } while (true);
            return equals;
        } finally {
            ISdump.close();
            ISref.close();
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
            assertNotNull("Problem locating " + relativeName + " in" + pluginId,e);
        }
        String filename = url.getFile();
        return filename;
    }

    /**
     * Utility method
     * @return the list of directories that belong to the pluginDirectory and
     * ends with the given extensionSuffix
     */
    public static File[] getTestDirs() {
        // load directories containing tests
        String filename = getAbsolutePath("org.eclipse.linuxtools.gprof.test", ".");
        File dir = new File(filename);
        File[] testDirs = dir.listFiles(
            new FileFilter() {
                @Override
                public boolean accept(File arg0) {
                    return (arg0.isDirectory() && arg0.getName().matches(".*" + DIRECTORY_SUFFIX));
                }
            }
        );

        // test if there is any directory samples
        assertNotNull("No project files to test",testDirs);
        return testDirs;
    }
}
