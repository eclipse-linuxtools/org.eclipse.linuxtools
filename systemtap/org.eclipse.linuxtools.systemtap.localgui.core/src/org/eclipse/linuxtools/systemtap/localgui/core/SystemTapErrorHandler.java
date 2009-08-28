package org.eclipse.linuxtools.systemtap.localgui.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;

public class SystemTapErrorHandler {
	
	public static final String errorPropFile = "errors.prop"; //$NON-NLS-1$
	
	/**
	 * Find and output error messages corresponding to the document text.
	 * 
	 * @param doc
	 */
	public static void handle (IDocument doc){
		String errorMessage = Messages.getString("SystemTapErrorHandler.ErrorMessage") + //$NON-NLS-1$
				Messages.getString("SystemTapErrorHandler.ErrorMessage1") + //$NON-NLS-1$
				Messages.getString("SystemTapErrorHandler.ErrorMessage2"); //$NON-NLS-1$
		String contents = doc.get();
		
		File file = new File(PluginConstants.PLUGIN_LOCATION+errorPropFile);
		try {
			BufferedReader buff = new BufferedReader (new FileReader(file));
			String line;
			int index;
			while ((line = buff.readLine()) != null){
				index = line.indexOf('=');
				String matchString = line.substring(0, index);
				Matcher matcher = Pattern.compile(matchString, Pattern.DOTALL).matcher(contents);

				if (matcher.matches()) {
					errorMessage+=line.substring(index+1) 
					+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE;
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		SystemTapUIErrorMessages mes = new SystemTapUIErrorMessages(
				Messages.getString("SystemTapErrorHandler.ErrorMessageName"), Messages.getString("SystemTapErrorHandler.ErrorMessageTitle"), errorMessage); //$NON-NLS-1$ //$NON-NLS-2$
		mes.schedule();
		File errorLog = new File(PluginConstants.DEFAULT_OUTPUT + "Error.log"); //$NON-NLS-1$

		try {
			//CREATE THE ERROR LOG IF IT DOES NOT EXIST
			//CLEAR THE ERROR LOG AFTER A FIXED SIZE(BYTES)
			if (!errorLog.exists()
					|| errorLog.length() > PluginConstants.MAX_LOG_SIZE) {
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
						+ PluginConstants.NEW_LINE + doc.get()
						+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
