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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Helper class parses the given string for recognizable error messages 
 *
 */
public class SystemTapErrorHandler {
	
	public static final String FILE_PROP = "errors.prop"; //$NON-NLS-1$
	public static final String FILE_ERROR_LOG = "Error.log"; //$NON-NLS-1$
	public static final int MAX_LOG_SIZE = 50000;
	private boolean errorRecognized;
	private String errorMessage = ""; //$NON-NLS-1$
	private StringBuilder logContents;
	private boolean mismatchedProbePoints;
	ArrayList<String> functions = new ArrayList<String>();
	
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
		mismatchedProbePoints = false;
		errorRecognized = false;
		if (errorMessage.length() < 1) {
			errorMessage = 
				Messages.getString("SystemTapErrorHandler.ErrorMessage") + //$NON-NLS-1$
				Messages.getString("SystemTapErrorHandler.ErrorMessage1"); //$NON-NLS-1$
		}
		
		logContents = new StringBuilder(); //$NON-NLS-1$
	}

	
	/**
	 * Search given string for recognizable error messages. Can append the contents of 
	 * the string to the error log if writeToLog() or finishHandling() are called.
	 * A call to finishHandling() will also open a popup window with user-friendly messages
	 * corresponding to the recognizable errors.
	 * 
	 * @param doc
	 */
	public void handle (IProgressMonitor m, String errors){	
		String[] blah = errors.split("\n"); //$NON-NLS-1$

		//READ FROM THE PROP FILE AND DETERMINE TYPE OF ERROR
		File file = new File(PluginConstants.PLUGIN_LOCATION+FILE_PROP);
		try {
			BufferedReader buff = new BufferedReader (new FileReader(file));
			String line;
			int index;
			
			for (String message : blah) {
				boolean firstLine = true; //Keep the error about mismatched probe points first
				buff = new BufferedReader (new FileReader(file));
				while ((line = buff.readLine()) != null){
					if (m != null && m.isCanceled())
						return;
					index = line.indexOf('=');
					String matchString = line.substring(0, index);
					Pattern pat = Pattern.compile(matchString, Pattern.DOTALL);
					Matcher matcher = pat.matcher(message);
	
					
					if (matcher.matches()) {
						if (!isErrorRecognized()) {
							errorMessage+=Messages.getString("SystemTapErrorHandler.ErrorMessage2"); //$NON-NLS-1$
							setErrorRecognized(true);
						}
							
						errorMessage+=line.substring(index+1) 
						+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE;
					
						if (firstLine) {
							findFunctions(m, message, pat);
							mismatchedProbePoints = true;
						}
						break;
					}
					firstLine = false;
				}
				buff.close();
			}
			
			logContents.append(errors);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void handle (IProgressMonitor m, FileReader f) throws IOException {
		BufferedReader br = new BufferedReader (f);
		
		String line;
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		while ( (line = br.readLine()) != null) {
			counter++;
			builder.append(line);
			builder.append("\n"); //$NON-NLS-1$
			if (m != null && m.isCanceled())
				return;
			if (counter == 300) {
				handle(m, builder.toString());
				builder = new StringBuilder();
				counter = 0;
			}
		}
		handle(m, builder.toString());

	}

	/**
	 * Run this method when there are no more error messages to handle. 
	 * Creates the error pop-up message and writes to log.
	 * 
	 */
	public void finishHandling(IProgressMonitor m, int numberOfErrors) {
		if (!isErrorRecognized()) {
			errorMessage+=Messages.getString("SystemTapErrorHandler.4") + //$NON-NLS-1$
					Messages.getString("SystemTapErrorHandler.5"); //$NON-NLS-1$
		}
		
		writeToLog();
		
		if (mismatchedProbePoints){
			if (numberOfErrors > PluginConstants.MAX_ERRORS) {
				errorMessage = PluginConstants.NEW_LINE + PluginConstants.NEW_LINE 
				+ Messages.getString("SystemTapErrorHandler.TooManyErrors1") + numberOfErrors +Messages.getString("SystemTapErrorHandler.TooManyErrors2") + //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("SystemTapErrorHandler.TooManyErrors3") + //$NON-NLS-1$
				Messages.getString("SystemTapErrorHandler.TooManyErrors4"); //$NON-NLS-1$
				SystemTapUIErrorMessages mes = new SystemTapUIErrorMessages(
						Messages.getString("SystemTapErrorHandler.ErrorMessageName"),  //$NON-NLS-1$
						Messages.getString("SystemTapErrorHandler.ErrorMessageTitle"),  //$NON-NLS-1$
						errorMessage); //$NON-NLS-1$ //$NON-NLS-2$
				mes.schedule();
				m.setCanceled(true);
				return;
			}
			
			
			StringBuffer resultFileContent = new StringBuffer();
			String fileLocation = PluginConstants.DEFAULT_OUTPUT + "callgraphGen.stp"; //$NON-NLS-1$
			String line;
			boolean skip = false;
			File file = new File(fileLocation);
			try {
				BufferedReader buff = new BufferedReader(new FileReader(file));
				while ((line = buff.readLine()) != null){
					if (m != null && m.isCanceled())
						return;
					skip =  false;
					for (String func : functions){
						if (line.contains("function(\"" + func + "\").call")){ //$NON-NLS-1$ //$NON-NLS-2$
							skip = true;
							break;
						}
					}
					
					if (!skip && !line.equals("\n")){							 //$NON-NLS-1$
						resultFileContent.append(line);
						resultFileContent.append("\n"); //$NON-NLS-1$
					}
				}
				
				buff.close();
				
				BufferedWriter wbuff= new BufferedWriter(new FileWriter(file));
				wbuff.write(resultFileContent.toString());
				wbuff.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else{
			SystemTapUIErrorMessages mes = new SystemTapUIErrorMessages(
					Messages.getString("SystemTapErrorHandler.ErrorMessageName"),  //$NON-NLS-1$
					Messages.getString("SystemTapErrorHandler.ErrorMessageTitle"),  //$NON-NLS-1$
					errorMessage); //$NON-NLS-1$ //$NON-NLS-2$
			mes.schedule();
		}
		
		
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
		
		logContents = new StringBuilder(); //$NON-NLS-1$
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


	public boolean hasMismatchedProbePoints() {
		return mismatchedProbePoints;
	}


	public void setMismatchedProbePoints(boolean mismatchedProbePoints) {
		this.mismatchedProbePoints = mismatchedProbePoints;
	}
	
	
	public void findFunctions(IProgressMonitor m, String message, Pattern pat) {
		String[] list = message.split("\n"); //$NON-NLS-1$
		String result;
		for (String s : list) {
			if (m.isCanceled())
				return;
			if (pat.matcher(s).matches()) {
				int lastQuote = s.lastIndexOf('"');
				if (lastQuote < 0)
					return;
				int secondLastQuote = s.lastIndexOf('"', lastQuote - 1);
				if (secondLastQuote < 0)
					return;
				result = s.substring(secondLastQuote+1, lastQuote);
				if (!functions.contains(result))
					functions.add(result);
			}
		}
	}


	public ArrayList<String> getFunctions() {
		return functions;
	}

}
