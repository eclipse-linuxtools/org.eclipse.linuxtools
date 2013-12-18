/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - python implementation (B#350065)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyLog;

/**
 * Class to parse a Python setup.py to grab specfile properties
 *
 */
public class PythonEggParser {

	private Map<String, String> variables;
	private Map<String, String> setupOptions;
	private IFile file;

	/**
	 * Initialize and then parse the file
	 *
	 * @param file The Python setup.py file
	 * @throws CoreException Throws CoreException
	 * @throws IOException  Throws IOException
	 */
	public PythonEggParser(IFile file) throws IOException, CoreException {
		setupOptions = new HashMap<>();
		variables = new HashMap<>();
		// end if file is empty or cannot get its contents
		if (file.getContents().available() <= 0) {
			return;
		}
		this.file = file;
		parse();
	}

	/**
	 * Parse the contents of the Python setup.py file and grab
	 * variables and meta-data from the setup(...) function
	 *
	 */
	public void parse() {
		String line = "";
		String setupLine = "";
		List<String> vars = new ArrayList<>();
		List<String> list = new ArrayList<>();
		int offset = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(file.getRawLocation().makeAbsolute().toFile(), "r");

			// end if cannot find setup(
			long bytesToSkip = findStartSetup(raf);
			if (bytesToSkip == -1) {
				return;
			}

			// end if the end of setup cannot be found
			long stop = findEndSetup(raf, bytesToSkip);
			if (stop == -1) {
				return;
			}

			raf.seek(0);
			while ((line = raf.readLine()) != null) {
				if (!line.trim().startsWith("#")) {
					if (isLineSimpleDefinition(line)) {
						vars.add(line);
					} else if (!vars.isEmpty() && vars.get(vars.size()-1).trim().endsWith("\\") && isLineContinuation(line)){
						offset = vars.get(vars.size()-1).lastIndexOf('\\');
						vars.set(vars.size() -1, vars.get(vars.size() - 1).substring(0, offset));
						vars.set(vars.size() - 1, vars.get(vars.size() - 1)
								.concat(line.trim()));
					}
				}
			}

			raf.seek(bytesToSkip);
			while ((line = raf.readLine()) != null && raf.getFilePointer() <= stop) {
				line = line.trim();
				if (!line.startsWith("#")) {
					if (setupLine.equals("")) {
						setupLine = line.trim();
					} else {
						setupLine = setupLine.concat(line.trim());
					}
				}
			}

			list = prepareSetupOptions(setupLine);

			for (String str : vars) {
				variables.putAll(parseLine(str));
			}

			for (String str : list) {
				setupOptions.putAll(parseLine(str));
			}

			resolveVariables(variables, setupOptions);

			raf.close();
		} catch (FileNotFoundException e) {
			StubbyLog.logError(e);
		} catch (IOException e) {
			StubbyLog.logError(e);
		}
	}

	/**
	 * Check to see if the string passed in is a function
	 *
	 * @param str The string to check
	 * @return True if the string matches the function regex
	 */
	public boolean checkFunction(String str) {
		boolean rc = false;
		Pattern pattern = Pattern.compile("\\s*\\w*\\s*?\\(.*\\)\\s*");
		Matcher variableMatcher = pattern.matcher(str);

		if (variableMatcher.matches()) {
			rc = true;
		}

		return rc;
	}

	/**
	 * Get the value of the variable
	 *
	 * @param key The variable to get the value of
	 * @return The value of the variable
	 */
	public String getValue(String key) {
		String rc = "";
		Pattern pattern = Pattern.compile("\\s*\\((.+)\\)\\s*");
		Matcher variableMatcher = null;

		if (setupOptions.containsKey(key)) {
			rc = setupOptions.get(key).replaceAll("('|\")", "").trim();
			variableMatcher = pattern.matcher(rc);
			if (variableMatcher.matches()) {
				rc = variableMatcher.group(1);
			}
		}

		return rc;
	}

	/**
	 * Get the list of strings for a key.
	 * Use with classifiers, platforms, install_requires, etc.
	 *
	 * @param key The variable to get the value of
	 * @return The value of the variable
	 */
	public List<String> getValueList(String key) {
		List<String> rc = new ArrayList<>();
		Pattern pattern = Pattern.compile("^\\[(.*)\\]");
		String[] temp = {};

		if (setupOptions.containsKey(key)) {
			Matcher variableMatcher = pattern.matcher(setupOptions.get(key).trim());
			if (variableMatcher.find()) {
				temp = variableMatcher.group(1).replaceAll("('|\")", "").split(",");
				for (String str : temp) {
					if (!str.isEmpty() && !str.trim().startsWith("#")) {
						rc.add(str.trim());
					}
				}
			}
		}

		return rc;
	}

	/**
	 * Prepare the setup options by returning each comma delimited option
	 *
	 * @param setupLine The single string containing all the setup options
	 * @return A list of setup options
	 */
	private static List<String> prepareSetupOptions(String setupLine) {
		List<String> rc = new ArrayList<>();
		String[] tempList = {};
		// match the setup(...) pattern
		Pattern pattern = Pattern.compile("\\bsetup\\b(\\s+)?\\((.*)\\)");
		Matcher variableMatcher = pattern.matcher(setupLine);

		if (variableMatcher.find()) {
			setupLine = variableMatcher.group(2);
		}

		tempList = setupLine.split("(?=,)");

		for (String str : tempList) {
			if (isOptionLineKeyValuePair(str) && !str.trim().startsWith("#")) {
				if (str.startsWith(",")) {
					str = str.substring(1, str.length()).trim();
				}
				rc.add(str);
			} else if (!str.trim().startsWith("#") && !rc.isEmpty()) {
				rc.set(rc.size() - 1, rc.get(rc.size() - 1).concat(str.trim()));
			}
		}

		return rc;
	}

	/**
	 * Resolves the setup option variables if they are referencing a
	 * define from outside the setup() function.
	 *
	 * @param variables The variables outside the setup() function
	 * @param options The options to be resolved within the setup() function
	 */
	private static void resolveVariables(Map<String, String> variables, Map<String, String> options) {
		for (Entry<String, String> entry : options.entrySet()) {
			if (variables.containsKey(entry.getValue())) {
				options.put(entry.getKey(), variables.get(entry.getValue()));
			}
		}
	}

	/**
	 * Check to see if the line in setup option is a new key->value pair
	 *
	 * @param line Line to check
	 * @return True if the line contains a key->value
	 */
	private static boolean isOptionLineKeyValuePair(String line) {
		boolean rc = false;
		Pattern pattern = Pattern.compile("(\\w+)(\\s+)?=[^=].*");
		Matcher variableMatcher = pattern.matcher(line.toLowerCase());

		if (variableMatcher.find()) {
			rc = true;
		}

		return rc;
	}

	/**
	 * Check to see if the line is a simple variable declaration (var=value)
	 *
	 * @param line Line to check
	 * @return True if it is a simple variable declaration
	 */
	private static boolean isLineSimpleDefinition(String line) {
		boolean rc = false;
		Pattern pattern = Pattern.compile("^(\\w+)(\\s+)?=(\\s+)?");
		Matcher variableMatcher = pattern.matcher(line.toLowerCase());

		if (variableMatcher.find()) {
			rc = true;
		}

		return rc;
	}

	/**
	 * Check to see if the line is a continuation from the previous.
	 * It is a continuation from the previous if it starts
	 * with a ' or "
	 *
	 * @param line Line to check
	 * @return True if the line is a continuation
	 */
	private static boolean isLineContinuation(String line) {
		boolean rc = false;
		Pattern pattern = Pattern.compile(".*[\'\"](/s+)?$");
		Matcher variableMatcher = pattern.matcher(line.toLowerCase());

		if (variableMatcher.find()) {
			rc = true;
		}

		return rc;
	}

	/**
	 * Parse the line and split it into a key->value pair
	 *
	 * @param line The line to be parsed
	 * @return The map containing the key->value pair
	 */
	private static Map<String, String> parseLine(String line) {
		Map<String, String> rc = new HashMap<>();
		Pattern pattern = Pattern.compile("(\\s+)?(\\w+)(\\s+)?=(\\s+)?(.*)");
		Matcher variableMatcher = pattern.matcher(line);

		if (variableMatcher.find()) {
			String value = variableMatcher.group(5);
			if (value.charAt(value.length()-1) == ',') {
				value = value.substring(0, value.length()-1);
			}
			rc.put(variableMatcher.group(2), value);
		}

		return rc;
	}

	/**
	 * Find the offset of when setup(...) starts
	 *
	 * @param reader The file reader
	 * @return The position of the start of setup(...
	 * @throws IOException
	 */
	private static long findStartSetup(RandomAccessFile reader) throws IOException {
		long rc = -1;
		long previous = 0;
		Pattern pattern = Pattern.compile("^\\bsetup\\b(\\s+)?(\\()?");
		Matcher variableMatcher = null;
		String line = "";

		reader.seek(0);
		while ((line = reader.readLine()) != null && rc == -1) {
			variableMatcher = pattern.matcher(line.toLowerCase());
			if (variableMatcher.find()) {
				// get the previous line's file pointer location
				rc = previous;
			}
			previous = reader.getFilePointer();
		}

		return rc;
	}

	/**
	 * Find the offset of when setup(...) ends based on the
	 * position AFTER the closing bracket of setup()
	 *
	 * @param reader The file reader
	 * @param startPosition The position of the start of setup(...
	 * @return The position of the end of setup ...)
	 * @throws IOException
	 */
	private static long findEndSetup(RandomAccessFile reader, long startPosition) throws IOException {
		int bracketCounter = 0;
		boolean flag = false;
		boolean stop = false;
		String line = "";

		reader.seek(startPosition);
		while ((line = reader.readLine()) != null && stop == false) {
			for (char x : line.toCharArray()) {
				if (x == '(') {
					bracketCounter++;
				} else if (x == ')') {
					bracketCounter--;
				}
				if (flag && bracketCounter == 0) {
					stop = true;
				}
				// prevent ending prematurely
				if (bracketCounter != 0) {
					flag = true;
				}
			}
		}

		return reader.getFilePointer();
	}
}
