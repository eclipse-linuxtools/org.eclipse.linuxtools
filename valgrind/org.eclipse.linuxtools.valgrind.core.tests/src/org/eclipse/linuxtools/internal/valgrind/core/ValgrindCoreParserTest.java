/*******************************************************************************
 * Copyright (c) 2015, 2018 QNX Software Systems adn others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.core.tests.AbstractInlineDataTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class ValgrindCoreParserTest extends AbstractInlineDataTest {
	private static final String VALGRIND_OUT1 = "valgrind_01.txt";
	private static final String VALGRIND_OUT2 = "valgrind_02.txt";
	private IValgrindMessage[] messages;
	private ILaunch launchMock;
	private File file;
	@TempDir
	File tmpfiles;

	@BeforeEach
	public void setUp() {
		launchMock = Mockito.mock(ILaunch.class);
		file = new File(tmpfiles, VALGRIND_OUT1);
	}

	private void parse(File file, ILaunch l) throws IOException {
		ValgrindCoreParser valgrindCoreParser = new ValgrindCoreParser(file, l);
		messages = valgrindCoreParser.getMessages();
		assertNotNull(messages);
	}

	private void parse() throws IOException {
		parse(file, launchMock);
	}

	private void parseComment(TestInfo info) throws IOException {
		file = getAboveCommentAndSaveFile(VALGRIND_OUT2, info);
		parse();
	}

	private void checkMessage(int index, IValgrindMessage[] messages, String string) {
		String text = messages[index].getText();
		text = text.replaceFirst(" \\[PID: \\d+\\]", "");
		assertEquals(string, text);
	}

	@Test
	public void test() {
		file.delete();
		launchMock = null;
		assertThrows(IOException.class, ()-> parse());
	}

	//this is pretend message which should be ignored
	@Test
	public void testLaunch(TestInfo info) throws IOException {
		parseComment(info);
		assertEquals(0, messages.length);
	}

	//==00:00:00:01.175 52756728== bla bla
	@Test
	public void testTimestamp(TestInfo info) throws IOException {
		parseComment(info);
		assertEquals(1, messages.length);
		assertEquals("bla bla [PID: 2]", messages[0].getText()); // TODO should be PID 52756728
	}

	//==2== one
	//==2==   two
	@Test
	public void testIndent(TestInfo info) throws IOException {
		parseComment(info);
		assertEquals(1, messages.length);
		assertEquals(1, messages[0].getChildren().length);
		checkMessage(0, messages, "one");
		checkMessage(0, messages[0].getChildren(), "two");
	}
}
