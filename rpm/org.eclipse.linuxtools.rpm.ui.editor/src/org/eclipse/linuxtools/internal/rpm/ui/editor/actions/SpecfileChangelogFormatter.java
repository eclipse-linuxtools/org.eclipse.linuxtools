/*******************************************************************************
 * Copyright (c) 2007-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import java.text.MessageFormat;
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
import org.eclipse.linuxtools.changelog.core.IFormatterChangeLogContrib;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.UiUtils;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class SpecfileChangelogFormatter implements IFormatterChangeLogContrib {

    public final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EEE MMM d yyyy"); //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private IEditorPart changelog;

    @Override
	public String formatDateLine(String authorName, String authorEmail) {
    	String dateLine;
        Specfile specfile = getParsedSpecfile();
        SpecfileElement resolveElement = new SpecfileElement();
        resolveElement.setSpecfile(specfile);
        String epoch = specfile.getEpoch() == -1 ? EMPTY_STRING : (specfile.getEpoch() + ":"); //$NON-NLS-1$
        String version = specfile.getVersion();
        String release = specfile.getRelease();

        // remove the dist macro if it exist in the release string.
        release = release.replaceAll("\\%\\{\\?dist\\}", EMPTY_STRING); //$NON-NLS-1$

        // default format
        dateLine = MessageFormat.format("* {0} {1} <{2}> {3}{4}-{5}", formatTodaysDate(), authorName, //$NON-NLS-1$
				authorEmail, epoch, version, release);

        String format = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT);
        if (format.equals(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_VERSIONED_WITH_SEPARATOR)) {
        	dateLine =  MessageFormat.format("* {0} {1} <{2}> - {3}{4}-{5}", formatTodaysDate(), //$NON-NLS-1$
					authorName, authorEmail, epoch, version, release);

        } else if (format.equals(PreferenceConstants.P_CHANGELOG_ENTRY_FORMAT_UNVERSIONED)) {
        	dateLine =  MessageFormat
					.format("* {0} {1} <{2}>", formatTodaysDate(), authorName, authorEmail); //$NON-NLS-1$
        }

       	dateLine = UiUtils.resolveDefines(specfile, dateLine);
        return dateLine;

    }

    protected Specfile getParsedSpecfile() {
        if (changelog == null) {
            changelog = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage()
                    .getActiveEditor();
        }
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
        String date = SIMPLE_DATE_FORMAT.format(new Date());
        Locale.setDefault(defaultLocale);
        return date;
    }

	@Override
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
            for (String positionCategory: positionCategories) {
                if (positionCategory.startsWith("__content_types_category")) {//$NON-NLS-1$
                    contentTypesPositionCategory = positionCategory;
                }
            }

            if (contentTypesPositionCategory != null) {
                try {
                    Position[] sectionPositions = doc
                            .getPositions(contentTypesPositionCategory);
                    ITypedRegion changelogPartition = null;
                    for (Position position: sectionPositions) {
                        int offset = position.getOffset();

                        ITypedRegion partition = doc.getPartition(offset);
                        if (partition.getType().equals(
                                SpecfilePartitionScanner.SPEC_CHANGELOG)) {
                            changelogPartition = partition;
                        }

                    }
                    // Temporary buffer for changelog text
                    StringBuilder buf = new StringBuilder();
                    String changelogText = EMPTY_STRING;
                    String[] changelogLines = new String[] {};
                    int offset = doc.getLength();
                    int length = 0;

                    // there was no changelog partition add it.
                    if (changelogPartition == null) {

                        // make sure there are at least 2 newlines before
                        // the changelog section
                        String endString = doc.get(doc.getLength() - 2, 2);
                        if (endString.charAt(0) != '\n') {
                            buf.append('\n');
                        }
                        if (endString.charAt(1) != '\n') {
                            buf.append('\n');
                        }

                        buf.append("%changelog\n"); //$NON-NLS-1$

                    // or get the old text and add the header
                    } else {
                        offset = changelogPartition.getOffset();
                        length = changelogPartition.getLength();
                        changelogText = doc.get(offset, length);

                        // get old changelog text
                        changelogLines = changelogText.split("\n"); //$NON-NLS-1$
                        // add the %changelog header
                        buf.append(changelogLines[0]).append('\n');
                    }

                    // now add the entry stub
                    buf.append(dateLine);
                    buf.append('\n');
                    buf.append("- \n"); //$NON-NLS-1$

                    // set the cursor at the end of the entry,
                    // count back 2 '\n's
                    int newCursorOffset = offset + buf.length() - 1;
                    for (int i = 1; i < changelogLines.length; i++) {
                        buf.append('\n').append(changelogLines[i]);
                    }

                    // always terminate the file with a new line
                    if (changelogLines.length > 0) {
                        buf.append('\n');
                    }

                    doc.replace(offset, length, buf.toString());

                    specEditor.selectAndReveal(newCursorOffset, 0);
                    specEditor.setFocus();
                } catch (BadPositionCategoryException e) {
        			SpecfileLog.logError(e);
                } catch (BadLocationException e) {
        			SpecfileLog.logError(e);
                }
            }
        }
        return EMPTY_STRING;
	}

}
