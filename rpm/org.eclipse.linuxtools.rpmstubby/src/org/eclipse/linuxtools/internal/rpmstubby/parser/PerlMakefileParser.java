/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - perl implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyLog;

/**
 * Class to parse a Perl Makefile.PL to grab specfile properties
 *
 */
public class PerlMakefileParser {

    /*
     * Perl Regular Expressions
     *
     */
    private static final String WHITE_SPACE = "(?:\\s+)";
    private static final String COMMENT = "#.+";                            // # comment
    private static final String LINE_WITH_COMMENT = "(?:(.*)#.+)";          // line # comment
    private static final String VARIABLE_PARAMS = "(?:my|local|our)";

    private static final String WORD = "(\\b\\w+\\b)";                      // variable
    private static final String NON_WHITE_SPACE = "(\\S+)";
    private static final String VARIABLE = "(?:" + VARIABLE_PARAMS
            + WHITE_SPACE + ")?(?:\\$|@|%)" + WORD + WHITE_SPACE + "?";     // %variable | my $variable | our @variable
    private static final String ASSOCIATIVE_KEY = WHITE_SPACE + "?"
            + NON_WHITE_SPACE + WHITE_SPACE + "?";

    private static final String EXCLUDE_SPECIALS = "(?![=~|\\-\\*\\+\\/])";
    private static final String NON_CONDITIONAL = "(?!(?:\\s*if|elsif|unless))";
    private static final String ASSIGNMENT_OPERATOR = "=";
    private static final String ASSOCIATIVE_OPERATOR = "=>";
    private static final String SIMPLE_ASSIGNMENT = NON_CONDITIONAL
            + WHITE_SPACE + "?" + VARIABLE + "(?:" + ASSIGNMENT_OPERATOR + ")"
            + EXCLUDE_SPECIALS + "(?:(.+))";                                // %var = value || [1] = [2]
    private static final String ASSOCIATIVE_ASSIGNMENT = ASSOCIATIVE_KEY
            + ASSOCIATIVE_OPERATOR + "(?:(.+))";                            // 'key' => 'value' || [1] => [2]

    private static final String FUNCTION = "\\s*" + WORD
            + "*\\s*?\\((.*)\\)\\s*";                                       // foo(bar) | foo(foo(bar)) | foo() || [1]([2])
    private static final String BEGIN_END = "(?:[^#]*<<END)";               // [CS] test<<END | <<END
    private static final String END_END = "^END$";                          // [CS]
    private static final String BEGIN_BC = "^=(?!cut)[a-z]\\S+(\\s)?\\S+";  // [CS] =test | =test test
    private static final String END_BC = "^=cut$";                          // [CS]

    private static final String MAKEFILE_FUNCTION_NAME = "WriteMakefile";
    private static final String MAKEFILE_FUNCTION = "^.*"
            + MAKEFILE_FUNCTION_NAME + WHITE_SPACE + "?\\(.*$";

    /*
     * A few common opening characters and their closing counterparts
     *
     */
    private static final Map<Character, Character> SURROUNDING_CHARACTER;
    static {
        Map<Character, Character> aMap = new HashMap<>();
        aMap.put('[', ']');
        aMap.put('{', '}');
        aMap.put('(', ')');
        aMap.put(']', '[');
        aMap.put('}', '{');
        aMap.put(')', '(');
        SURROUNDING_CHARACTER = Collections.unmodifiableMap(aMap);
    }

    private static final char SQUARE_BRACKET = '[';
    private static final char CURLY_BRACKET = '{';
    private static final char ROUND_BRACKET = '(';

    private static final int IN_BRACKETS = 0x00000001;
    private static final int MAKE_FUNCTION = 0x00000002;

    private IFile file;
    private Map<String, String> mVariableDefinitions;
    private Map<String, String> mMakefileDefinitions;

    /**
     * Initialize.
     *
     * @param file
     *            The perl Makefile.
     * @throws CoreException
     *             Throws CoreException.
     * @throws IOException
     *             Throws IOException.
     */
    public PerlMakefileParser(IFile file) throws IOException, CoreException {
        mVariableDefinitions = new HashMap<>();
        mMakefileDefinitions = new HashMap<>();
        if (file.getContents().available() <= 0) {
            return;
        }
        this.file = file;
        parse();
    }

    /**
     * Parse the perl Makefile.
     *
     */
    public void parse() {
        String content = "";
        String line = "";
        try (Scanner variableScanner = new Scanner(file.getContents())) {
            grabSimpleDefinitions(cleanUpContent(variableScanner));
            cleanupVariables(mVariableDefinitions);
            resolveVariables(mVariableDefinitions);

            /*
             * Going through the makefile function's attributes
             */
            if (!mVariableDefinitions.containsKey(MAKEFILE_FUNCTION_NAME)) {
                return;
            }
            try (Scanner makefileScanner = new Scanner(
                    mVariableDefinitions.get(MAKEFILE_FUNCTION_NAME))) {
                makefileScanner.useDelimiter("(?<=,)");
                ArrayList<String> makefileList = new ArrayList<>();

                while (makefileScanner.hasNext()) {
                    line = makefileScanner.next();
                    if (matchesAssociativeAssignment(line)) {
                        makefileList.add(line);
                    } else if (!makefileList.isEmpty()) {
                        makefileList.set(makefileList.size() - 1, makefileList
                                .get(makefileList.size() - 1).concat(line));
                    }
                }

                for (String str : makefileList) {
                    content = content.concat(str + '\n');
                }
                grabAssociativeDefinitions(content);
                cleanupVariables(mMakefileDefinitions);
                resolveVariables(mMakefileDefinitions);

            }
            variableScanner.close();
        } catch (CoreException e) {
            StubbyLog.logError(e);
        }
    }

    /**
     * Get the value of the variable.
     *
     * @param key The variable to get the value of.
     * @return The value of the variable.
     */
    public String getValue(String key) {
        String rc = "";
        if (mMakefileDefinitions.containsKey(key)) {
            rc = mMakefileDefinitions.get(key);
        }
        return rc;
    }

    /**
     * Get the list of values from the variable.
     *
     * @param key The variable to get the value of.
     * @return The list of values from the variable.
     */
    public List<String> getValueList(String key) {
        List<String> rc = new ArrayList<>();
        String var = "";
        if (mMakefileDefinitions.containsKey(key)) {
            var = mMakefileDefinitions.get(key);
            String[] tmp = var.split(",");
            for (String str : tmp) {
                str = cleanUpString(str);
                rc.add(str);
            }
        }
        return rc;
    }

    /**
     * Grab the simple key->value pairs from some content.
     *
     * @param content
     *            The content to grab the key->value pairs from.
     */
    private void grabSimpleDefinitions(String content) {
        try (Scanner scanner = new Scanner(content)) {
            Stack<Character> brackets = new Stack<>();
            String key = "";
            String value = "";
            String tempVar = "";
            String line = "";
            String[] tmp;
            int flags = 0;

            while (scanner.hasNext()) {
                line = scanner.nextLine();
                if (matchesSimpleAssignment(line)) {
                    tmp = line.split("=");
                    key = removeVariableSigils(tmp[0]).toLowerCase()
                            .replaceAll("\\W", "");
                    value = tmp[1];
                    if (containsOpener(value)) {
                        flags |= IN_BRACKETS;
                    } else {
                        mVariableDefinitions.put(key, value);
                    }
                } else if (containsMakefileFunction(line)) {
                    flags |= MAKE_FUNCTION;
                    flags |= IN_BRACKETS;
                }
                if ((flags & IN_BRACKETS) == IN_BRACKETS) {
                    checkBrackets(brackets, line);
                    tempVar = tempVar.concat(line.trim());
                    if (brackets.isEmpty()) {
                        if ((flags & MAKE_FUNCTION) == MAKE_FUNCTION) {
                            mVariableDefinitions
                                    .putAll(extractFunction(tempVar));
                        } else {
                            tmp = tempVar.split("=[^>]");
                            key = removeVariableSigils(tmp[0]).toLowerCase()
                                    .replaceAll("\\W", "");
                            value = tmp[1];
                            mVariableDefinitions.put(key, value);
                        }
                        tempVar = "";
                        flags &= 0;
                    }
                }
            }
        }
    }

    /**
     * Grab the associative key=>value pairs from some content.
     *
     * @param content
     *            The content to grab the key=>value pairs from.
     */
    private void grabAssociativeDefinitions(String content) {
        try (Scanner scanner = new Scanner(content)) {
            Stack<Character> brackets = new Stack<>();
            String key = "";
            String value = "";
            String tempVar = "";
            String line = "";
            String[] tmp;
            int flags = 0;

            while (scanner.hasNext()) {
                line = scanner.nextLine();
                if (matchesAssociativeAssignment(line)
                        && (flags & IN_BRACKETS) != IN_BRACKETS) {
                    tmp = line.split("=>");
                    key = removeVariableSigils(tmp[0].toLowerCase().replaceAll(
                            "\\W", ""));
                    value = tmp[1];
                    if (containsOpener(value)) {
                        flags |= IN_BRACKETS;
                    } else {
                        mMakefileDefinitions.put(key, value);
                    }
                }
                if ((flags & IN_BRACKETS) == IN_BRACKETS) {
                    checkBrackets(brackets, line);
                    tempVar = tempVar.concat(line.trim());
                    if (brackets.isEmpty()) {
                        key = removeVariableSigils(tempVar
                                .substring(0, tempVar.indexOf("=>"))
                                .toLowerCase().replaceAll("\\W", ""));
                        value = tempVar.substring(tempVar.indexOf("=>") + 2,
                                tempVar.length());
                        mMakefileDefinitions.put(key, value);
                        tempVar = "";
                        flags &= 0;
                    }
                }
            }
        }
    }

    /**
     * Check if the line is within a set of brackets. Update the stack tracking
     * bracket completeness.
     *
     * @param brackets
     *            The stack tracking the bracket completeness.
     * @param line
     *            The line to parse for brackets.
     */
    public static void checkBrackets(Stack<Character> brackets, String line) {
        for (char c : line.toCharArray()) {
            if (c == SQUARE_BRACKET || c == CURLY_BRACKET || c == ROUND_BRACKET) {
                brackets.push(c);
            } else if (c == SURROUNDING_CHARACTER.get(SQUARE_BRACKET)
                    || c == SURROUNDING_CHARACTER.get(CURLY_BRACKET)
                    || c == SURROUNDING_CHARACTER.get(ROUND_BRACKET)) {
                if (brackets.peek() == SURROUNDING_CHARACTER.get(c)
                        && !brackets.isEmpty()) {
                    brackets.pop();
                }
            }
        }
    }

    /**
     * Go through the map of key->value pairings and check and see if a key has
     * another key as a value. If so, resolve that.
     *
     * @param variables
     *            The key->value pairings.
     */
    private void resolveVariables(Map<String, String> variables) {
        String tempVal = "";
        for (Entry<String,String> entry : variables.entrySet()) {
            tempVal = entry.getValue();
            if (mVariableDefinitions.containsKey(tempVal)) {
                variables.put(entry.getKey(), mVariableDefinitions.get(tempVal));
            }
        }
    }

    /**
     * Extract the function name and the contents of the function and return it
     * as a key->value pairing.
     *
     * @param line
     *            The line to extract the function from.
     * @return The key->value pairing of function and parameter(s).
     */
    public static Map<String, String> extractFunction(String line) {
        Map<String, String> rc = new HashMap<>();
        Pattern pattern = Pattern.compile(FUNCTION, Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.find()) {
            rc.put(variableMatcher.group(1), variableMatcher.group(2));
        }
        return rc;
    }

    /**
     * Extract the key->value pairing from an associative assignment.
     *
     * @param line
     *            The line to extract the function from.
     * @return The key->value pairing of function and parameter(s).
     */
    public static Map<String, String> extractKeyValueAssociation(String line) {
        Map<String, String> rc = new HashMap<>();
        String key = "";
        String value = "";
        if (matchesAssociativeAssignment(line)) {
            String[] keyValue = line.split("=>");
            key = cleanUpString(keyValue[0].toLowerCase());
            value = cleanUpString(keyValue[1]);
            rc.put(key, value);
        }
        return rc;
    }

    /**
     * Utility to go through the map of variables to clean up their values.
     *
     * @param variables
     *            The map of variables to go through.
     */
    private static void cleanupVariables(Map<String, String> variables) {
        String val = "";
        for (Entry<String,String> entry : variables.entrySet()) {
            val = cleanUpString(removeVariableSigils(entry.getValue()))
                    .trim();
            if (val.startsWith("(") || val.startsWith("[")
                    || val.startsWith("{")) {
                val = val.substring(1, val.length());
            }
            if (val.endsWith(")") || val.endsWith("]") || val.endsWith("}")) {
                val = val.substring(0, val.length() - 1);
            }
            val = cleanUpString(val);
            variables.put(entry.getKey(), val);
        }
    }

    /**
     * Utility to clean up the line of some unwanted characters.
     *
     * @param line
     *            The line to clean up.
     * @return The cleaned up version of the line.
     */
    private static String cleanUpString(String line) {
        String rc = "";
        line = line.trim().replaceAll("('|\")", "");
        if (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1);
        }
        if (line.endsWith(",")) {
            line = line.substring(0, line.length() - 1);
        }
        rc = line;
        return rc;
    }

    /**
     * Remove the variable sigils.
     *
     * @param variable
     *            The variable to remove sigils from.
     * @return A word only variable with no sigils.
     */
    public static String removeVariableSigils(String variable) {
        return variable
                .replaceAll(
                        "(\\bmy\\b|\\bour\\b|\\blocal\\b|(\\\\)?\\$|(\\\\)?@|(\\\\)?%)",
                        "");
    }

    /**
     * Clean up the contents of the file and remove all the comments, END
     * blocks, and block comments.
     *
     * @param scanner
     *            The scanner of the file to be read.
     * @return The cleaned up content.
     */
    private static String cleanUpContent(Scanner scanner) {
        String rc = "";
        String line = "";
        boolean flagEND = true;
        boolean flagBC = true;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            // ignore lines between ENDS
            if (containsBeginEND(line)) {
                flagEND = false;
            } else if (matchesEndEND(line)) {
                line = scanner.nextLine();
                flagEND = true; // true
            }
            // ignore lines between =someword and =cut
            if (matchesBeginBC(line)) {
                flagBC = false;
            } else if (matchesEndBC(line)) {
                line = scanner.nextLine();
                flagBC = true;
            }
            // remove the comments
            if (matchesLineWithComment(line)) {
                line = line.replaceAll(COMMENT, "");
            }
            // if not empty line or within comment/END block
            if (flagEND && flagBC && !line.trim().equals("")) {
                rc = rc.concat(line + '\n');
            }
        }
        return rc;
    }

    /**
     * Check if a line contains a function.
     *
     * @param line
     *            The line to check.
     * @return True if the line contains a function.
     */
    public static boolean containsFunction(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(FUNCTION, Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.find()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check to see if the line contains the WriteMakefile function.
     *
     * @param line
     *            The line to check.
     * @return True if the line contains the WriteMakefile function.
     */
    public static boolean containsMakefileFunction(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(MAKEFILE_FUNCTION,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.find()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check to see if the line contains an opening bracket.
     *
     * @param line
     *            The line to check.
     * @return True if the line contains an opening bracket.
     */
    public static boolean containsOpener(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile("(\\(|\\[|\\{)",
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.find()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check if a line contains <<END. It is case-sensitive.
     *
     * @param line
     *            The line to check.
     * @return True if the line contains <<END.
     */
    public static boolean containsBeginEND(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(BEGIN_END);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.find()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check if a line is a simple assignment of a variable.
     *
     * @param line
     *            The line to check.
     * @return True if the line is a simple assignment of a variable.
     */
    public static boolean matchesSimpleAssignment(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(SIMPLE_ASSIGNMENT,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.matches()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check if a line is a simple assignment of a variable.
     *
     * @param line
     *            The line to check.
     * @return True if the line is a simple assignment of a variable.
     */
    public static boolean matchesAssociativeAssignment(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(ASSOCIATIVE_ASSIGNMENT,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.matches()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check if a line matches the END. It is case-sensitive.
     *
     * @param line
     *            The line to check.
     * @return True if the line matches END.
     */
    public static boolean matchesEndEND(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(END_END);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.matches()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check if a line matches the beginning of a block comment. It is
     * case-sensitive.
     *
     * @param line
     *            The line to check.
     * @return True if the line matches the beginning of a block comment.
     */
    public static boolean matchesBeginBC(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(BEGIN_BC);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.matches()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check if a line matches the end of a block comment (=cut). It is
     * case-sensitive.
     *
     * @param line
     *            The line to check.
     * @return True if the line matches =cut.
     */
    public static boolean matchesEndBC(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(END_BC);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.matches()) {
            rc = true;
        }
        return rc;
    }

    /**
     * Check if a line matches that of a line with a comment.
     *
     * @param line
     *            The line to check.
     * @return True if the line matches that of a line with a comment.
     */
    public static boolean matchesLineWithComment(String line) {
        boolean rc = false;
        Pattern pattern = Pattern.compile(LINE_WITH_COMMENT,
                Pattern.CASE_INSENSITIVE);
        Matcher variableMatcher = pattern.matcher(line);
        if (variableMatcher.matches()) {
            rc = true;
        }
        return rc;
    }
}
