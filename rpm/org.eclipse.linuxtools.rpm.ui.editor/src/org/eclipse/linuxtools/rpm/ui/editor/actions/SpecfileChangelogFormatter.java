/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.actions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.linuxtools.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.changelog.core.IFormatterChangeLogContrib;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfilePartitionScanner;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.ui.IEditorPart;

public class SpecfileChangelogFormatter implements IFormatterChangeLogContrib {

    private IEditorPart changelog;

    public String formatDateLine(String authorName, String authorEmail) {
    	String dateLine;
        Specfile specfile = getParsedSpecfile();
        SpecfileElement resolveElement = new SpecfileElement();
        resolveElement.setSpecfile(specfile);
        String epoch = specfile.getEpoch() == -1 ? "" : new String(specfile
                .getEpoch()
                + ":");
        String version = specfile.getVersion() == null ? "" : resolveElement
                .resolve(specfile.getVersion());
        String release = specfile.getRelease() == null ? "" : resolveElement
                .resolve(specfile.getRelease());
        
        // remove the dist macro if it exist in the release string.
        release = release.replaceAll("\\%\\{\\?dist\\}", "");
     
        // default format
        dateLine = "* " + formatTodaysDate() + " " + authorName + " " + "<" + authorEmail
        + ">" + " " + epoch + version + "-" + release;

        String format = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT);
        if (format.equals(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_VERSIONED))
        	return dateLine;
            
        else if (format.equals(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_VERSIONED_WITH_SEPARATOR))
        	dateLine =  "* " + formatTodaysDate() + " " + authorName + " " + "<" + authorEmail
            + ">" + " - " + epoch + version + "-" + release;
        
        else if (format.equals(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_UNVERSIONED))
        	dateLine =  "* " + formatTodaysDate() + " " + authorName + " " + "<" + authorEmail + ">";
        
        return dateLine;

    }

    public String mergeChangelog(String dateLine, String functionGuess,
            IEditorPart changelog, String changeLogLocation, String fileLocation) {
    	return mergeChangelog(dateLine, functionGuess, "", changelog, changeLogLocation, fileLocation);
    }

    protected Specfile getParsedSpecfile() {
        if (changelog == null)
            changelog = ChangelogPlugin.getDefault().getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage()
                    .getActiveEditor();
        if (changelog instanceof SpecfileEditor) {
            SpecfileEditor specEditor = (SpecfileEditor) changelog;
            return specEditor.getSpecfile();
        }
        return null;
    }

    private String formatTodaysDate() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        // Get default locale
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_CHANGELOG_LOCAL)));
        String date = (new SimpleDateFormat("EEE MMM d yyyy"))
                .format(new Date());
        Locale.setDefault(defaultLocale);
        return date;
    }

	public String mergeChangelog(String dateLine, String functionGuess,
			String defaultContent, IEditorPart changelog,
			String changeLogLocation, String fileLocation) {
		if (changelog instanceof SpecfileEditor) {
            SpecfileEditor specEditor = (SpecfileEditor) changelog;
            IDocument doc = specEditor.getDocumentProvider().getDocument(
                    specEditor.getEditorInput());
            String[] positionCategories = doc.getPositionCategories();
            String contentTypesPositionCategory = null;

            // there is some random number suffix to the category positions,
            // we need to find the one we want
            for (int i = 0; i < positionCategories.length; i++) {
                if (positionCategories[i]
                        .startsWith("__content_types_category"))
                    contentTypesPositionCategory = positionCategories[i];
            }

            if (contentTypesPositionCategory != null) {
                try {
                    Position[] sectionPositions = doc
                            .getPositions(contentTypesPositionCategory);
                    ITypedRegion changelogPartition = null;
                    for (int i = 0; i < sectionPositions.length; i++) {
                        Position position = sectionPositions[i];
                        int offset = position.getOffset();

                        ITypedRegion partition = doc.getPartition(offset);
                        if (partition.getType().equals(
                                SpecfilePartitionScanner.SPEC_CHANGELOG))
                            changelogPartition = partition;

                    }
                    // Temporary buffer for changelog text
                    StringBuffer buf = new StringBuffer();
                    String changelogText = "";
                    String[] changelogLines = new String[] {};
                    int offset = doc.getLength();
                    int length = 0;

                    // there was no changelog partition add it.
                    if (changelogPartition == null) {

                        // make sure there are at least 2 newlines before 
                        // the changelog section
                        String endString = doc.get(doc.getLength() - 2, 2);
                        if (endString.charAt(0) != '\n')
                            buf.append("\n");
                        if (endString.charAt(1) != '\n')
                            buf.append("\n");
                        
                        buf.append("%changelog\n");
                        
                    // or get the old text and add the header
                    } else {
                        offset = changelogPartition.getOffset();
                        length = changelogPartition.getLength();
                        changelogText = doc.get(offset, length);

                        // get old changelog text
                        changelogLines = changelogText.split("\n");
                        // add the %changelog header
                        buf.append(changelogLines[0] + "\n");
                    }

                    // now add the entry stub
                    buf.append(dateLine);
                    buf.append("\n");
                    buf.append("- \n");

                    // set the cursor at the end of the entry,
                    // count back 2 '\n's
                    int newCursorOffset = offset + buf.length() - 1;

                    for (int i = 1; i < changelogLines.length; i++) {
                        buf.append("\n" + changelogLines[i]);
                    }
                    
                    // always terminate the file with a new line
                    if (changelogLines.length > 0)
                        buf.append("\n");
                    
                    doc.replace(offset, length, buf.toString());

                    specEditor.selectAndReveal(newCursorOffset, 0);
                    specEditor.setFocus();
                } catch (BadPositionCategoryException e) {
        			SpecfileLog.logError(e);
                } catch (BadLocationException e) {
        			SpecfileLog.logError(e);
                }
            } else {
                // log error, we didn't find content type category positions,
                // WTF?
            }
        } else {
            // TODO: LOg error.
            System.err.println("Got " + changelog.getClass().toString()
                    + " editor");
        }
        return "";
	}

}
