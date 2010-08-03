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
package org.eclipse.linuxtools.internal.valgrind.cachegrind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindDescription;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindLine;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.core.ValgrindParserUtils;

public class CachegrindParser {
	private static final String COLON = ":"; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String EQUALS = "="; //$NON-NLS-1$
	
	private static final String CMD = "cmd"; //$NON-NLS-1$
	private static final String DESC = "desc"; //$NON-NLS-1$
	private static final String FL = "fl"; //$NON-NLS-1$
	private static final String FN = "fn"; //$NON-NLS-1$
	private static final String EVENTS = "events"; //$NON-NLS-1$
	private static final String SUMMARY = "summary"; //$NON-NLS-1$

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
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(cgOut));
			output.setPid(ValgrindParserUtils.parsePID(cgOut.getName(), CachegrindLaunchDelegate.OUT_PREFIX));

			String line;
			CachegrindFile curFl = null;
			CachegrindFunction curFn = null;
			while ((line = br.readLine()) != null) {		
				if (line.startsWith(EVENTS + COLON)) {
					output.setEvents(ValgrindParserUtils.parseStrValue(line, COLON + SPACE).split(SPACE));
				}
				else if (line.startsWith(CMD + COLON)) {
					output.setCommand(ValgrindParserUtils.parseStrValue(line, COLON + SPACE));
				}
				else if (line.startsWith(DESC + COLON)) {
					CachegrindDescription description = parseDescription(line);
					output.addDescription(description);
				}
				else if (line.startsWith(FL + EQUALS)) {
					curFl = new CachegrindFile(output, ValgrindParserUtils.parseStrValue(line, EQUALS));
					output.addFile(curFl);
				}
				else if (line.startsWith(FN + EQUALS)) {				
					if (curFl != null) {
						curFn = new CachegrindFunction(curFl, ValgrindParserUtils.parseStrValue(line, EQUALS));
						curFl.addFunction(curFn);
					}
					else {
						ValgrindParserUtils.fail(line);
					}
				}
				else if (line.startsWith(SUMMARY + COLON)) {
					long[] summary = parseData(line, ValgrindParserUtils.parseStrValue(line, COLON + SPACE).split(SPACE));
					output.setSummary(summary);
				}
				else { // line data
					String[] tokens = line.split(SPACE, 2);
					if (ValgrindParserUtils.isNumber(tokens[0])) {
						int lineNo = Integer.parseInt(tokens[0]);

						long[] data = parseData(line, tokens[1].split(SPACE));
						if (curFn != null) {
							curFn.addLine(new CachegrindLine(curFn, lineNo, data));
						}
						else {
							ValgrindParserUtils.fail(line);
						}
					}
					else {
						ValgrindParserUtils.fail(line);
					}
				}
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	private long[] parseData(String line, String[] data) throws IOException {
		long[] result = new long[data.length];
		for (int i = 0; i < data.length; i++) {
			if (!ValgrindParserUtils.isNumber(data[i])) {
				ValgrindParserUtils.fail(line);
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
				ValgrindParserUtils.fail(line);
			}
		}
		else {
			ValgrindParserUtils.fail(line);
		}
		return desc;
	}
}