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

package org.eclipse.linuxtools.systemtap.local.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class parses the given string for recognizable error messages 
 *
 */
public class SystemTapErrorHandler {
	
	public static final String FILE_PROP = "errors.prop"; //$NON-NLS-1$
	public static final String FILE_ERROR_LOG = "Error.log";
	public static final int MAX_LOG_SIZE = 50000;
	private boolean errorRecognized;
	private String errorMessage = "";
	private String logContents;
	
	/**
	 * Delete the log file and create an empty one
	 */
	public static void delete(){
		File log = new File(PluginConstants.DEFAULT_OUTPUT + FILE_ERROR_LOG); //$NON-NLS-1$
		log.delete();
		try {
			log.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
 
	public SystemTapErrorHandler() {
		errorRecognized = false;
		errorMessage = "";
		logContents = "";
	}

	
	/**
	 * Search given string for recognizable error messages. Can append the contents of 
	 * the string to the error log if writeToLog() or finishHandling() are called.
	 * A call to finishHandling() will also open a popup window with user-friendly messages
	 * corresponding to the recognizable errors.
	 * 
	 * @param doc
	 */
	public void handle (String message){		
		if (errorMessage.length() < 1) {
			errorMessage = 
				Messages.getString("SystemTapErrorHandler.ErrorMessage") + //$NON-NLS-1$
				Messages.getString("SystemTapErrorHandler.ErrorMessage1"); //$NON-NLS-1$
		}
		
		//READ FROM THE PROP FILE AND DETERMINE TYPE OF ERROR
		File file = new File(PluginConstants.PLUGIN_LOCATION+FILE_PROP);
		try {
			BufferedReader buff = new BufferedReader (new FileReader(file));
			String line;
			int index;
			while ((line = buff.readLine()) != null){
				index = line.indexOf('=');
				String matchString = line.substring(0, index);
				Matcher matcher = Pattern.compile(matchString, Pattern.DOTALL).matcher(message);

				if (matcher.matches()) {
					if (!isErrorRecognized()) {
						errorMessage+=Messages.getString("SystemTapErrorHandler.ErrorMessage2"); //$NON-NLS-1$
						setErrorRecognized(true);
					}
						
					errorMessage+=line.substring(index+1) 
					+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE;
				}
			}
			
			logContents += message;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Run this method when there are no more error messages to handle. 
	 * Creates the error pop-up message and writes to log.
	 * 
	 */
	public void finishHandling() {
		if (!isErrorRecognized()) {
			errorMessage+="No recognizable errors detected. " +
					"Please consult error log for more information.";
		}
		
		SystemTapUIErrorMessages mes = new SystemTapUIErrorMessages(
				Messages.getString("SystemTapErrorHandler.ErrorMessageName"), 
				Messages.getString("SystemTapErrorHandler.ErrorMessageTitle"), 
				errorMessage); //$NON-NLS-1$ //$NON-NLS-2$
		mes.schedule();
		
		
		writeToLog();
	}
	
	
	/**
	 * Writes the contents of logContents to the error log, along with date and time.
	 */
	public void writeToLog() {
		File errorLog = new File(PluginConstants.DEFAULT_OUTPUT + "Error.log"); //$NON-NLS-1$

		try {
			//CREATE THE ERROR LOG IF IT DOES NOT EXIST
			//CLEAR THE ERROR LOG AFTER A FIXED SIZE(BYTES)
			if (!errorLog.exists()
					|| errorLog.length() > MAX_LOG_SIZE) {
				errorLog.delete();
					errorLog.createNewFile();
			}
	
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		//APPEND THE ERROR TO THE LOG
		Helper
				.appendToFile(errorLog.getAbsolutePath(),
						Messages.getString("SystemTapErrorHandler.ErrorLogDashes") //$NON-NLS-1$
						+ PluginConstants.NEW_LINE 
						+ day + "/" + month //$NON-NLS-1$
						+ "/" + year + " - " + hour + ":" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ minute + ":" + second //$NON-NLS-1$
						+ PluginConstants.NEW_LINE + logContents
						+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logContents = "";
	}

	/**
	 * Returns true if an error matches one of the regex's in error.prop
	 * @return
	 */
	public boolean isErrorRecognized() {
		return errorRecognized;
	}


	/**
	 * Convenience method to change the error recognition value.
	 * @param errorsRecognized
	 */
	private void setErrorRecognized(boolean errorsRecognized) {
		errorRecognized = errorsRecognized;
	}

}
