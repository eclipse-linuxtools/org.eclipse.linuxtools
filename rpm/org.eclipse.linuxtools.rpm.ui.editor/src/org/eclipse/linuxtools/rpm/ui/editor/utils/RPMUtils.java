/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for RPM UI Editor related things.
 *
 */
public class RPMUtils {

    /**
     * Utility classes should not have a public or default constructor.
     */
    private RPMUtils() {}

    /**
     * Show an error dialog.
     *
     * @param shell A valid shell
     * @param title The error dialog title
     * @param message The message to be displayed.
     */
    public static void showErrorDialog(final Shell shell,
            final String title, final String message) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(shell, title, message);
            }
        });
    }

    /**
     * Check if the line passed in is a valid URL.
     *
     * @param line The line to check if is a valid URL.
     * @return True if valid URL, false otherwise.
     */
    public static boolean isValidUrl(String line) {
        try {
            new URL(line);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Get the file from the URL if any.
     *
     * @param url The URL to get the file from.
     * @return Return the filename.
     */
    public static String getURLFilename(String url) {
        String rc = ""; //$NON-NLS-1$

        try {
            // URL#getPath will ignore any queries after the filename
            String fileName = new URL(url).getPath();
            int lastSegment = fileName.lastIndexOf('/') + 1;
            rc = fileName.substring(lastSegment).trim();
        } catch (IndexOutOfBoundsException e) {
            SpecfileLog.logError(e);
        } catch (MalformedURLException e) {
            SpecfileLog.logError(e);
        }

        return rc;
    }

    /**
     * Check if the file exists within the current project.
     * It will first check the root of the project and then the sources. If the
     * file cannot be found in either, return false.
     * An empty file name would immediately return false.
     * @param original A file in the project.
     * @param fileName The file name being searched.
     *
     * @return True if the file exists.
     */
    public static boolean fileExistsInSources(IFile original, String fileName) {
        if (fileName.trim().isEmpty()) {
            return false;
        }
        IContainer container = original.getParent();
        IResource resourceToOpen = container.findMember(fileName);
        IFile file = null;

        if (resourceToOpen == null) {
            IResource sourcesFolder = container.getProject().findMember(
                    "SOURCES"); //$NON-NLS-1$
            file = container.getFile(new Path(fileName));
            if (sourcesFolder != null) {
                file = ((IFolder) sourcesFolder).getFile(new Path(fileName));
            }
            if (!file.exists()) {
                return false;
            }
        }

        return true;
    }
}
