/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.man.Activator;
import org.eclipse.linuxtools.internal.man.preferences.PreferenceConstants;
import org.eclipse.linuxtools.tools.launch.core.factory.LinuxtoolsProcessFactory;

import com.jcraft.jsch.JSchException;

/**
 * Parser for the man executable output.
 *
 */
public class ManParser {

    /**
     * Returns the raw representation of the man executable for a given man page
     * i.e. `man ls`.
     *
     * @param manPage
     *            The man page to fetch.
     * @return Raw output of the man command.
     */
    public StringBuilder getRawManPage(String manPage) {
        ProcessBuilder builder = new ProcessBuilder(getManExecutable(), manPage);
        builder.redirectErrorStream(true);
        Process process;
        StringBuilder sb = new StringBuilder();
        try {
            process = builder.start();
            if (!(System.getProperty("os.name").toLowerCase() //$NON-NLS-1$
                    .indexOf("windows") == 0)) { //$NON-NLS-1$
                process.waitFor();
            }

            String line = null;
            try (InputStream manContent = process.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(manContent))) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n"); //$NON-NLS-1$
                }
            }
        } catch (IOException | InterruptedException e1) {
            Status status = new Status(IStatus.ERROR, e1.getMessage(),
                    Activator.PLUGIN_ID);
            Activator.getDefault().getLog().log(status);
        }
        return sb;
    }

    /**
     * Returns the raw representation of the man page of an executable on a
     * remote machine.
     *
     * @param manPage
     *            The man page to fetch.
     * @param user
     *            The name of the user to access the man page as.
     * @param host
     *            The name of host where the man page is to be fetched from.
     * @param password
     *            The user's login password.
     * @return Raw output of the man command.
     */
    public StringBuilder getRemoteRawManPage(String manPage, String user,
            String host, String password) {
        final StringBuilder sb = new StringBuilder();
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                sb.append((char) b);
            }
        };
        try {
            LinuxtoolsProcessFactory.execRemoteAndWait(new String[] {
                    getManExecutable(), manPage }, out, out, user, host,
                    password);
        } catch (JSchException e) {
            sb.setLength(0);
            sb.append(Messages.ManParser_RemoteAccessError);
        }
        return sb;
    }

    private static String getManExecutable() {
        return Activator.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.P_PATH);
    }
}
