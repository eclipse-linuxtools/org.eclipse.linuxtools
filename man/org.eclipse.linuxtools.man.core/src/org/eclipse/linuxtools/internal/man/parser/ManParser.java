/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc. and others.
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.man.Activator;
import org.eclipse.linuxtools.internal.man.preferences.PreferenceConstants;
import org.eclipse.linuxtools.tools.launch.core.factory.LinuxtoolsProcessFactory;

import com.jcraft.jsch.JSchException;

/**
 * Parser for the man executable output.
 */
public class ManParser {

    /**
     * Gets the list of paths returned when one runs "man -w" with no other
     * parameters. This is the list of directories that is searched by man for
     * man pages.
     * 
     * @return the list of paths in which man searches for man pages in same
     *         order that man would return them
     */
    public static List<Path> getManPaths() {
        // Build param list
        List<String> params = new ArrayList<>();
        params.add(getManExecutable());
        params.add("-w"); //$NON-NLS-1$

        List<Path> manPaths = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder(params);
        try (InputStream stdout = builder.start().getInputStream()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int x;
            while ((x = stdout.read()) != -1) {
                bos.write(x);
            }
            for (String path : bos.toString().trim()
                    .split(File.pathSeparator)) {
                manPaths.add(Paths.get(path));
            }
        } catch (IOException e) {
            Status status = new Status(IStatus.ERROR, e.getMessage(),
                    Activator.getDefault().getPluginId());
            Activator.getDefault().getLog().log(status);
        }
        return manPaths;
    }

    /**
     * Opens a manual page and returns an input stream from which to read it.
     * 
     * @param page
     *            the name of the man page to open
     * @param html
     *            true to open the given man page as an HTML document, false to
     *            open it as a plain text document suitable for display in a
     *            terminal
     * @param sections
     *            a string array of manual sections in which to look for the
     *            given man page
     * @return a new input stream, the caller is responsible for closing it
     */
    public InputStream getManPage(String page, boolean html,
            String... sections) {
        StringBuilder sectionParam = new StringBuilder();
        for (String section : sections) {
            if (sectionParam.length() > 0) {
                sectionParam.append(':');
            }
            sectionParam.append(section);
        }

        // Build param list
        List<String> params = new ArrayList<>();
        params.add(getManExecutable());
        if (page != null && !page.isEmpty() && sectionParam.length() > 0) {
            params.add("-S"); //$NON-NLS-1$
            params.add(sectionParam.toString());
        }
        if (html) {
            params.add("-Thtml"); //$NON-NLS-1$
        }
        params.add(page);

        ProcessBuilder builder = new ProcessBuilder(params);
        InputStream stdout = null;
        try {
            Process process = builder.start();
            stdout = process.getInputStream();
        } catch (IOException e) {
            Status status = new Status(IStatus.ERROR, e.getMessage(),
                    Activator.getDefault().getPluginId());
            Activator.getDefault().getLog().log(status);
        }
        return stdout;
    }

    /**
     * Returns the raw representation of the man executable for a given man page
     * i.e. `man ls`.
     *
     * @param manPage
     *            The man page to fetch.
     * @return Raw output of the man command.
     */
    public StringBuilder getRawManPage(String manPage) {
        StringBuilder sb = new StringBuilder();
        try (InputStream manContent = getManPage(manPage, false);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(manContent))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            Status status = new Status(IStatus.ERROR, e.getMessage(),
                    Activator.getDefault().getPluginId());
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
            LinuxtoolsProcessFactory.execRemoteAndWait(
                    new String[] { getManExecutable(), manPage }, out, out,
                    user, host, password);
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
