/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.massif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.linuxtools.valgrind.core.AbstractValgrindTextParser;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot.SnapshotType;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot.TimeUnit;
import org.eclipse.osgi.util.NLS;

public class MassifParser extends AbstractValgrindTextParser {
	private static final String CMD = "cmd"; //$NON-NLS-1$
	private static final String TIME_UNIT = "time_unit"; //$NON-NLS-1$
	private static final String SNAPSHOT = "snapshot"; //$NON-NLS-1$
	private static final String TIME = "time"; //$NON-NLS-1$
	private static final String MEM_HEAP_B = "mem_heap_B"; //$NON-NLS-1$
	private static final String MEM_HEAP_EXTRA_B = "mem_heap_extra_B"; //$NON-NLS-1$
	private static final String MEM_STACKS_B = "mem_stacks_B"; //$NON-NLS-1$
	private static final String HEAP_TREE = "heap_tree"; //$NON-NLS-1$
	
	private static final String INSTRUCTIONS = "i"; //$NON-NLS-1$
	private static final String MILLISECONDS = "ms"; //$NON-NLS-1$
	private static final String BYTES = "B"; //$NON-NLS-1$
	private static final String PEAK = "peak"; //$NON-NLS-1$
	private static final String DETAILED = "detailed"; //$NON-NLS-1$
	private static final String EMPTY = "empty"; //$NON-NLS-1$

	private static final String COLON = ":"; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String EQUALS = "="; //$NON-NLS-1$
	private static final String DOT = "."; //$NON-NLS-1$

	protected Integer pid;
	protected MassifSnapshot[] snapshots;

	public MassifParser(File inputFile) throws IOException {
		ArrayList<MassifSnapshot> list = new ArrayList<MassifSnapshot>();
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line;
		MassifSnapshot snapshot = null;
		String cmd = null;
		TimeUnit unit = null;  
		int n = 0;
		
		// retrive PID from filename
		String filename = inputFile.getName();
		String pidstr = filename.substring(MassifLaunchDelegate.OUT_PREFIX.length(), filename.indexOf(DOT));
		if (isNumber(pidstr)) {
			pid = new Integer(pidstr);
		}
		else {
			throw new IOException(Messages.getString("MassifParser.Cannot_parse_PID")); //$NON-NLS-1$
		}
		
		// parse contents of file
		while ((line = br.readLine()) != null) {
			if (line.startsWith(CMD + COLON)){
				cmd = parseStrValue(line, COLON + SPACE);
			}
			else if (line.startsWith(TIME_UNIT + COLON)) {
				unit = parseTimeUnit(line);
			}			
			else if (line.startsWith(SNAPSHOT)) {
				if (snapshot != null) {
					// this snapshot finished parsing
					list.add(snapshot);
					n++;
				}
				snapshot = new MassifSnapshot(n);
				snapshot.setCmd(cmd);
				snapshot.setUnit(unit);
			}
			else if (line.startsWith(TIME + EQUALS)) {
				snapshot.setTime(parseLongValue(line, EQUALS));
			}
			else if (line.startsWith(MEM_HEAP_B + EQUALS)) {
				snapshot.setHeapBytes(parseLongValue(line, EQUALS));
			}
			else if (line.startsWith(MEM_HEAP_EXTRA_B + EQUALS)) {
				snapshot.setHeapExtra(parseLongValue(line, EQUALS));
			}
			else if (line.startsWith(MEM_STACKS_B + EQUALS)) {
				snapshot.setStacks(parseLongValue(line, EQUALS));
			}
			else if (line.startsWith(HEAP_TREE + EQUALS)) {
				SnapshotType type = parseSnapshotType(line);
				snapshot.setType(type);
				switch (type) {
				case DETAILED:
				case PEAK:
					MassifHeapTreeNode node = parseTree(snapshot, null, br);
					node.setText(NLS.bind(Messages.getString("MassifParser.Snapshot_n"), n, node.getText())); // prepend snapshot number //$NON-NLS-1$
					snapshot.setRoot(node);
				}
			}
		}
		if (snapshot != null) {
			// last snapshot that finished parsing
			list.add(snapshot);
		}
		snapshots = list.toArray(new MassifSnapshot[list.size()]);
	}

	private MassifHeapTreeNode parseTree(MassifSnapshot snapshot, MassifHeapTreeNode parent, BufferedReader br) throws IOException {
		String line = br.readLine();
		if (line == null) {
			throw new IOException(Messages.getString("MassifParser.Unexpected_EOF")); //$NON-NLS-1$
		}
		line = line.trim(); // remove leading whitespace
		String[] parts = line.split(" "); //$NON-NLS-1$
		Integer numChildren = parseNumChildren(parts[0]);
		if (numChildren == null) {
			fail(line);
		}

		StringBuffer nodeText = new StringBuffer();
		Long numBytes = parseNumBytes(parts[1]);
		if (numBytes == null) {
			fail(line);
		}
		
		double percentage;
		if (numBytes.intValue() == 0) {
			percentage = 0;
		}
		else {
			percentage = numBytes.doubleValue() / snapshot.getTotal() * 100;
		}
		nodeText.append(Double.valueOf(new DecimalFormat("0.##").format(percentage)) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		nodeText.append(" ("); //$NON-NLS-1$
		nodeText.append(new DecimalFormat("#,##0").format(numBytes.longValue()) + "B"); //$NON-NLS-1$ //$NON-NLS-2$
		nodeText.append(")"); //$NON-NLS-1$
		
		// append the rest
		for (int i = 2; i < parts.length; i++) {
			nodeText.append(" "); //$NON-NLS-1$
			nodeText.append(parts[i]);
		}
		
		MassifHeapTreeNode node = new MassifHeapTreeNode(parent, nodeText.toString());
		
		// Parse source file if specified
		parseSourceFile(node, line);
		for (int i = 0; i < numChildren.intValue(); i++) {
			node.addChild(parseTree(snapshot, node, br));
		}
		return node;
	}

	/*
	 * Assumes syntax is: "\(.*:[0-9]+\)$"
	 */
	private void parseSourceFile(MassifHeapTreeNode node, String line) {
		int ix = line.indexOf("("); //$NON-NLS-1$
		if (ix >= 0) {
			String part = line.substring(ix, line.length());
			part = part.substring(1, part.length() - 1); // remove leading and trailing parentheses
			if ((ix = part.lastIndexOf(":")) >= 0 && ix < part.length()) { //$NON-NLS-1$		
				String strLineNo = part.substring(ix + 1);
				if (isNumber(strLineNo)) {
					int lineNo = Integer.parseInt(strLineNo);
					String filename = part.substring(0, ix);
					node.setFilename(filename);
					node.setLine(lineNo);
				}
			}
		}
	}
	
	private Long parseNumBytes(String string) {
		Long result = null;
		if (isNumber(string)) {
			result = Long.parseLong(string);
		}
		return result;
	}

	/*
	 * format is "n[0-9]+:"
	 */
	private Integer parseNumChildren(String string) {
		Integer result = null;
		if (string.length() >= 3) {
			String number = string.substring(1, string.length() - 1);
			if (isNumber(number)) {
				result = Integer.parseInt(number);
			}
		}
		return result;
	}

	public Integer getPid() {
		return pid;
	}
	
	public MassifSnapshot[] getSnapshots() {
		return snapshots;
	}

	protected SnapshotType parseSnapshotType(String line) throws IOException {
		SnapshotType result = null;
		String[] parts = line.split(EQUALS);
		if (parts.length > 1) {
			String type = parts[1];
			if (type.equals(EMPTY)) {
				result = SnapshotType.EMPTY;
			}
			else if (type.equals(DETAILED)) {
				result = SnapshotType.DETAILED;
			}
			else if (type.equals(PEAK)) {
				result = SnapshotType.PEAK;
			}
		}
		if (result == null) {
			fail(line);
		}
		return result;
	}
	
	protected TimeUnit parseTimeUnit(String line) throws IOException {
		TimeUnit result = null;
		String[] parts = line.split(COLON + SPACE);
		if (parts.length > 1) {
			String type = parts[1];
			if (type.equals(INSTRUCTIONS)) {
				result = TimeUnit.INSTRUCTIONS;
			}
			else if (type.equals(MILLISECONDS)) {
				result = TimeUnit.MILLISECONDS;
			}
			else if (type.equals(BYTES)) {
				result = TimeUnit.BYTES;
			}
		}
		if (result == null) {
			fail(line);
		}
		return result;
	}
}
