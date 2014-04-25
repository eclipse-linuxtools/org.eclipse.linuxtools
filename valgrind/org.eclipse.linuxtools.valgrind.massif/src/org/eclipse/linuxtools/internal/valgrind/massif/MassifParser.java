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
package org.eclipse.linuxtools.internal.valgrind.massif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot.SnapshotType;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot.TimeUnit;
import org.eclipse.linuxtools.valgrind.core.ValgrindParserUtils;
import org.eclipse.osgi.util.NLS;

public class MassifParser {
    private static final String COLON = ":"; //$NON-NLS-1$
    private static final String SPACE = " "; //$NON-NLS-1$
    private static final String EQUALS = "="; //$NON-NLS-1$

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

    private Integer pid;
    private MassifSnapshot[] snapshots;

    public MassifParser(File inputFile) throws IOException {
        ArrayList<MassifSnapshot> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))){
            String line;
            MassifSnapshot snapshot = null;
            String cmd = null;
            TimeUnit unit = null;
            int n = 0;

            // retrive PID from filename
            String filename = inputFile.getName();
            pid = ValgrindParserUtils.parsePID(filename, MassifLaunchDelegate.OUT_PREFIX);

            // parse contents of file
            while ((line = br.readLine()) != null) {
                if (line.startsWith(CMD + COLON)){
                    cmd = ValgrindParserUtils.parseStrValue(line, COLON + SPACE);
                } else if (line.startsWith(TIME_UNIT + COLON)) {
                    unit = parseTimeUnit(line);
                } else if (line.startsWith(SNAPSHOT)) {
                    if (snapshot != null) {
                        // this snapshot finished parsing
                        list.add(snapshot);
                        n++;
                    }
                    snapshot = new MassifSnapshot(n);
                    snapshot.setCmd(cmd);
                    snapshot.setUnit(unit);
                } else if (line.startsWith(TIME + EQUALS)) {
                    snapshot.setTime(ValgrindParserUtils.parseLongValue(line, EQUALS));
                } else if (line.startsWith(MEM_HEAP_B + EQUALS)) {
                    snapshot.setHeapBytes(ValgrindParserUtils.parseLongValue(line, EQUALS));
                } else if (line.startsWith(MEM_HEAP_EXTRA_B + EQUALS)) {
                    snapshot.setHeapExtra(ValgrindParserUtils.parseLongValue(line, EQUALS));
                } else if (line.startsWith(MEM_STACKS_B + EQUALS)) {
                    snapshot.setStacks(ValgrindParserUtils.parseLongValue(line, EQUALS));
                } else if (line.startsWith(HEAP_TREE + EQUALS)) {
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
    }

    private MassifHeapTreeNode parseTree(MassifSnapshot snapshot, MassifHeapTreeNode parent, BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null) {
            throw new IOException(Messages.getString("MassifParser.Unexpected_EOF")); //$NON-NLS-1$
        }
        line = line.trim(); // remove leading whitespace
        String[] parts = line.split(" "); //$NON-NLS-1$
        // bounds checking so we can fail with a more informative error
        if (parts.length < 2) {
            ValgrindParserUtils.fail(line);
        }

        Integer numChildren = parseNumChildren(parts[0]);
        if (numChildren == null) {
            ValgrindParserUtils.fail(line);
        }

        Long numBytes = parseNumBytes(parts[1]);
        if (numBytes == null) {
            ValgrindParserUtils.fail(line);
        }

        double percentage;
        if (numBytes.intValue() == 0) {
            percentage = 0;
        } else {
            percentage = numBytes.doubleValue() / snapshot.getTotal() * 100;
        }

        MassifHeapTreeNode node;
        String address = null;
        String function = null;
        String filename = null;
        int lineNo = 0;
        if (parts[2].startsWith("0x")) { //$NON-NLS-1$
            // we extend the above bounds checking
            if (parts.length < 3) {
                ValgrindParserUtils.fail(line);
            }
            // remove colon from address
            address = parts[2].substring(0, parts[2].length() - 1);

            function = parseFunction(parts[3], line);

            // Parse source file if specified
            Object[] subparts = ValgrindParserUtils.parseFilename(line);
            filename = (String) subparts[0];
            lineNo = (Integer) subparts[1];

            node = new MassifHeapTreeNode(parent, percentage, numBytes, address, function, filename, lineNo);
        } else {
            // concatenate the rest
            StringBuffer text = new StringBuffer();
            for (int i = 2; i < parts.length; i++) {
                text.append(parts[i]);
                text.append(" "); //$NON-NLS-1$
            }

            node = new MassifHeapTreeNode(parent, percentage, numBytes, text.toString().trim());
        }


        for (int i = 0; i < numChildren.intValue(); i++) {
            node.addChild(parseTree(snapshot, node, br));
        }
        return node;
    }

    private String parseFunction(String start, String line) throws IOException {
        String function = null;
        int ix = line.lastIndexOf('(');
        if (ix >= 0) {
            function = line.substring(line.indexOf(start), ix);
        }
        else {
            function = line.substring(line.indexOf(start));
        }
        if (function != null) {
            function = function.trim();
        }
        else {
            ValgrindParserUtils.fail(line);
        }

        return function;
    }

    private Long parseNumBytes(String string) {
        Long result = null;
        if (ValgrindParserUtils.isNumber(string)) {
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
            if (ValgrindParserUtils.isNumber(number)) {
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

    private SnapshotType parseSnapshotType(String line) throws IOException {
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
            ValgrindParserUtils.fail(line);
        }
        return result;
    }

    private TimeUnit parseTimeUnit(String line) throws IOException {
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
            ValgrindParserUtils.fail(line);
        }
        return result;
    }
}
