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

package org.eclipse.linuxtools.systemtap.structures;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.internal.systemtap.structures.Localization;
import org.eclipse.linuxtools.systemtap.structures.listeners.IGobblerListener;

/**
 * A utility for saving script output to a log file as it runs.
 */
public class LoggingStreamDaemon implements IGobblerListener {
    private static final int BUFFER_SIZE = 1024;
    private static final Set<LoggingStreamDaemon> allLogs = new HashSet<>();

    protected StringBuilder output;
    protected File outputFile;
    protected FileWriter writer;
    private boolean saveLog = false;

    /**
     * Sets up a new logger. Log contents will be saved to a temporary file
     * until {@link #saveLog} is used to save the log to a specified file.
     */
    public LoggingStreamDaemon() {
        output = new StringBuilder();
        try {
            outputFile = File.createTempFile(this.toString(), ".tmp"); //$NON-NLS-1$
            writer = new FileWriter(outputFile, true);
        } catch (IOException ioe) {
            outputFile = null;
            writer = null;
        }
    }

    /**
     * Pushes output to log.
     */
    private void pushData() {
        try {
            // Recreate the log if it was deleted
            if (!outputFile.exists()) {
                startRestoredLog();
            }
            writer.write(output.toString());
            output.setLength(0);
            writer.flush();
        } catch (IOException ioe) {}
    }

    /**
     * Outputs one line.
     */
    @Override
    public void handleDataEvent(String line) {
        if (isReady()) {
            output.append(line);
            pushData();
        }
    }

    /**
     * Reads in and returns the output produced.
     * @return The logged data.
     */
    public String getOutput() {
        if (!isReady()) {
            return null;
        }
        if (output.length() > 0) {
            pushData();
        }
        try (FileReader reader = new FileReader(outputFile)) {
            char[] buffer = new char[BUFFER_SIZE];
            int count;
            StringBuilder builder = new StringBuilder();
            while (-1 != (count = reader.read(buffer))) {
                builder.append(buffer, 0, count);
            }
            return builder.toString();
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Sets the logging stream to be continuously saved to a file.
     * @param file The file to save the log data to. Must not be <code>null</code>.
     * @return <code>true</code> if the save was successful, <code>false</code> otherwise.
     */
    public boolean saveLog(File file) {
        if (!isReady()) {
            return false;
        }
        // If saving to the same file that's already being saved to,
        // either do nothing if it exists, or restore it if it doesn't.
        if (file.equals(outputFile)) {
            if (!outputFile.exists()) {
                try {
                    startRestoredLog();
                } catch (IOException e) {
                    return false;
                }
            }
            return true;
        }
        // If saving to a file used by another active log,
        // quit to avoid write conflicts.
        for (LoggingStreamDaemon log : allLogs) {
            if (!log.equals(this) && file.equals(log.outputFile)) {
                return false;
            }
        }

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter w = new FileWriter(file, false);
            try (FileReader r = new FileReader(outputFile)) {
                char[] buffer = new char[BUFFER_SIZE];
                int count;
                while (-1 != (count = r.read(buffer))) {
                    w.write(new String(buffer, 0, count));
                }
            }
            w.flush();
            writer.close();
            writer = w;
        } catch (IOException ioe) {
            return false;
        }
        outputFile.delete();
        outputFile = file;
        saveLog = true;
        allLogs.add(this);
        return true;
    }

    private void startRestoredLog() throws IOException {
        outputFile.createNewFile();
        output.insert(0, Localization.getString("LoggingStreamDaemon.ResumedLog") + '\n'); //$NON-NLS-1$
        writer.close();
        writer = new FileWriter(outputFile, false);
    }

    public void dispose() {
        if (outputFile != null) {
            if (!saveLog) {
                outputFile.delete();
            }
            outputFile = null;
        }

        if (writer != null) {
            try {
                writer.close();
            } catch(IOException ioe) {}
            writer = null;
        }

        if (output != null) {
            output.setLength(0);
            output = null;
        }

        allLogs.remove(this);
    }

    private boolean isReady() {
        return writer != null && outputFile != null;
    }
}
