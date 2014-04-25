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

package org.eclipse.linuxtools.internal.callgraph.core;

import java.util.ArrayList;

/**
 * Contains methods for generating a stap command
 * @author chwang
 *
 */
public class SystemTapCommandGenerator {

    private static boolean needsToSendCommand;
    private static boolean needsArguments;
    private static String arguments;
    private static String scriptPath;
    private static String flags;
    private static String binaryPath = null;
    private static String binaryArguments;
    private static String command;


    public static String generateCommand(String scrPath, String binPath, String opts, boolean needBinary, boolean needsArgs, String arg, String binArguments,
            String cmdTarget) {
        needsToSendCommand = needBinary;
        needsArguments = needsArgs;
        binaryPath = binPath;
        scriptPath = scrPath;
        arguments = "--runtime=dyninst " + arg; //$NON-NLS-1$
        flags = opts;
        binaryArguments = binArguments;
        command = cmdTarget;


        String[] script = buildScript();

        String cmd = ""; //$NON-NLS-1$
        for (int i = 0; i < script.length-1; i++) {
            cmd = cmd + script[i] + " "; //$NON-NLS-1$
        }
        cmd = cmd + script[script.length-1];

        return cmd;
    }


    /**
     * Parses the data created from generateCommand
     * @return An array of strings to be joined and executed by the shell
     */
    private static String[] buildScript() {
        //TODO: Take care of this in the next release. For now only the guru mode is sent
        ArrayList<String> cmdList = new ArrayList<>();
        String[] script;

        if (flags.length() > 0){
            cmdList.add(flags);
        }

        //Execute a binary
        if (needsToSendCommand){
            if (binaryArguments.length() < 1){
                cmdList.add("-c '" + binaryPath + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                cmdList.add("-c \"" + binaryPath + " " + binaryArguments +"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }


        if (needsArguments) {
            script = new String[cmdList.size() + 3];
            script[script.length-2] = scriptPath;
            script[script.length-1] = arguments;
        } else {
            script = new String[cmdList.size() + 2];
            script[script.length-1] = scriptPath;
        }

        script[0] = command;

        for(int i=0; i< cmdList.size(); i++) {
            if (cmdList.get(i) != null) {
                script[i +1] = cmdList.get(i);
            } else {
                script[i + 1] = ""; //$NON-NLS-1$
            }
        }
        return script;

    }

}
