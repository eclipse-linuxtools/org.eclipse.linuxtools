/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.formatters.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.changelog.tests.fixtures.ChangeLogTestProject;
import org.eclipse.linuxtools.changelog.tests.helpers.EditorHelper;
import org.eclipse.linuxtools.internal.changelog.core.formatters.GNUFormat;

import static org.eclipse.linuxtools.changelog.tests.helpers.EditorHelper.closeEditor;
import static org.eclipse.linuxtools.changelog.tests.helpers.EditorHelper.openEditor;
import static org.eclipse.linuxtools.changelog.tests.helpers.EditorHelper.getContent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.jface.text.IDocument;

public class GNUFormatTest {

    // The instance under test
    private GNUFormat gnuFormatter;
    // A faked project
    private ChangeLogTestProject project;
    // The IEditorPart corresponding to the ChangeLog file
    private IEditorPart changelogEditorPart = null;

    // Tokens for the GNU format
    private static final String TWO_SPACES = "  ";
    private static final String SPACE = " ";
    private static final String TAB = "\t";
    private static final String LEFT_EMAIL_BRACKET = "<";
    private static final String RIGHT_EMAIL_BRACKET = ">";
    private static final String FILE_ENTRY_START_MARKER = "* ";
    private static final String FILE_ENTRY_END_MARKER = ": ";
    private static final String FUNCTION_START_MARKER = "(";
    private static final String FUNCTION_END_MARKER = ")";
    private static final String NEW_LINE = "\n";

    @Before
    public void setUp() throws Exception {
        gnuFormatter = new GNUFormat();
        project = new ChangeLogTestProject("GNUFormatterTest");
    }

    @After
    public void tearDown() throws Exception {
        // Most tests in this class use changelogEditorPart. In order to avoid
        // spill-over from previous runs, truncate content (i.e. manually set
        // content to the empty string).
        if (changelogEditorPart != null) { // testFormatDateLine does not use it
            AbstractTextEditor castEditor = (AbstractTextEditor) changelogEditorPart;
            IDocumentProvider iDocProvider = castEditor.getDocumentProvider();
            IDocument changelogContentDoc = iDocProvider.getDocument(castEditor.getEditorInput());
            changelogContentDoc.set(""); // empty content
            changelogEditorPart.doSave(null);
            // Also close open editor in order for default content to work.
            // I.e. avoid spill over from previous test runs
            closeEditor(changelogEditorPart);
        }
        project.getTestProject().delete(true, true, null); // dispose
    }

    /**
     * GNU ChangeLog style date lines are of the following format
     *
     * YYYY-MM-DD  Author Name  <author.email@domain.com>
     */
    @Test
    public void testFormatDateLine() {
        // Today's date in ISO format
        Calendar c = new GregorianCalendar();
        String isoDate = String.format("%1$tY-%1$tm-%1$td", c);
        String authorName = "William Shakespeare";
        String authorEmail = "william.shakespeare@medieval.com";
        final String expectedDateFormatting = isoDate +
                                              TWO_SPACES +
                                              authorName +
                                              TWO_SPACES +
                                              LEFT_EMAIL_BRACKET +
                                              authorEmail +
                                              RIGHT_EMAIL_BRACKET +
                                              "\n\n";
        assertEquals(expectedDateFormatting,
                gnuFormatter.formatDateLine(authorName, authorEmail));
    }

    /**
     * Assume the following initial content of ChangeLog:
     *
     * <code>
     * 2010-11-26  Severin Gehwolf  <sgehwolf@redhat.com>
     *
     *   * src/org/eclipse/linuxtools/changelog/parsers/java/HelloWorld.java (test): New Method\n\t\n
     * </code>
     *
     * Note the trailing new-line, tab, new-line combination. If another change in the same file but
     * in a different method (or function) is merged, the empty line containing a tab only should be
     * removed.
     *
     */
    @Test
    public void twoChangesInSameFileAreProperlyMergedWhenThereIsATrailingTabNewLine() throws Exception {
        // set date/author line
        String authorName = "Test Foo";
        String email = "test@example.com";
        final String dateLine = gnuFormatter.formatDateLine(authorName, email);

        // full absolute path to ChangeLog file (relative to project root)
        String changelogPath = "/" + project.getTestProject().getName() + "/path/to";
        final String changelogFilePath = changelogPath + "/ChangeLog";

        // entry file path (need overlap with changelogPath)
        String fileEntryRelPath = "eclipse/example/test/NewCoffeeMaker.java";
        final String entryFilePath = changelogPath + "/" + fileEntryRelPath;

        final String firstMethodName = "main";
        final String firstChangeComment = "Fix args parsing.";
        // Setup proper pre-existing content
        String content = dateLine + NEW_LINE + NEW_LINE + TAB + FILE_ENTRY_START_MARKER +
                         fileEntryRelPath + SPACE + FUNCTION_START_MARKER +
                         firstMethodName + FUNCTION_END_MARKER + FILE_ENTRY_END_MARKER +
                         firstChangeComment + NEW_LINE +
                         TAB + NEW_LINE; // produces an empty line which should be removed

        assertNull(project.getTestProject().findMember(new Path("/path/to/ChangeLog")));

        // add a ChangeLog file to our test project
        InputStream newFileInputStream = new ByteArrayInputStream(
                content.getBytes());
        IFile changelogFile = project.addFileToProject( "/path/to", "ChangeLog",
                newFileInputStream);

        assertNotNull(project.getTestProject().findMember(new Path("/path/to/ChangeLog")));

        // Open a document and get the IEditorPart
        changelogEditorPart = EditorHelper.openEditor(changelogFile);

        // make sure changelog editor content is right before merging
        assertEquals(content, getContent(changelogEditorPart));

        final String secondMethodName = "toString";

        // Do the merge with the existing content
        gnuFormatter.mergeChangelog(dateLine, secondMethodName,
                "" /* no default content */, changelogEditorPart,
                changelogFilePath, entryFilePath);

        final String actualMergeResult = getContent(changelogEditorPart);

        // Expect trailing tab+newline combination to not show up in merge
        final String expectedResult = dateLine + NEW_LINE + NEW_LINE + TAB + FILE_ENTRY_START_MARKER +
                                       fileEntryRelPath + SPACE + FUNCTION_START_MARKER +
                                       firstMethodName + FUNCTION_END_MARKER + FILE_ENTRY_END_MARKER +
                                       firstChangeComment + NEW_LINE + TAB + FUNCTION_START_MARKER +
                                       secondMethodName + FUNCTION_END_MARKER + FILE_ENTRY_END_MARKER +
                                       NEW_LINE + NEW_LINE;
        assertEquals(expectedResult, actualMergeResult);
    }

    /**
     * Test merge of two separate changes to an existing file within the same changelog entry.
     * I.e. test for
     *
     *    * path/to/file (method1): First change.
     *    (method2): Another change in same file but different function/method.
     */
    @Test
    public void twoChangesInSameFileAreProperlyMerged() throws Exception {
        // set date/author line
        String authorName = "Test Author";
        String email = "test@example.com";
        final String dateLine = gnuFormatter.formatDateLine(authorName, email);

        // full absolute path to ChangeLog file (relative to project root)
        String changelogPath = "/" + project.getTestProject().getName() + "/path/to";
        final String changelogFilePath = changelogPath + "/ChangeLog";

        // entry file path (need overlap with changelogPath)
        String fileEntryRelPath = "eclipse/example/test/NewCoffeeMaker.java";
        final String entryFilePath = changelogPath + "/" + fileEntryRelPath;

        final String firstMethodName = "main";
        final String firstChangeComment = "Fix args parsing.";
        // Setup proper pre-existing content
        String content = dateLine + NEW_LINE + NEW_LINE + TAB + FILE_ENTRY_START_MARKER +
                         fileEntryRelPath + SPACE + FUNCTION_START_MARKER +
                         firstMethodName + FUNCTION_END_MARKER + FILE_ENTRY_END_MARKER +
                         firstChangeComment + NEW_LINE;

        assertNull(project.getTestProject().findMember(new Path("/path/to/ChangeLog")));

        // add a ChangeLog file to our test project
        InputStream newFileInputStream = new ByteArrayInputStream(
                content.getBytes());
        IFile changelogFile = project.addFileToProject( "/path/to", "ChangeLog",
                newFileInputStream);

        assertNotNull(project.getTestProject().findMember(new Path("/path/to/ChangeLog")));

        // Open a document and get the IEditorPart
        changelogEditorPart = openEditor(changelogFile);
        // make sure changelog editor content is right before merging
        assertEquals(content, getContent(changelogEditorPart));

        final String secondMethodName = "toString";

        // Do the merge with the existing content
        gnuFormatter.mergeChangelog(dateLine, secondMethodName,
                "" /* no default content */, changelogEditorPart,
                changelogFilePath, entryFilePath);

        final String actualMergeResult = getContent(changelogEditorPart);

        final String expectedResult = dateLine + NEW_LINE + NEW_LINE + TAB + FILE_ENTRY_START_MARKER +
                                       fileEntryRelPath + SPACE + FUNCTION_START_MARKER +
                                       firstMethodName + FUNCTION_END_MARKER + FILE_ENTRY_END_MARKER +
                                       firstChangeComment + NEW_LINE + TAB + FUNCTION_START_MARKER +
                                       secondMethodName + FUNCTION_END_MARKER + FILE_ENTRY_END_MARKER +
                                       NEW_LINE;

        assertEquals(expectedResult, actualMergeResult);
    }

    /**
     * If two different authors make modifications on the same day, both modifications must
     * show up as separate changelog entries for that day.
     */
    @Test
    public void newChangeLogEntryForNewAuthorOnSameDay() throws Exception {
        // first author
        String authorName = "Test Author";
        String email = "test@example.com";
        final String firstDateLine = gnuFormatter.formatDateLine(authorName, email);
        // second author
        authorName = "William Shakespeare";
        email = "will@pear.com";
        final String secondDateLine = gnuFormatter.formatDateLine(authorName, email);

        // full absolute path to ChangeLog file
        String changelogPath = "/" + project.getTestProject().getName() + "/path/to";
        final String changelogFilePath = changelogPath + "/ChangeLog";

        // No existing content in ChangeLog file
        String content = "";

        assertNull(project.getTestProject().findMember(new Path("/path/to/ChangeLog")));

        // add a ChangeLog file to our test project
        InputStream newFileInputStream = new ByteArrayInputStream(
                content.getBytes());
        IFile changelogFile = project.addFileToProject( changelogPath, "ChangeLog",
                newFileInputStream);
        // Open a document and get the IEditorPart
        changelogEditorPart = openEditor(changelogFile);

        // entry file path (needs overlap with changelogPath)
        String fileEntryRelPath = "eclipse/example/test/NewCoffeeMaker.java";
        final String entryFilePath = changelogPath + "/" + fileEntryRelPath;

        final String guessedFunctionName = "bazinga";

        // merge first changelog entry with empty content
        gnuFormatter.mergeChangelog(firstDateLine, guessedFunctionName,
                "" /* empty default content */, changelogEditorPart,
                changelogFilePath, entryFilePath);

        String actualMergeResult = getContent(changelogEditorPart);

        String expectedResult = firstDateLine + TAB + FILE_ENTRY_START_MARKER +
                                      fileEntryRelPath + SPACE + FUNCTION_START_MARKER +
                                      guessedFunctionName + FUNCTION_END_MARKER +
                                      FILE_ENTRY_END_MARKER;

        assertEquals(expectedResult, actualMergeResult);

        // add second changelog entry on same date but by different author
        gnuFormatter.mergeChangelog(secondDateLine, guessedFunctionName,
                "" /* empty default content */, changelogEditorPart,
                changelogFilePath, entryFilePath);

        actualMergeResult = getContent(changelogEditorPart);

        expectedResult = secondDateLine + TAB + FILE_ENTRY_START_MARKER +
                                      fileEntryRelPath + SPACE + FUNCTION_START_MARKER +
                                      guessedFunctionName + FUNCTION_END_MARKER +
                                      FILE_ENTRY_END_MARKER + NEW_LINE + NEW_LINE +
                                      expectedResult;

        assertEquals(expectedResult, actualMergeResult);
    }

    /**
     * Basic format check.
     */
    @Test
    public void mergeChangeLogHasProperFormat() throws Exception {
        // set date/author line
        String authorName = "Test Author";
        String email = "test@example.com";
        final String dateLine = gnuFormatter.formatDateLine(authorName, email);

        // full absolute path to ChangeLog file (relative to project root)
        String changelogPath = "/" + project.getTestProject().getName() + "/path/to";
        final String changelogFilePath = changelogPath + "/ChangeLog";

        // Content to merge into
        String content = "";

        assertNull(project.getTestProject().findMember(new Path("/path/to/ChangeLog")));

        // add a ChangeLog file to our test project
        InputStream newFileInputStream = new ByteArrayInputStream(
                content.getBytes());
        IFile changelogFile = project.addFileToProject( "/path/to", "ChangeLog",
                newFileInputStream);
        // Open a document and get the IEditorPart
        changelogEditorPart = openEditor(changelogFile);

        // make sure changelog editor content is empty
        assertEquals(content, getContent(changelogEditorPart));

        // entry file path (need overlap with changelogPath)
        String fileEntryRelPath = "eclipse/example/test/NewCoffeeMaker.java";
        final String entryFilePath = changelogPath + "/" + fileEntryRelPath;

        // Will show up surrounded by "(" and ")" in ChangeLog
        final String guessedFunctionName = "bazinga";

        // This always returns an empty String (should probably be changed...)
        // merge result will be written to editorContent
        gnuFormatter.mergeChangelog(dateLine, guessedFunctionName,
                "" /* empty default content */, changelogEditorPart,
                changelogFilePath, entryFilePath);

        final String actualMergeResult = getContent(changelogEditorPart);

        final String expectedResult = dateLine + TAB + FILE_ENTRY_START_MARKER +
                                      fileEntryRelPath + SPACE + FUNCTION_START_MARKER +
                                      guessedFunctionName + FUNCTION_END_MARKER +
                                      FILE_ENTRY_END_MARKER;

        assertEquals(expectedResult, actualMergeResult);
    }

    /**
     * Here's the scenario for this test. It may be that there are only files removed and
     * new files added for a commit. This should yield to changelog entries for the following
     * form:
     *
     *<code>
     * 2010-11-26  Some Author  <some.author@example.com>
     *
     *     * path/to/deleted/file.c: Removed.
     *     * path/to/new/file.c: New file.
     *</code>
     *
     * Prior to the fix for Eclipse Bz #331244 the result was:
     *
     *<code>
     * 2010-11-26  Some Author  <some.author@example.com>
     *
     *     * path/to/deleted/file.c: Removed.
     *     * path/to/new/file.c:
     *</code>
     *
     * This regression test should catch this.
     */
    @Test
    public void canHaveEntriesWithDefaultTextOnly() throws Exception {
        // set date/author line
        String authorName = "Test Author";
        String email = "test@example.com";
        final String dateLine = gnuFormatter.formatDateLine(authorName, email);

        // full absolute path to ChangeLog file (relative to project root)
        String changelogPath = "/" + project.getTestProject().getName() + "/path/example";
        final String changelogFilePath = changelogPath + "/ChangeLog";

        // Content to merge into
        String content = "";

        // add a ChangeLog file to our test project
        InputStream newFileInputStream = new ByteArrayInputStream(
                content.getBytes());
        IFile changelogFile = project.addFileToProject( "/path/example", "ChangeLog",
                newFileInputStream);
        // Open a document and get the IEditorPart
        changelogEditorPart = openEditor(changelogFile);

        // make sure changelog editor content is empty
        assertEquals(content, getContent(changelogEditorPart));

        // entry file path (need overlap with changelogPath)
        final String firstFileEntryRelPath = "eclipse/example/test/NewCoffeeMaker.java";
        String entryFilePath = changelogPath + "/" + firstFileEntryRelPath;

        final String firstDefaultContent = "New file.";

        // Note: This always returns an empty String (should probably be changed...)
        // merge result will be written to editorContent.
        //
        // Create a line with default text "New file"
        gnuFormatter.mergeChangelog(dateLine, "" /* no guessed function name */,
                firstDefaultContent, changelogEditorPart,
                changelogFilePath, entryFilePath);
        final String secondFileEntryRelPath = "eclipse/example/test/OldCoffeeMaker.java";
        entryFilePath = changelogPath + "/" + secondFileEntryRelPath;
        final String secondDefaultContent = "Removed.";
        // Add entry for removed file
        gnuFormatter.mergeChangelog(dateLine, "" /* no guessed function name */,
                secondDefaultContent,    changelogEditorPart,
                changelogFilePath, entryFilePath);

        final String actualMergeResult = getContent(changelogEditorPart);

        // Note: Removed files occur in the list first, new file items come after that.
        final String expectedResult = dateLine + TAB + FILE_ENTRY_START_MARKER +
                                      secondFileEntryRelPath+ FILE_ENTRY_END_MARKER +
                                      secondDefaultContent + NEW_LINE + TAB + FILE_ENTRY_START_MARKER +
                                      firstFileEntryRelPath + FILE_ENTRY_END_MARKER +
                                      firstDefaultContent;

        assertEquals(expectedResult, actualMergeResult);
    }

    /**
     * Test for a changelog entry with items for removed files, new files and modified
     * existing files. This test differs from the previous in that the ChangeLog is
     * empty to start with.  This verifies Bz #366854 fix.
     *
     * @throws Exception
     */
    @Test
    public void canHaveEntriesWithDefaultTextAndSomeModificationToAnExistingFile2() throws Exception {
        // set date/author line
        String authorName = "Test Author";
        String email = "test@example.com";
        final String dateLine = gnuFormatter.formatDateLine(authorName, email);

        // full absolute path to ChangeLog file (relative to project root)
        String changelogPath = "/" + project.getTestProject().getName() + "/test/example";
        final String changelogFilePath = changelogPath + "/ChangeLog";

        // add a new empty ChangeLog file to our test project
        InputStream newFileInputStream = new ByteArrayInputStream(new byte[0]);
        IFile changelogFile = project.addFileToProject( "/test/example", "ChangeLog",
                newFileInputStream);
        // Open a document and get the IEditorPart
        changelogEditorPart = openEditor(changelogFile);

        // entry file path (need overlap with changelogPath)
        final String firstFileEntryRelPath = "eclipse/example/test/NewCoffeeMaker.java";
        String entryFilePath = changelogPath + "/" + firstFileEntryRelPath;

        final String firstDefaultContent = "New file.";

        // Note: This always returns an empty String (should probably be changed...)
        // merge result will be written to editorContent.
        //
        // Create an item with default text "New file"
        gnuFormatter.mergeChangelog(dateLine, "" /* no guessed function name */,
                firstDefaultContent, changelogEditorPart,
                changelogFilePath, entryFilePath);
        final String secondFileEntryRelPath = "eclipse/example/test/OldCoffeeMaker.java";
        entryFilePath = changelogPath + "/" + secondFileEntryRelPath;
        final String secondDefaultContent = "Removed.";
        // Add entry for removed file
        gnuFormatter.mergeChangelog(dateLine, "" /* no guessed function name */,
                secondDefaultContent,    changelogEditorPart,
                changelogFilePath, entryFilePath);
        final String thirdFileEntryRelPath = "eclipse/example/test/ModifiedFile.java";
        entryFilePath = changelogPath + "/" + thirdFileEntryRelPath;
        final String guessedFunctionName = "main";
        // Create a bullet point describing a change in some existing file
        gnuFormatter.mergeChangelog(dateLine, guessedFunctionName,
                "" /* no default content */, changelogEditorPart,
                changelogFilePath, entryFilePath);

        final String actualMergeResult = getContent(changelogEditorPart);

        // Note that changes to existing files appear first in the changelog entries.
        // Second are removed files and new files are last.
        final String expectedResult = dateLine + TAB + FILE_ENTRY_START_MARKER +
                                        thirdFileEntryRelPath + SPACE + FUNCTION_START_MARKER +
                                        guessedFunctionName + FUNCTION_END_MARKER + FILE_ENTRY_END_MARKER +
                                        NEW_LINE + TAB + FILE_ENTRY_START_MARKER +
                                      secondFileEntryRelPath+ FILE_ENTRY_END_MARKER +
                                      secondDefaultContent + NEW_LINE + TAB + FILE_ENTRY_START_MARKER +
                                      firstFileEntryRelPath + FILE_ENTRY_END_MARKER +
                                      firstDefaultContent;

        assertEquals(expectedResult, actualMergeResult);
    }

}
