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

package org.eclipse.linuxtools.callgraph.core;

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
	private StringBuilder errorMessage = new StringBuilder(""); //$NON-NLS-1$
	private StringBuilder logContents;
	private boolean mismatchedProbePoints;
	ArrayList<String> functions = new ArrayList<String>();

	/**
	 * Delete the log file and create an empty one
	 */
	public static void delete() {
		File log = new File(PluginConstants.getDefaultOutput() + FILE_ERROR_LOG); //$NON-NLS-1$
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
			errorMessage.append(Messages
					.getString("SystemTapErrorHandler.ErrorMessage") + //$NON-NLS-1$
					Messages.getString("SystemTapErrorHandler.ErrorMessage1")); //$NON-NLS-1$
		}

		logContents = new StringBuilder(); //$NON-NLS-1$
	}

	/**
	 * Search given string for recognizable error messages. Can append the
	 * contents of the string to the error log if writeToLog() or
	 * finishHandling() are called. A call to finishHandling() will also open a
	 * popup window with user-friendly messages corresponding to the
	 * recognizable errors.
	 * 
	 * @param doc
	 */
	public void handle(IProgressMonitor m, String errors) {
		String[] errorsList = errors.split("\n"); //$NON-NLS-1$

		// READ FROM THE PROP FILE AND DETERMINE TYPE OF ERROR
		File file = new File(PluginConstants.getPluginLocation() + FILE_PROP);
		try {
			BufferedReader buff = new BufferedReader(new FileReader(file));
			String line;
			int index;

			for (String message : errorsList) {
				boolean firstLine = true; // Keep the error about mismatched
											// probe points first
				buff = new BufferedReader(new FileReader(file));
				while ((line = buff.readLine()) != null) {
					if (m != null && m.isCanceled())
						return;
					index = line.indexOf('=');
					Pattern pat = Pattern.compile(line.substring(0, index),Pattern.DOTALL);
					Matcher matcher = pat.matcher(message);

					if (matcher.matches()) {
						if (!isErrorRecognized()) {
							errorMessage.append(Messages.getString("SystemTapErrorHandler.ErrorMessage2")); //$NON-NLS-1$
							setErrorRecognized(true);
						}
						String errorFound = line.substring(index+1);

						if (!errorMessage.toString().contains(errorFound)) {
							errorMessage.append(errorFound+ PluginConstants.NEW_LINE);
						}

						//first line in error properties is mismatched probes
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
	
	
	/**
	 * Append to the log contents
	 */
	public void appendToLog (String header){
		logContents.append(header);
	}
	

	/**
	 * Handle the error. Intended to work with a FileReader for a temporary error file.
	 * 
	 * @param m
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public String handle(IProgressMonitor m, FileReader f) throws IOException {
		BufferedReader br = new BufferedReader(f);

		String line;
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		while ((line = br.readLine()) != null) {
			counter++;
			builder.append(line);
			builder.append("\n"); //$NON-NLS-1$
			if (m != null && m.isCanceled())
				return ""; //$NON-NLS-1$
			if (counter == 300) {
				handle(m, builder.toString());
				builder = new StringBuilder();
				counter = 0;
			}
		}
		handle(m, builder.toString());
		return errorMessage.toString();
	}

	/**
	 * Run this method when there are no more error messages to handle. Creates
	 * the error pop-up message and writes to log. Returns true if a relaunch should
	 * be attempted. Currently relaunch only works for the callgraph script.
	 * 
	 */
	public boolean finishHandling(IProgressMonitor m, String scriptPath) {
		if (!isErrorRecognized()) {
			errorMessage.append(Messages.getString("SystemTapErrorHandler.4") + //$NON-NLS-1$
					Messages.getString("SystemTapErrorHandler.5")); //$NON-NLS-1$
		}

		writeToLog();

		if (mismatchedProbePoints) {
			if (functions.size() > PluginConstants.MAX_ERRORS) {
				errorMessage.setLength(0);
				errorMessage
						.append(PluginConstants.NEW_LINE
								+ Messages
										.getString("SystemTapErrorHandler.TooManyErrors1") + functions.size() + Messages.getString("SystemTapErrorHandler.TooManyErrors2") + //$NON-NLS-1$ //$NON-NLS-2$
								Messages
										.getString("SystemTapErrorHandler.TooManyErrors3") + //$NON-NLS-1$
								Messages
										.getString("SystemTapErrorHandler.TooManyErrors4")); //$NON-NLS-1$
				SystemTapUIErrorMessages mes = new SystemTapUIErrorMessages(
						Messages
								.getString("SystemTapErrorHandler.ErrorMessageName"), //$NON-NLS-1$
						Messages
								.getString("SystemTapErrorHandler.ErrorMessageTitle"), //$NON-NLS-1$
						errorMessage.toString());
				mes.schedule();
				m.setCanceled(true);
				return false;
			}

			return cleanScript(m, new File(scriptPath));
			

		}
		return false;

	}

	
	private boolean cleanScript(IProgressMonitor m, File script) {
		StringBuilder resultFileContent = new StringBuilder();
		int counter = 0;
		String line;
		try {
			BufferedReader buff = new BufferedReader(new FileReader(script));
			while ((line = buff.readLine()) != null) {
				if (m != null && m.isCanceled())
					return false;
				boolean skip = false;
				for (String func : functions) {
					if (line.contains("function(\"" + func + "\").call")) { //$NON-NLS-1$ //$NON-NLS-2$
						skip = true;
						counter++;
//						if (counter == functions.size()) {
//							buff.close();
//							return false;
//						}
						break;
					}
				}
				

				if (!skip && !line.equals("\n")) { //$NON-NLS-1$
					//This works only because call and return are on the same line.
					resultFileContent.append(line);
					resultFileContent.append("\n"); //$NON-NLS-1$
				}
			}

			buff.close();

			BufferedWriter wbuff = new BufferedWriter(new FileWriter(script));
			wbuff.write(resultFileContent.toString());
			wbuff.close();
			
			return true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Writes the contents of logContents to the error log, along with date and
	 * time.
	 */
	public void writeToLog() {
		File errorLog = new File(PluginConstants.getDefaultOutput() + "Error.log"); //$NON-NLS-1$

		try {
			// CREATE THE ERROR LOG IF IT DOES NOT EXIST
			// CLEAR THE ERROR LOG AFTER A FIXED SIZE(BYTES)
			if (!errorLog.exists() || errorLog.length() > MAX_LOG_SIZE) {
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

			// APPEND THE ERROR TO THE LOG
			Helper.appendToFile(errorLog.getAbsolutePath(), Messages
					.getString("SystemTapErrorHandler.ErrorLogDashes") //$NON-NLS-1$
					+ PluginConstants.NEW_LINE
					+ day
					+ "/" + month //$NON-NLS-1$
					+ "/" + year + " - " + hour + ":" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ minute
					+ ":" + second //$NON-NLS-1$
					+ PluginConstants.NEW_LINE
					+ logContents
					+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);
		} catch (IOException e) {
			e.printStackTrace();
		}

		logContents = new StringBuilder(); //$NON-NLS-1$
	}

	/**
	 * Returns true if an error matches one of the regex's in error.prop
	 * 
	 * @return
	 */
	public boolean isErrorRecognized() {
		return errorRecognized;
	}

	/**
	 * Convenience method to change the error recognition value.
	 * 
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
		String result;
			if (m.isCanceled())
				return;
				int lastQuote = message.lastIndexOf('"');
				if (lastQuote < 0)
					return;
				int secondLastQuote = message.lastIndexOf('"', lastQuote - 1);
				if (secondLastQuote < 0)
					return;
				result = message.substring(secondLastQuote + 1, lastQuote);
				if (!functions.contains(result))
					functions.add(result);
	}

	public ArrayList<String> getFunctions() {
		return functions;
	}

}
