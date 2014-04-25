/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.changelog.core.IFormatterChangeLogContrib;
import org.eclipse.ui.IEditorPart;

/**
 * Writes changelog using extension point IFormatterChangeLogContrib.
 *
 * @author klee
 *
 */
public class ChangeLogWriter {

    private String defaultContent = ""; // $NON-NLS-1$

    private String entryFilePath = null;

    private String guessedFName = null;

    private IFormatterChangeLogContrib formatter = null;

    private IEditorPart changelog = null;

    private String dateLine = null;

    private String changelogLocation = null;

    public IEditorPart getChangelog() {
        return changelog;
    }

    public void setChangelog(IEditorPart changelog) {
        this.changelog = changelog;
    }

    public String getChangelogLocation() {
        return changelogLocation;
    }

    public void setChangelogLocation(String changelogLocation) {
        this.changelogLocation = changelogLocation;
    }

    public String getDateLine() {
        return dateLine;
    }

    public void setDateLine(String dateLine) {
        this.dateLine = dateLine;
    }

    public String getEntryFilePath() {
        return entryFilePath;
    }

    public void setEntryFilePath(String entryFilePath) {
        // Replace characters in the name that are supposed to be
        // token markers such as blanks, parentheses, and colon with
        // escaped characters so they won't fool the colorization or
        // other parsing.
        String resolvedPath = entryFilePath.replace("(", "\\(");
        resolvedPath = resolvedPath.replace(")", "\\)");
        resolvedPath = resolvedPath.replace(":", "\\:");
        resolvedPath = resolvedPath.replace(" ", "\\ ");
        this.entryFilePath = resolvedPath;
    }

    public IFormatterChangeLogContrib getFormatter() {
        return formatter;
    }

    public void setFormatter(IFormatterChangeLogContrib formatter) {
        this.formatter = formatter;
    }

    public String getGuessedFName() {
        return guessedFName;
    }

    public void setGuessedFName(String guessedFName) {
        this.guessedFName = guessedFName;
    }

    public void writeChangeLog() {

        // System.out.println("Debug Output :");
        // System.out.println(entryFilePath);
        // System.out.println(guessedFName);
        // System.out.println(formatter);
        // System.out.println(changelog);
        // System.out.println(dateLine);
        // System.out.println(changelogLocation);
        // System.out.println("\n");

        if (entryFilePath == null || guessedFName == null || formatter == null
                || changelog == null || dateLine == null
                || changelogLocation == null) {
            ChangelogPlugin.getDefault().getLog().log(
                    new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID, IStatus.ERROR,
                            Messages.getString("ChangeLogWriter.ErrUninitialized"), null)); // $NON-NLS-1$

            return;
        }

        formatter.mergeChangelog(dateLine, guessedFName, defaultContent, changelog,
                changelogLocation, entryFilePath);

    }

    public String getDefaultContent() {
        return defaultContent;
    }

    public void setDefaultContent(String defaultContent) {
        this.defaultContent = defaultContent;
    }

}
