/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.CommandLineConstants;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.core.ValgrindParserUtils;

public class ValgrindCoreParser {
	private static final String AT = "at"; //$NON-NLS-1$
	private static final String BY = "by"; //$NON-NLS-1$

	protected List<IValgrindMessage> messages;
	protected int pid;
	protected ILaunch launch;

	public ValgrindCoreParser(File inputFile, ILaunch launch) throws IOException {
		this.launch = launch;
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		// keep track of nested messages and their corresponding indents
		Stack<IValgrindMessage> messageStack = new Stack<IValgrindMessage>();
		Stack<Integer> indentStack = new Stack<Integer>();
		messages = new ArrayList<IValgrindMessage>();

		try { 
			pid = ValgrindParserUtils.parsePID(inputFile.getName(), CommandLineConstants.LOG_PREFIX);
			String line;
			while ((line = br.readLine()) != null) {
				// remove PID string
				// might encounter warnings also #325130
				line = line.replaceFirst("==\\d+==|\\*\\*\\d+\\*\\*", ""); //$NON-NLS-1$ //$NON-NLS-2$

				int indent;
				for (indent = 0; indent < line.length()
				&& line.charAt(indent) == ' '; indent++){}

				line = line.trim();
				if (!line.isEmpty()) { 
					/*
					 * indent == 1 -> top level message
					 * indent > 1 -> child message
					 * indent == 0 -> should not occur
					 */
					if (indent == 1) {
						// top-level message, clear stacks
						IValgrindMessage message = getMessage(null, line);
						messages.add(message);
						messageStack.clear();
						messageStack.push(message);
						indentStack.clear();
						indentStack.push(indent);
					}
					else if (indent > 1) {
						/**
						 * We assume that an indented child message has a
						 * parent, but this may not be the case.
						 * See BZ #360225
						 */
						if (indentStack.isEmpty()){
							// pretend this is a top level message
							IValgrindMessage message = getMessage(null, line);
							messages.add(message);
							messageStack.clear();
							messageStack.push(message);
							indentStack.clear();
							indentStack.push(1);
						}else{
							// find this message's parent
							while (indent <= indentStack.peek()) {
								messageStack.pop();
								indentStack.pop();
							}
							
							messageStack.push(getMessage(messageStack.peek(), line));
							indentStack.push(indent);
						}
					}
				}
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	protected IValgrindMessage getMessage(IValgrindMessage message, String line) {
		if (line.startsWith(AT) || line.startsWith(BY)) {
			Object[] parsed = ValgrindParserUtils.parseFilename(line);
			String filename = (String) parsed[0];
			int lineNo = (Integer) parsed[1];
			return new ValgrindStackFrame(message, line, launch, filename, lineNo);
		}
		return new ValgrindError(message, line, launch, pid);
	}
	
	public IValgrindMessage[] getMessages() {
		return messages.toArray(new IValgrindMessage[messages.size()]);
	}
	
	public void printMessages(IValgrindMessage m, int indent) {
		for (int i = 0; i < indent; i++) {
			System.out.print(" "); //$NON-NLS-1$
		}
		System.out.println(m.getText());
		for (IValgrindMessage child : m.getChildren()) {
			printMessages(child, indent + 1);
		}
	}
}
