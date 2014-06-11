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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import java.text.MessageFormat;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.CommentRemover;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
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
    private TreeNode functions;

    /**
     * The descriptor used for unresolvable types.
     */
    public static final String UNKNOWN_TYPE = "unknown"; //$NON-NLS-1$

    private static final String FUNC_REGEX = "(?s)(?<!\\w)function\\s+{0}(?:\\s*:\\s*(\\w+))?\\s*\\(([^)]+?)?\\)"; //$NON-NLS-1$
    private static final Pattern P_FUNCTION = Pattern.compile("function (?!_)(\\w+) \\(.*?\\)"); //$NON-NLS-1$
    private static final Pattern P_PARAM = Pattern.compile("(\\w+)(?:\\s*:\\s*(\\w+))?"); //$NON-NLS-1$
    private static final Pattern P_ALL_CAP = Pattern.compile("[A-Z_1-9]*"); //$NON-NLS-1$
    private static final Pattern P_RETURN = Pattern.compile("\\sreturn\\W"); //$NON-NLS-1$

    public static FunctionParser getInstance() {
        if (parser != null) {
            return parser;
        }
        parser = new FunctionParser();
        return parser;
    }

    private FunctionParser() {
        super("Function Parser"); //$NON-NLS-1$
    }

    @Override
    public synchronized TreeNode getTree() {
        return functions;
    }

    @Override
    protected void resetTree() {
        functions = new TreeNode(null, false);
    }

    @Override
    public void dispose() {
        functions.dispose();
    }

    /**
     * Runs stap to collect all available tapset functions.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        super.run(monitor);
        boolean canceled = !addFunctions(monitor);
        functions.sortTree();
        return new Status(!canceled ? IStatus.OK : IStatus.CANCEL,
                IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
    }

    /**
     * This method is used to build up the list of functions that were found
     * during the first pass of stap.
     *
     * FunctionTree organized as:
     *    Root->Functions->Parameters
     *
     * @return <code>false</code> if a cancelation prevented all functions from being added;
     * <code>true</code> otherwise.
     */
    private boolean addFunctions(IProgressMonitor monitor) {
        String tapsetContents = SharedParser.getInstance().getTapsetContents();
        if (tapsetContents == null) {
            // Functions are only drawn from the tapset dump, so exit if it's empty.
            return true;
        }
        try (Scanner st = new Scanner(tapsetContents)) {
            String filename = null;
            String scriptText = null;

            SharedParser sparser = SharedParser.getInstance();
            while (st.hasNextLine()) {
                if (monitor.isCanceled()) {
                    return false;
                }
                String tok = st.nextLine();
                Matcher mFilename = sparser.filePattern.matcher(tok);
                if (mFilename.matches()) {
                    filename = mFilename.group(1).toString();
                    scriptText = null;
                } else if (filename != null) {
                    Matcher mFunction = P_FUNCTION.matcher(tok);
                    if (mFunction.matches()) {
                        String functionName = mFunction.group(1);
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
            }
            return true;
        }
    }

    private void addFunctionFromScript(String functionName, String scriptText, String scriptFilename) {
        String regex = MessageFormat.format(FUNC_REGEX, functionName);
        Matcher mScript = Pattern.compile(regex).matcher(scriptText);
        if (mScript.find()) {
            String functionLine = mScript.group();
            String functionType = mScript.group(1);
            // If the function has no return type, look for a "return" statement to check
            // if it's really a void function, or if its return type is just unspecified
            if (functionType == null && getNextBlockContents(scriptText, mScript.end(), P_RETURN)) {
                functionType = UNKNOWN_TYPE;
            }
            TreeDefinitionNode function = new TreeDefinitionNode(
                    new FunctionNodeData(functionLine, functionType),
                    functionName, scriptFilename, true);
            functions.add(function);
            addParamsFromString(mScript.group(2), function);
        }
    }

    private boolean getNextBlockContents(String scriptText, int start, Pattern p) {
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
    }

}
