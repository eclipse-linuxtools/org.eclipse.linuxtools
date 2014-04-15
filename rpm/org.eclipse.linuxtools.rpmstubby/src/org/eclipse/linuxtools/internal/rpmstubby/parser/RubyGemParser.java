/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - ruby implementation (B#350066)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.parser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyLog;

/**
 * Class to parse a Ruby gemspec to grab specfile properties.
 */
public class RubyGemParser {

    private IFile file;
    private String gemVariable;
    private Map<String, List<String>> mSetupDefinitions;
    private Map<String, ArrayList<String>> mSetupDependencies;

    private static final String SETUP_START = "^gem[:\\.]{1,2}specification[:\\.]{1,2}new(\\s+)?do(\\s+)?\\|(\\s+)?(\\w+)(\\s+)?\\|";
    private static final String STRING = "(?:\\\"([^\\\"]+)\\\"|'([^']+)'|(?:%q|%Q)(?:([^\\w])([^/3].+)[^\\w]))";		// (%q|%Q) "value" | 'value' | {value}
    private static final String LIST =  	"(?!%q|%Q)(?:%w|%W)?(?:([\\W])(.+)[\\W])"; 									// (%w|%W) [value, value2] | {"value", "value2"}
    private static final String STRING_LIST = "(?:\\\"([^\\\"]+)\\\"|'([^']+)')(?:[, ])??";								// "test", "test2" | 'test' 'test2'
    private static final String GENERIC_LIST = "(?:\\S+)(?:\\s+)??"; 													// test, test2 | test test2

    private static final String REPLACE_ME = "(%REPLACE_ME)";
    private String simpleDefineRx = "(\\s+)?(?!#)(?:\\b(%REPLACE_ME)\\b\\.(\\w+))(\\s+)?=(?!=)(\\s+)?(.*)"; 		// gem.variable = ...
    private String genericDefineRx = "(\\s+)?(?!#)(?:\\b(%REPLACE_ME)\\b\\.(\\w+))(\\s+)?(.*)"; 					// gem.variable...
    private String simpleFunctionRx = "(\\s+)?(?!#)(?:\\b(%REPLACE_ME)\\b\\.(\\w+))(\\s+)?(?:\\((.*)\\))(.*)?"; 	// gem.variable(...)

    /**
     * Initialize
     *
     * @param file
     *            The gemspec file
     * @throws IOException Could not read from file
     * @throws CoreException File is not valid
     */
    public RubyGemParser(IFile file) throws IOException, CoreException {
        mSetupDefinitions = new HashMap<>();
        mSetupDependencies = new HashMap<>();
        if (file.getContents().available() <= 0) {
            return;
        }
        this.file = file;
        gemVariable = "";
        parse();
    }

    /**
     * Parse the Ruby gemspec file
     */
    public void parse() {
        List<String> rawSetupDefinitions = new ArrayList<>();
        List<String> lSetupDefinitions = new ArrayList<>();
        String line = "";
        long startPos;
        long endPos;
        try (RandomAccessFile raf = new RandomAccessFile(file.getRawLocation()
                .makeAbsolute().toFile(), "r")) {
            startPos = findStart(raf);
            endPos = findEnd(raf, startPos);

            raf.seek(startPos);
            while ((line = raf.readLine()) != null
                    && raf.getFilePointer() < endPos) {
                rawSetupDefinitions.add(line);
            }

            lSetupDefinitions = prepareOptions(rawSetupDefinitions);

            for (String str : lSetupDefinitions) {
                parseLine(str);
            }
        } catch (IOException e) {
            StubbyLog.logError(e);
        }
    }

    /**
     * Get the values taken from parsing the file as a list object
     *
     * @param key
     *            The gemspec option to get the values for
     * @return The values of the gemspec option
     */
    public List<String> getValueList(String key) {
        List<String> rc = new ArrayList<>();

        if (mSetupDependencies.containsKey(key)) {
            if (!mSetupDependencies.get(key).isEmpty()) {
                for (String element : mSetupDependencies.get(key)) {
                    rc.add(element);
                }
            }
        } else if (mSetupDefinitions.containsKey(key)) {
            if (!mSetupDefinitions.get(key).isEmpty()) {
                for (String element : mSetupDefinitions.get(key)) {
                    rc.add(element);
                }
            }
        }

        return rc;
    }

    /**
     * Parses a line to figure out what type of line it is
     *
     * @param str
     *            The line to parse
     */
    private void parseLine(String str) {
        if (str.matches(simpleDefineRx)) {
            parseSimpleDefine(str);
        } else if (str.matches(simpleFunctionRx)) {
            parseSimpleFunction(str);
        } else if (str.matches(genericDefineRx)) {
            parseGenericOption(str);
        }
    }

    /**
     * Parses a string to figure its value
     *
     * @param str
     *            The string parse
     * @return A list of objects that was found
     */
    private static List<String> parseValue(String str) {
        List<String> rc = new ArrayList<>();
        String temp = str.trim();
        Pattern pattern = null;
        Matcher variableMatcher = null;

        if (temp.matches(STRING)) {
            pattern = Pattern.compile(STRING,
                    Pattern.CASE_INSENSITIVE);
            variableMatcher = pattern.matcher(temp);
            // "" matches group 1
            if (temp.startsWith("\"") && variableMatcher.matches()) {
                rc.add(variableMatcher.group(1));
            } // '' matches group 2
            else if (temp.startsWith("'") && variableMatcher.matches()) {
                rc.add(variableMatcher.group(2));
            } // %q|%Q match
            else if ((temp.startsWith("%q") || temp.startsWith("%Q"))
                    && variableMatcher.matches()) {
                rc.add(variableMatcher.group(4));
            }
        } else if (temp.matches(LIST)) {
            pattern = Pattern.compile(LIST,
                    Pattern.CASE_INSENSITIVE);
            variableMatcher = pattern.matcher(temp);
            if (variableMatcher.matches()) {
                rc.addAll(parseList(variableMatcher.group(2)));
            }
        }

        return rc;
    }

    /**
     * Parse the long string into a list
     *
     * @param str
     *            The string to parse into a list
     * @return A list containing the found values
     */
    private static List<String> parseList(String str) {
        List<String> rc = new ArrayList<>();
        String temp = str.trim();
        Pattern pattern = isPatternFoundList(str);

        if (pattern == null) {
            return rc;
        }

        Matcher variableMatcher = pattern.matcher(temp);

        if (variableMatcher != null) {
            while (variableMatcher.find()) {
                rc.add(variableMatcher.group());
            }
        }
        return rc;
    }

    /**
     * Check if the string is a list of string values e.g. ["test", "test2"] OR
     * ['test' 'test2']
     *
     * @param str
     *            The string containing the list
     * @return The pattern of the string
     */
    private static Pattern isPatternFoundList(String str) {
        Pattern rc = Pattern.compile("");
        Pattern pattern = Pattern.compile(STRING_LIST,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(str);

        if (variableMatcher.find()) {
            rc = pattern;
        } else {
            rc = isGenericFoundList(str);
        }

        return rc;
    }

    /**
     * Check if the string is a list of generic values (non-strings) e.g. [test,
     * test2] OR [test test2]
     *
     * @param str
     *            The string containing the list
     * @return The pattern of the string
     */
    private static Pattern isGenericFoundList(String str) {
        Pattern rc = Pattern.compile("");
        Pattern pattern = Pattern.compile(GENERIC_LIST,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(str);

        if (variableMatcher.find()) {
            rc = pattern;
        }

        return rc;
    }

    /**
     * Parse and grab the value of a simple define. The value taken is anything
     * after "gem.variable = ..."
     *
     * @param str
     *            The simple define to parse
     */
    private void parseSimpleDefine(String str) {
        String temp = str.trim();
        Pattern pattern = Pattern.compile(simpleDefineRx,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(temp);

        if (variableMatcher.find()) {
            String optionName = variableMatcher.group(2);
            List<String> value = parseValue(variableMatcher
                    .group(5));
            if (!value.isEmpty()) {
                mSetupDefinitions.put(optionName, value);
            }
        }
    }

    /**
     * Parse and grab the value of a simple function. The value taken is
     * anything in "gem.variable(...)"
     *
     * @param str
     *            The function to parse
     */
    private void parseSimpleFunction(String str) {
        String temp = str.trim();
        Pattern pattern = Pattern.compile(simpleFunctionRx,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(temp);

        if (variableMatcher.find()) {
            String functionName = variableMatcher.group(2);
            ArrayList<String> dependencies = new ArrayList<>();
            dependencies.add(variableMatcher.group(4));
            if (!mSetupDependencies.containsKey(functionName)) {
                mSetupDependencies.put(functionName, dependencies);
            } else {
                if (!mSetupDependencies.get(functionName).containsAll(
                        dependencies)) {
                    mSetupDependencies.get(functionName).addAll(dependencies);
                }
            }
        }
    }

    /**
     * Parse and grab the value of a generic option. The value taken is anything
     * after "gem.variable..."
     *
     * @param str
     *            The option to parse
     */
    private void parseGenericOption(String str) {
        String temp = str.trim();
        Pattern pattern = Pattern.compile(genericDefineRx,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(temp);

        if (variableMatcher.find()) {
            String functionName = variableMatcher.group(2);
            ArrayList<String> dependencies = new ArrayList<>();
            dependencies.add(variableMatcher.group(4));
            if (!mSetupDependencies.containsKey(functionName)) {
                mSetupDependencies.put(functionName, dependencies);
            } else {
                if (!mSetupDependencies.get(functionName).containsAll(
                        dependencies)) {
                    mSetupDependencies.get(functionName).addAll(dependencies);
                }
            }
        }
    }

    /**
     * Prepare the options within the specification by taking multiple lines and
     * concatenating them to the option it belongs to
     *
     * @param list
     *            The list of options in their raw form
     * @return A refined list of options with a single line for each option
     */
    private List<String> prepareOptions(List<String> list) {
        List<String> rc = new ArrayList<>();
        String temp = "";

        for (String str : list) {
            temp = str.trim();
            if (isLineValidOption(temp)) {
                rc.add(str);
            } else if (!temp.startsWith("#") && !rc.isEmpty()) {
                rc.set(rc.size() - 1, rc.get(rc.size() - 1).concat(str));
            }
        }

        return rc;
    }

    /**
     * Check to see if the line being read is a valid option within the
     * specification
     *
     * @param line
     *            The line to check
     * @return True if the option within the specification is valid
     */
    private boolean isLineValidOption(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(genericDefineRx,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(line);

        if (variableMatcher.matches()) {
            rc = true;
        }

        return rc;
    }

    /**
     * Find the offset of when the specification starts and also store the
     * gemVariable used in the specification
     *
     * @param reader
     *            The file reader
     * @return The position of the start of the specification
     * @throws IOException
     */
    private long findStart(RandomAccessFile raf) throws IOException {
        long rc = -1;
        Pattern pattern = Pattern
                .compile(
                        SETUP_START,
                        Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = null;
        String line = "";

        raf.seek(0);
        while ((line = raf.readLine()) != null && rc == -1) {
            variableMatcher = pattern.matcher(line.trim());
            if (variableMatcher.matches()) {
                setGemVariable(variableMatcher.group(4));
                rc = raf.getFilePointer();
            }
        }

        return rc;
    }

    /**
     * Find the offset of when specification ends based on the position before
     * the end statement
     *
     * @param reader
     *            The file reader
     * @param startPosition
     *            The position of the start of the specification
     * @return The position of the end of the specification
     * @throws IOException
     */
    private static long findEnd(RandomAccessFile raf, long startPos)
            throws IOException {
        long rc = -1;
        Pattern pattern = Pattern.compile("^end", Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = null;
        String line = "";

        raf.seek(startPos);
        while ((line = raf.readLine()) != null && rc == -1) {
            variableMatcher = pattern.matcher(line.trim());
            if (variableMatcher.matches()) {
                rc = raf.getFilePointer();
            }
        }

        return rc;
    }

    /**
     * Sets the gemVariable taken from Gem::Specification.new do |gemVariable|
     *
     * @param str
     *            The variable to set the gemVariable as
     */
    private void setGemVariable(String str) {
        if (gemVariable.isEmpty()) {
            gemVariable = str.trim();
            simpleDefineRx = simpleDefineRx.replace(REPLACE_ME, gemVariable);
            simpleFunctionRx = simpleFunctionRx.replace(REPLACE_ME, gemVariable);
            genericDefineRx = genericDefineRx.replace(REPLACE_ME, gemVariable);
        }
    }
}
