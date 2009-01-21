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
package org.eclipse.linuxtools.valgrind.cachegrind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindDescription;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindLine;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.core.AbstractValgrindTextParser;

public class CachegrindParser extends AbstractValgrindTextParser {
	private static final String CMD = "cmd"; //$NON-NLS-1$
	private static final String DESC = "desc"; //$NON-NLS-1$
	private static final String FL = "fl"; //$NON-NLS-1$
	private static final String FN = "fn"; //$NON-NLS-1$
	private static final String EVENTS = "events"; //$NON-NLS-1$
	private static final String SUMMARY = "summary"; //$NON-NLS-1$
	
	private static final String EQUALS = "="; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String COLON = ":"; //$NON-NLS-1$
	private static final String COMMA = ","; //$NON-NLS-1$
	
	protected static CachegrindParser instance;
	
	protected CachegrindParser() {
	}
	
	public static CachegrindParser getParser() {
		if (instance == null) {
			instance = new CachegrindParser();
		}
		return instance;
	}

	public void parse(CachegrindOutput output, File cgOut) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(cgOut));
		output.setPid(parsePID(cgOut.getName(), CachegrindLaunchDelegate.OUT_PREFIX));
		
		String line;
		CachegrindFile curFl = null;
		CachegrindFunction curFn = null;
		while ((line = br.readLine()) != null) {		
			if (line.startsWith(EVENTS + COLON)) {
				String[] tokens = line.split(SPACE);
				output.setEvents(Arrays.copyOfRange(tokens, 1, tokens.length));
			}
			else if (line.startsWith(CMD + COLON)) {
				output.setCommand(parseStrValue(line, COLON + SPACE));
			}
			else if (line.startsWith(DESC + COLON)) {
				CachegrindDescription description = parseDescription(line);
				output.addDescription(description);
			}
			else if (line.startsWith(FL + EQUALS)) {
				curFl = new CachegrindFile(output, parseStrValue(line, EQUALS));
				output.addFile(curFl);
			}
			else if (line.startsWith(FN + EQUALS)) {				
				if (curFl != null) {
					curFn = new CachegrindFunction(curFl, parseStrValue(line, EQUALS));
					curFl.addFunction(curFn);
				}
				else {
					fail(line);
				}
			}
			else if (line.startsWith(SUMMARY + COLON)) {
				String[] tokens = line.split(SPACE);
				long[] summary = parseData(line, Arrays.copyOfRange(tokens, 1, tokens.length));
				output.setSummary(summary);
			}
			else { // line data
				String[] tokens = line.split(SPACE);
				if (isNumber(tokens[0])) {
					int lineNo = Integer.parseInt(tokens[0]);
					long[] data = parseData(line, Arrays.copyOfRange(tokens, 1, tokens.length));
					if (curFn != null) {
						curFn.addLine(new CachegrindLine(curFn, lineNo, data));
					}
					else {
						fail(line);
					}
				}
				else {
					fail(line);
				}
			}
		}
	}
	
	private long[] parseData(String line, String[] data) throws IOException {
		long[] result = new long[data.length];
		for (int i = 0; i < data.length; i++) {
			if (!isNumber(data[i])) {
				fail(line);
			}
			result[i] = Long.parseLong(data[i]);
		}
		return result;
	}

	private CachegrindDescription parseDescription(String line) throws IOException {
		CachegrindDescription desc = null;
		String[] tokens = line.split(COLON + "\\s+"); //$NON-NLS-1$
		if (tokens.length == 3) {
			String name = tokens[1];
			tokens = tokens[2].split(COMMA + SPACE);
			if (tokens.length == 3) {
				desc = new CachegrindDescription(name, tokens[0], tokens[1], tokens[2]);
			}
			else {
				fail(line);
			}
		}
		else {
			fail(line);
		}
		return desc;
	}
}