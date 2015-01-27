/*******************************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers;

import java.text.MessageFormat;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.CommentRemover;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.Messages;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.FuncparamNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.FunctionNodeData;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;

/**
 * Runs stap -vp1 in order to get all of the functions
 * that are defined in the tapsets.  Builds function trees
 * with the values obtained from the tapsets.
 *
 * @author Ryan Morse
 * @since 2.0
 */
public final class FunctionParser extends TreeTapsetParser {

    private static FunctionParser parser = null;

    /**
     * The descriptor used for unresolvable types.
     */
    public static final String UNKNOWN_TYPE = "unknown"; //$NON-NLS-1$

    private static final String FUNC_REGEX = "(?s)(?<!\\w)function\\s+{0}(?:\\s*:\\s*(\\w+))?\\s*\\(([^)]+?)?\\)"; //$NON-NLS-1$
    private static final Pattern P_FUNCTION = Pattern.compile("function (?!_)(\\w+) \\(.*?\\)"); //$NON-NLS-1$
    private static final Pattern P_PARAM = Pattern.compile("(\\w+)(?:\\s*:\\s*(\\w+))?"); //$NON-NLS-1$
    private static final Pattern P_ALL_CAP = Pattern.compile("[A-Z_1-9]*"); //$NON-NLS-1$
    private static final Pattern P_RETURN = Pattern.compile("(?<!\\w)return\\W"); //$NON-NLS-1$

    public static FunctionParser getInstance() {
        if (parser != null) {
            return parser;
        }
        parser = new FunctionParser();
        return parser;
    }

    private FunctionParser() {
        super(Messages.FunctionParser_name);
    }

    /**
     * This method is used to build up the list of functions that were found
     * during the first pass of stap.
     *
     * FunctionTree organized as:
     *    Root->Functions->Parameters
     */
    @Override
    protected int runAction(IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            return IStatus.CANCEL;
        }

        String tapsetContents = SharedParser.getInstance().getTapsetContents();
        int result = verifyRunResult(tapsetContents);
        if (result != IStatus.OK) {
            return result;
        }

        boolean canceled = false;
        try (Scanner scanner = new Scanner(tapsetContents)) {
            scanner.useDelimiter("(?=" + SharedParser.TAG_FILE + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            while (scanner.hasNext()) {
                if (monitor.isCanceled()) {
                    canceled = true;
                    break;
                }
                addFunctionsFromFileContents(scanner.next());
            }
        }
        tree.sortLevel();
        return !canceled ? IStatus.OK : IStatus.CANCEL;
    }

    /**
     * Uses the tapset content dump of a single file to collect all
     * functions provided by that file.
     * @param fileContents The tapset contents of a single file.
     */
    private void addFunctionsFromFileContents(String fileContents) {
        String filename;
        try (Scanner st = new Scanner(fileContents)) {
            filename = SharedParser.findFileNameInTag(st.nextLine());
        }

        Matcher matcher = P_FUNCTION.matcher(fileContents);
        String scriptText = null;
        while (matcher.find()) {
            String functionName = matcher.group(1);
            if (P_ALL_CAP.matcher(functionName).matches()) {
                // Ignore ALL_CAPS functions, since they are not meant for end-user use.
                continue;
            }
            if (scriptText == null) {
                // If this is the first time seeing this file, remove its comments.
                scriptText = CommentRemover.execWithFile(filename);
            }
            addFunctionFromScript(functionName, scriptText, filename);
        }
    }

    /**
     * Searches the actual contents of a .stp script file for a specific function, and adds
     * @param functionName The name of the function to search for.
     * @param scriptText The contents of the script to search, with its comments removed
     * (Use {@link CommentRemover} on file contents before passing them here, if necessary).
     * @param scriptFilename The name of the script file being searched.
     */
    private void addFunctionFromScript(String functionName, String scriptText, String scriptFilename) {
        String regex = MessageFormat.format(FUNC_REGEX, functionName);
        Matcher mScript = Pattern.compile(regex).matcher(scriptText);
        if (mScript.find()) {
            String functionLine = mScript.group();
            String functionType = mScript.group(1);
            // If the function has no return type, look for a "return" statement to check
            // if it's really a void function, or if its return type is just unspecified
            if (functionType == null && isPatternInScriptBlock(scriptText, mScript.end(), P_RETURN)) {
                functionType = UNKNOWN_TYPE;
            }
            TreeDefinitionNode function = new TreeDefinitionNode(
                    new FunctionNodeData(functionLine, functionType),
                    functionName, scriptFilename, true);
            tree.add(function);
            addParamsFromString(mScript.group(2), function);
        }
    }

    private boolean isPatternInScriptBlock(String scriptText, int start, Pattern p) {
        int end, bcount = 1;
        start = scriptText.indexOf('{', start) + 1;
        for (end = start; end < scriptText.length(); end++) {
            char c = scriptText.charAt(end);
            if (c == '{') {
                bcount++;
            } else if (c == '}' && --bcount == 0) {
                break;
            }
        }
        return p.matcher(scriptText.substring(start, end)).find();
    }

    private void addParamsFromString(String params, TreeNode parentFunction) {
        if (params != null) {
            Matcher mParams = P_PARAM.matcher(params);
            while (mParams.find()) {
                parentFunction.add(new TreeNode(
                        new FuncparamNodeData(mParams.group(2)),
                        mParams.group(1), false));
            }
        }
        parentFunction.sortLevel();
    }

    @Override
    protected int delTapsets(String[] deletions, IProgressMonitor monitor) {
        for (int i = 0; i < deletions.length; i++) {
            for (int f = 0, fn = tree.getChildCount(); f < fn; f++) {
                if (monitor.isCanceled()) {
                    return IStatus.CANCEL;
                }
                String definition = ((TreeDefinitionNode) tree.getChildAt(f)).getDefinition();
                if (definition != null && definition.startsWith(deletions[i])) {
                    tree.remove(f--);
                    fn--;
                }
            }
        }
        return IStatus.OK;
    }

    @Override
    protected int addTapsets(String tapsetContents, String[] additions, IProgressMonitor monitor) {
        boolean canceled = false;
        // Search tapset contents for all files provided by each added directory.
        for (int i = 0; i < additions.length; i++) {
            int firstTagIndex = 0;
            while (true) {
                // Get the contents of each file provided by the directory additions[i].
                firstTagIndex = tapsetContents.indexOf(
                        SharedParser.makeFileTag(additions[i]), firstTagIndex);
                if (firstTagIndex == -1) {
                    break;
                }
                int nextTagIndex = tapsetContents.indexOf(SharedParser.TAG_FILE, firstTagIndex + 1);
                String fileContents = nextTagIndex != -1
                        ? tapsetContents.substring(firstTagIndex, nextTagIndex)
                                : tapsetContents.substring(firstTagIndex);

                if (monitor.isCanceled()) {
                    canceled = true;
                    break;
                }
                addFunctionsFromFileContents(fileContents);
                // Remove the contents of the file that was just examined from the total contents.
                tapsetContents = tapsetContents.substring(0, firstTagIndex).concat(
                        tapsetContents.substring(firstTagIndex + fileContents.length()));
            }
        }
        tree.sortLevel();
        return !canceled ? IStatus.OK : IStatus.CANCEL;
    }

}
