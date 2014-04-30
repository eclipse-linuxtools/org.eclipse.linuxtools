/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.binutils.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.tools.launch.core.factory.CdtSpawnerProcessFactory;

/**
 * This class launches NM and parses output.
 */
public class STNM {

    private static final Pattern UNDEF_PATTERN = Pattern.compile("^\\s+U\\s+(\\S+)"); //$NON-NLS-1$
    private static final Pattern NORMAL_PATTERN = Pattern.compile("^(\\S+)\\s+([AaTtBbDd])\\s+(\\S+)"); //$NON-NLS-1$

    private final STNMSymbolsHandler handler;

    /**
     * Constructor
     *
     * @param command
     *            the nm to call
     * @param params
     *            nm params
     * @param file
     *            file to parse
     * @param handler The symbol handler.
     * @param project
     *            the project to get the path to use to run nm
     * @throws IOException If an IOException occured.
     */
    public STNM(String command, String[] params, String file, STNMSymbolsHandler handler, IProject project)
            throws IOException {
        this.handler = handler;
        if (handler != null) {
            init(command, params, file, project);
        }
    }

    private void init(String command, String[] params, String file, IProject project) throws IOException {
        String[] args = null;
        if (params == null || params.length == 0) {
            args = new String[] { command, file };
        } else {
            args = new String[params.length + 2];
            args[0] = command;
            args[params.length + 1] = file;
            System.arraycopy(params, 0, args, 1, params.length);
        }
        Process process = CdtSpawnerProcessFactory.getFactory().exec(args, project);
        parseOutput(process.getInputStream());
        process.destroy();
    }

    private void parseOutput(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;

        // See matcher.java for regular expression string data definitions.

        while ((line = reader.readLine()) != null) {
            Matcher undef_matcher = UNDEF_PATTERN.matcher(line);
            Matcher normal_matcher = NORMAL_PATTERN.matcher(line);
            try {
                if (undef_matcher.matches()) {
                    handler.foundUndefSymbol(undef_matcher.group(1));
                } else if (normal_matcher.matches()) {
                    char type = normal_matcher.group(2).charAt(0);
                    String name = normal_matcher.group(3);
                    String address = normal_matcher.group(1);

                    switch (type) {
                    case 'T':
                    case 't':
                        handler.foundTextSymbol(name, address);
                        break;
                    case 'B':
                    case 'b':
                        handler.foundBssSymbol(name, address);
                        break;
                    case 'D':
                    case 'd':
                        handler.foundDataSymbol(name, address);
                        break;
                    }
                }
            } catch (NumberFormatException|IndexOutOfBoundsException e) {
                // ignore
            }
        }

    }

}
