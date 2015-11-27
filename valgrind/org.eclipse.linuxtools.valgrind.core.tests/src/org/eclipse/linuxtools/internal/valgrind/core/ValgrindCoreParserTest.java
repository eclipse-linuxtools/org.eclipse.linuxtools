/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.core.tests.AbstractInlineDataTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ValgrindCoreParserTest extends AbstractInlineDataTest {
	private static final String VALGRIND_OUT1 = "valgrind_01.txt";
	private static final String VALGRIND_OUT2 = "valgrind_02.txt";
	private IValgrindMessage[] messages;
	private ILaunch launchMock;
	private File file;

	@Before
	public void setUp() throws IOException {
		launchMock = Mockito.mock(ILaunch.class);
		file = tmpfiles.newFile(VALGRIND_OUT1);
	}

	private void parse(File file, ILaunch l) throws IOException {
		ValgrindCoreParser valgrindCoreParser = new ValgrindCoreParser(file, l);
		messages = valgrindCoreParser.getMessages();
		assertNotNull(messages);
	}

	private void parse() throws IOException {
		parse(file, launchMock);
	}

	private void parseComment() throws IOException {
		file = getAboveCommentAndSaveFile(VALGRIND_OUT2);
		parse();
	}

	private void checkMessage(int index, IValgrindMessage[] messages, String string) {
		String text = messages[index].getText();
		text = text.replaceFirst(" \\[PID: \\d+\\]", "");
		assertEquals(string, text);
	}

	@Test(expected = IOException.class)
	public void test() throws IOException {
		file.delete();
		launchMock = null;
		parse();
	}

	//this is pretend message which should be ignored
	@Test
	public void testLaunch() throws IOException {
		parseComment();
		assertEquals(0, messages.length);
	}

	//==00:00:00:01.175 52756728== bla bla
	@Test
	public void testTimestamp() throws IOException {
		parseComment();
		assertEquals(1, messages.length);
		assertEquals("bla bla [PID: 2]", messages[0].getText()); // TODO should be PID 52756728
	}

	//==2== one
	//==2==   two
	@Test
	public void testIndent() throws IOException {
		parseComment();
		assertEquals(1, messages.length);
		assertEquals(1, messages[0].getChildren().length);
		checkMessage(0, messages, "one");
		checkMessage(0, messages[0].getChildren(), "two");
	}
}
