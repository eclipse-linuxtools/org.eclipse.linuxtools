/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.core;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.cdt.autotools.core.AutotoolsPlugin;

// This class would normally extend IErrorParser and use the CDT error parser
// extension.  However, we want an extended IMarker that contains library info and
// possibly other data in the future.  The standard CDT ErrorParserManager doesn't allow
// us to pass an extended ProblemMarkerInfo, so we are forced to have our own mechanism
// which is similar to the CDT one.
public class ErrorParser extends MarkerGenerator {

	public static final String ID = AutotoolsPlugin.PLUGIN_ID + ".errorParser"; //$NON-NLS-1$
	private Pattern pkgconfigError = 
		Pattern.compile(".*?(configure:\\s+error:\\s+Package requirements\\s+\\((.*?)\\)\\s+were not met).*"); //$NON-NLS-1$
	private Pattern genconfigError = 
			Pattern.compile(".*?configure:\\s+error:\\s+.*"); //$NON-NLS-1$
	private Pattern checkingFail = 
			Pattern.compile("checking for (.*)\\.\\.\\. no"); //$NON-NLS-1$
	private ErrorParserManager epm;

	private Pattern changingConfigDirectory = 
			Pattern.compile("Configuring in (.*)"); //$NON-NLS-1$

	private IPath configDir;

	public ErrorParser(IPath configPath) {
		this.configDir = configPath.removeLastSegments(1);
	}

	public boolean processLine(String line, ErrorParserManager eoParser) {
		Matcher m;
		
		m = changingConfigDirectory.matcher(line);
		if(m.matches()){
			// set configuration directory.
			return false;
		}
		if (epm == null)
			epm = eoParser;
		m = pkgconfigError.matcher(line);
		if (m.matches()) {
			eoParser.generateExternalMarker(epm.getProject(), -1, m.group(1), SEVERITY_ERROR_BUILD, null, null, m.group(2));
			return true;
		} 
		
		m = genconfigError.matcher(line);
		if (m.matches()) {
			eoParser.generateMarker(epm.getProject(), -1, m.group(1), SEVERITY_ERROR_BUILD, null);
			return true; 			
		}
		
		m = checkingFail.matcher(line);
		if (m.matches()) {
			// We know that there is a 'checking for ...' fail.
			// Find the log file containing this check
			String type = getCheckType(m.group(1));
			if (type != null)
				eoParser.generateMarker(epm.getProject(), -1, "Missing " + type + " " + m.group(1), SEVERITY_ERROR_BUILD, null);
			return true; 			
		}
		
		return false;
	}

	/**
	 * Given the name of the filed check object, look for it in the log file
	 * file and then examine the configure script to figure out what the type of
	 * the check was.
	 * 
	 * @param name
	 * @return
	 */
	private String getCheckType(String name) {
		int lineNumber = getErrorConfigLineNumber(name);

		// now open configure file.
		File file = new File(configDir + "/configure");
		// If the log file is not present there is nothing we can do.
		if (!file.exists())
			return null;

		FileReader stream;
		try {
			stream = new FileReader(file);
			LineNumberReader reader = new LineNumberReader(stream);

			// look for something like:
			// if test "${ac_cv_prog_WINDRES+set}" = set; then :
			Pattern errorPattern = Pattern.compile(".*ac_cv_([a-z]*)_.*"); //$NON-NLS-1$

			// skip to the line
			String line = reader.readLine();
			for (int i = 0; i < lineNumber + 10 && line != null; i++) {
				if (i < lineNumber) {
					line = reader.readLine();
					continue;
				}
				Matcher m = errorPattern.matcher(line);
				if (m.matches()) {
					return m.group(1);
				}
				line = reader.readLine();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	/**
	 * Check the log file for the check for the given name and return the line
	 * number in configure where the check occurs.
	 * 
	 * @param name
	 * @return
	 */
	private int getErrorConfigLineNumber(String name) {
		try {
			File file = new File(configDir + "/config.log");
			// If the log file is not present there is nothing we can do.
			if (!file.exists())
				return -1;

			FileReader stream = new FileReader(file);
			LineNumberReader reader = new LineNumberReader(stream);

			Pattern errorPattern = Pattern
					.compile("configure:(\\d+): checking for " + name); //$NON-NLS-1$
			String line = reader.readLine();
			while (line != null) {
				// configure:9751: checking for windres
				Matcher m = errorPattern.matcher(line);

				if (m.matches()) {
					return Integer.parseInt(m.group(1));
				}

				line = reader.readLine();
			}

		} catch (Exception e) {
			return -1;
		}
		return -1;
	}
	
	@Override
	public IProject getProject() {
		if (epm != null)
			return epm.getProject();
		return null;
	}

}
