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

package org.eclipse.linuxtools.internal.callgraph.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Helper class parses the given string for recognizable error messages
 *
 */
public class SystemTapErrorHandler {

    public static final String FILE_PROP = "errors.prop"; //$NON-NLS-1$
    private final int LINE_LIMIT = 300;
    private boolean errorRecognized;
    private StringBuilder errorMessage = new StringBuilder(""); //$NON-NLS-1$
    private StringBuilder logContents;

    public SystemTapErrorHandler() {
        errorRecognized = false;
        errorMessage.append(Messages
             .getString("SystemTapErrorHandler.ErrorMessage") + //$NON-NLS-1$
             Messages.getString("SystemTapErrorHandler.ErrorMessage1")); //$NON-NLS-1$
        logContents = new StringBuilder();
    }

    /**
     * Search given string for recognizable error messages. Can append the
     * contents of the string to the error log if writeToLog() or
     * finishHandling() are called.
     * @param doc
     */
    public void handle(IProgressMonitor m, String errors) {
        String[] errorsList = errors.split("\n"); //$NON-NLS-1$

        // READ FROM THE PROP FILE AND DETERMINE TYPE OF ERROR
        File file = new File(PluginConstants.getPluginLocation() + FILE_PROP);
        try (BufferedReader buff1 = new BufferedReader(new FileReader(file))) {
            String line;
            for (String message : errorsList) {
				try (BufferedReader innerBuff = new BufferedReader(
						new FileReader(file))) {
					while ((line = innerBuff.readLine()) != null) {
						if (m != null && m.isCanceled()) {
							return;
						}
						int index = line.indexOf('=');
						Pattern pat = Pattern.compile(line.substring(0, index),
								Pattern.DOTALL);
						Matcher matcher = pat.matcher(message);

						if (matcher.matches()) {
							if (!isErrorRecognized()) {
								// First error
								errorMessage
										.append(Messages
												.getString("SystemTapErrorHandler.ErrorMessage2")); //$NON-NLS-1$
								setErrorRecognized(true);
							}
							String errorFound = line.substring(index + 1);

							if (!errorMessage.toString().contains(errorFound)) {
								errorMessage.append(errorFound
										+ PluginConstants.NEW_LINE);
							}
							break;
						}
					}
				}
            }

            logContents.append(errors);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
   
    /**
     * Append to the log contents
     */
    public void appendToLog (String header){
        logContents.append(header);
    }

    /**
     * Handle the error.
     *
     * @param m
     * @param f Temporary error file
     * @throws IOException
     */
    public void handle(IProgressMonitor m, FileReader f) throws IOException {
        BufferedReader br = new BufferedReader(f);

        String line;
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        while ((line = br.readLine()) != null) {
            counter++;
            builder.append(line + "\n"); //$NON-NLS-1$
            if (m != null && m.isCanceled()) {
                return;
            }
            if (counter == LINE_LIMIT) {
                handle(m, builder.toString());
                builder = new StringBuilder();
                counter = 0;
            }
        }
        handle(m, builder.toString());
    }

    /**
     * Run this method when there are no more error messages to handle. Creates
     * the error pop-up message and writes to log.Currently relaunch only works 
     * for the callgraph script.
     */
    public void finishHandling(IProgressMonitor m, String scriptPath) {
        if (!isErrorRecognized()) {
            errorMessage.append(Messages.getString("SystemTapErrorHandler.NoErrRecognized") + //$NON-NLS-1$
                    Messages.getString("SystemTapErrorHandler.NoErrRecognizedMsg")); //$NON-NLS-1$
        }

        writeToLog();
    }
   
    /**
     * Writes the contents of logContents to the error log, along with date and
     * time.
     */
    public void writeToLog() {
    	IStatus status = new Status(IStatus.ERROR,CallgraphCorePlugin.PLUGIN_ID,logContents.toString());
    	CallgraphCorePlugin.getDefault().getLog().log(status);
    	
        logContents = new StringBuilder();
    }
   
    /**
     * Delete the log at File and replace it with a new (empty) file
     * @param log The File object for the log file to delete and refresh
     */
    public static void deleteLog(File log) {
        log.delete();
        try {
            log.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    /**
     * @return Returns true if an error matches one of the regex's in error.prop
     * and false otherwise.
     */
    public boolean isErrorRecognized() {
        return errorRecognized;
    }

    /**
     * Convenience method to change the error recognition value.
     * @param errorsRecognized True if the handler recognizes some error
     * and false otherwise.
     */
    private void setErrorRecognized(boolean errorsRecognized) {
        errorRecognized = errorsRecognized;
    }
    
    /**
     * @return The error message string
     */
    public String getErrorMessage(){
    	return errorMessage.toString();
    }
    
    /**
     * @return The string contents of what will be printed to the log
     */
    public String getLogContents(){
    	return logContents.toString();
    }
   
}