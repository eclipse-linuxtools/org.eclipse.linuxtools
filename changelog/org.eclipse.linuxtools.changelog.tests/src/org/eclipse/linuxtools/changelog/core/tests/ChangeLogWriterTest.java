/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.linuxtools.changelog.core.IFormatterChangeLogContrib;
import org.eclipse.linuxtools.changelog.tests.fixtures.ChangeLogTestProject;
import org.eclipse.linuxtools.changelog.tests.helpers.EditorHelper;
import org.eclipse.linuxtools.internal.changelog.core.ChangeLogWriter;
import org.eclipse.linuxtools.internal.changelog.core.formatters.GNUFormat;

/**
 * @author Severin Gehwolf <sgehwolf@redhat.com>
 * 
 */
public class ChangeLogWriterTest {

	// The instance of the class under test
	private ChangeLogWriter clogWriter;
	// A fake project
	private ChangeLogTestProject project;
	// the path elements to the ChangeLog file, absolute to the project root
	private final String CHANGELOG_FILE_PATH = "/project-name/src/org/";
	private final String CHANGELOG_FILE_NAME = "ChangeLog";
	private String changelogFilePath;
	// IFile handle to '/path/changelog/ChangeLog'
	private IFile changelogFile;
	// Current content of the ChangeLog file
	private final String changeLogContent = "2009-10-14  Some Author  <some.author@example.com>\n\n" +
			"\t* this/file/does/not/really/exist/SpringRoll.java: new file\n\n";
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		clogWriter = new ChangeLogWriter();
		// create a testproject and add a file to it
		project = new ChangeLogTestProject("changelogWriterProject");
		// Generate full path to ChangeLog file
		changelogFilePath = CHANGELOG_FILE_PATH + CHANGELOG_FILE_NAME;
		// add a ChangeLog file to the project at the path specified by
		// CHANGELOG_FILE_PATH_SEGMENTS
		InputStream newFileInputStream = new ByteArrayInputStream(
				changeLogContent.getBytes());
		changelogFile = project.addFileToProject(CHANGELOG_FILE_PATH + CHANGELOG_FILE_NAME, CHANGELOG_FILE_NAME,
				newFileInputStream);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws CoreException {
		// dispose testproject
		project.getTestProject().delete(true, null);
	}

	/**
	 * Test for setting and getting the content of the ChangeLog
	 * file. 
	 */
	@Test
	public void testGetSetChangelog() {
		// Open the ChangeLog file and get the IEdiorPart
		final IEditorPart currentContent = EditorHelper.openEditor(changelogFile);
		clogWriter.setChangelog(currentContent);
		assertEquals(currentContent, clogWriter.getChangelog());
		EditorHelper.closeEditor(currentContent);
	}

	@Test
	public void testGetSetChangelogLocation() {
		final String path = "/path/to/changelog/file/ChangeLog";
		clogWriter.setChangelogLocation(path);
		assertEquals(path, clogWriter.getChangelogLocation());
	}

	@Test
	public void testGetSetDateLine() {
		String authorName = "Test Author";
		String email = "spongebob@commedycentral.com";
		clogWriter.setDateLine(new GNUFormat().formatDateLine(
				authorName, email));
		
		// Today's date in ISO format
		Calendar c = new GregorianCalendar();
		String isoDate = String.format("%1$tY-%1$tm-%1$td", c);
		
		// expected date/author line
		String expectedDateLine = isoDate + "  " + authorName + "  <" + email + ">\n\n";
		assertEquals(expectedDateLine, clogWriter.getDateLine());
	}

	@Test
	public void testGetSetEntryFilePath() {
		final String entryFilePath = "/some/path/to/some/File.java";
		clogWriter.setEntryFilePath(entryFilePath);
		assertEquals(entryFilePath, clogWriter.getEntryFilePath());
	}

	@Test
	public void testGetSetFormatter() {
		IFormatterChangeLogContrib formatter = new GNUFormat();
		clogWriter.setFormatter(formatter);
		assertEquals(formatter, clogWriter.getFormatter());
	}

	@Test
	public void testGetSetGuessedFName() {
		final String guessedFunctionName = "getInstance";
		clogWriter.setGuessedFName(guessedFunctionName);
		assertEquals(guessedFunctionName, clogWriter.getGuessedFName());
	}

	/**
	 * Note that there can be several Changelogs inside a directory tree.
	 * The {@link ChangeLogWriter#writeChangeLog()} code assumes that the full path to 
	 * the ChangeLog file and the full path to the file for which to generate a ChangeLog
	 * entry have the same ancestor (with some potential overlap).
	 * 
	 * Consider the following example:
	 * 
	 * 1. The ChangeLog file is <project-root>/src/ChangeLog
	 * 2. The currently open editor contains code of <project-root>/src/org/eclipse/example/Test.java
	 * 
	 * In the above case entries in <project-root>/src/ChangeLog *should* be of the form:
	 * 
	 * <code>
	 * 
	 * YYYY-MM-DD  Author Name  <email@example.com>
	 * 
	 *    * org/eclipse/example/Test.java: new File
	 * 
	 * </code>
	 * 
	 * Similarly, if the ChangeLog file is in <project-root>/ChangeLog and the currently open
	 * file is <project-root>/src/org/eclipse/example/Sun.java, generated entries in 
	 * <project-root>/ChangeLog would look like (note the "src" path is added in this case):
	 * 
	 * <code>
	 * 
	 * YYYY-MM-DD  Author Name  <email@example.com>
	 * 
	 *    * src/org/eclipse/example/Sun.java: new File
	 * 
	 * </code>
	 * 
	 * Test for method {@link org.eclipse.linuxtools.internal.changelog.core.ChangeLogWriter#writeChangeLog()}
	 */
	@Test
	public void testWriteChangeLog() throws Exception {
		// We want paths up to the ChangeLog file to overlap
		
		final String pathRelativeToChangeLog = "eclipse/example/test/NewCoffeeMaker.java";
		clogWriter.setEntryFilePath( CHANGELOG_FILE_PATH + pathRelativeToChangeLog );

		// Will show up surrounded by "(" and ")" in ChangeLog
		String guessedFunctionName = "bazinga";
		clogWriter.setGuessedFName(guessedFunctionName);

		// set GNU formatter
		clogWriter.setFormatter(new GNUFormat());

		// Open a document and get the IEditorPart
		IEditorPart editorContent = EditorHelper.openEditor(changelogFile);
		clogWriter.setChangelog(editorContent);

		// set date/author line
		String authorName = "Test Author";
		String email = "test@example.com";
		clogWriter.setDateLine(clogWriter.getFormatter().formatDateLine(
				authorName, email));

		// full absolute path to ChangeLog file (relative to project root)
		clogWriter.setChangelogLocation(changelogFilePath);

		// Write changelog to buffer - need to save for persistence
		clogWriter.writeChangeLog();
		
		// above written content is not persistent yet; save it to make it persistent
		clogWriter.getChangelog().doSave(null);
		
		// Today's date in ISO format
		Calendar c = new GregorianCalendar();
		String isoDate = String.format("%1$tY-%1$tm-%1$td", c);

		// Construct the changelog entry by hand and match it with what has been written
		String expectedChangeLogEntry = isoDate + "  " + authorName + "  <" + email + ">\n\n";
		expectedChangeLogEntry += "\t* " + pathRelativeToChangeLog + " (" + guessedFunctionName + "): \n\n";
		
		String expectedContent = expectedChangeLogEntry + changeLogContent;
		
		// Read in content written to file
		StringBuffer actualContent = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(changelogFile.getLocation().toFile())))) {
			String line;
			while ((line = br.readLine()) != null) {
				actualContent.append(line + "\n");
			}
		}
		// Assert proper content has been added
		assertEquals(expectedContent, actualContent.toString());
		EditorHelper.closeEditor(editorContent);
	}

	@Test
	public void testGetSetDefaultContent() {
		final String defaultContent = "DISCLAIMER: default ChangeLog content for new files?";
		clogWriter.setDefaultContent(defaultContent);
		assertEquals(defaultContent, clogWriter.getDefaultContent());
	}
	
	
	/**
	 * Test the use of default text.
	 *
	 * @throws Exception
	 */
	@Test
	public void canWriteChangeLogToEmptyChangeLogButWithSomeDefaultContent() throws Exception {
		// set GNU formatter
		clogWriter.setFormatter(new GNUFormat());

		// Open up a new ChangeLog file at newPathToChangeLog with empty content
		// and get the IEditorPart
		InputStream newFileInputStream = new ByteArrayInputStream(
				"".getBytes()); // no content
		String destinationPath = "/this/is/some/random/path";
		IFile emptyChangeLogFile = project.addFileToProject(destinationPath, CHANGELOG_FILE_NAME,
				newFileInputStream);
		IEditorPart editorContent = EditorHelper.openEditor(emptyChangeLogFile);
		clogWriter.setChangelog(editorContent);

		String authorName = "Test Author";
		String email = "test@example.com";
		clogWriter.setDateLine(clogWriter.getFormatter().formatDateLine(
				authorName, email));
		clogWriter.setChangelogLocation(destinationPath + "/" + CHANGELOG_FILE_NAME);

		// Set some default content
		String defaultContent = "Removed.";
		clogWriter.setDefaultContent(defaultContent);
		
		String relativePathOfChangedFile = "path/to/file/for/new/entry/test.c";
		clogWriter.setEntryFilePath( destinationPath + "/" + relativePathOfChangedFile  );
		
		clogWriter.setGuessedFName("");
		
		// Write changelog to buffer - need to save for persistence
		clogWriter.writeChangeLog();
		
		// above written content is not persistent yet; save it to make it persistent
		clogWriter.getChangelog().doSave(null);
		
		// Construct the changelog entry by hand and match it with what has been written
		String expectedChangeLogEntry = new GNUFormat().formatDateLine(authorName, email);
		expectedChangeLogEntry += "\t* " + relativePathOfChangedFile + ": "
			+ defaultContent + "\n";
		
		// Read in content written to file
		StringBuffer actualContent = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(emptyChangeLogFile.getLocation().toFile())))) {
			String line;
			while ((line = br.readLine()) != null) {
				actualContent.append(line + "\n");
			}
		}
		// Assert proper content has been added
		assertEquals(expectedChangeLogEntry, actualContent.toString());
		EditorHelper.closeEditor(editorContent);
	}
}
