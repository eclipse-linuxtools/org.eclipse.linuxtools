/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.perf.ReportComparisonData;
import org.eclipse.linuxtools.internal.perf.SourceDisassemblyData;
import org.eclipse.linuxtools.internal.perf.StatData;
import org.eclipse.linuxtools.internal.perf.handlers.PerfStatDataOpenHandler;
import org.junit.Test;

public class DataManipulatorTest {

    private static final String output = "output"; //$NON-NLS-1$

    @Test
    public void testEchoSourceDisassemblyData() {
        final IPath path = new Path("/a/b/c/"); //$NON-NLS-1$

        StubSourceDisassemblyData sdData = new StubSourceDisassemblyData(
                "disassembly data", path); //$NON-NLS-1$
        sdData.parse();

        String expected = "perf annotate -i " + path.toOSString() + "perf.data"; //$NON-NLS-1$

        assertEquals(expected, sdData.getPerfData().trim());
    }
    @Test
    public void testEchoStatData() {
        final String binary = "a/b/c.out";
        final String[] args = new String[] { "arg1", "arg2", "arg3" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final int runCount = 3;

        StubStatData sData = new StubStatData(
                "stat data", binary, args, runCount, null); //$NON-NLS-1$
        sData.parse();

        String expected = "perf stat -r " + runCount + " -o " + output + " " + binary; //$NON-NLS-1$ //$NON-NLS-2$
        for (String i:args) {
            expected += " " + i; //$NON-NLS-1$
        }

        assertEquals(expected, sData.getPerfData().trim());
    }
    @Test
    public void testEchoStatDataEvents() {
        final String binary = "a/b/c.out";
        final String[] args = new String[] { "arg1", "arg2", "arg3" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final String[] events = new String[] { "event1", "event2", "event3" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final int runCount = 3;

        StubStatData sData = new StubStatData(
                "stat data", binary, args, runCount, events); //$NON-NLS-1$
        sData.parse();

        String expected = "perf stat -r " + runCount; //$NON-NLS-1$
        for(String event : events){
            expected += " -e " + event; //$NON-NLS-1$
        }

        expected = expected + " -o " + output + " " + binary; //$NON-NLS-1$
        for (String i : args) {
            expected += " " + i; //$NON-NLS-1$
        }

        assertEquals(expected, sData.getPerfData().trim());
    }

    @Test
    public void testEchoReportDiffData() {
        IPath oldData = Path.fromOSString("perf.old.data"); //$NON-NLS-1$
        IPath newData = Path.fromOSString("perf.data"); //$NON-NLS-1$
        StubReportDiffData diffData = new StubReportDiffData("title", //$NON-NLS-1$
                oldData, newData);
        diffData.parse();

        String expected = "perf diff " + oldData.toOSString()  //$NON-NLS-1$
                + " " + newData.toOSString();  //$NON-NLS-1$

        assertEquals(expected, diffData.getPerfData().trim());
    }

    @Test
    public void testPerfStatDataOpenHandler() throws IOException {
        String resourceDirPath = "/resources/stat-data/perf_simple.stat"; //$NON-NLS-1$
        String path = FileLocator.toFileURL(
                this.getClass().getResource(resourceDirPath)).getPath();
        PerfStatDataOpenHandler handler = new PerfStatDataOpenHandler();
        handler.open(new Path(path));
    }

    /**
     * Used for testing SourceDisassemblyData
     */
    private static class StubSourceDisassemblyData extends SourceDisassemblyData {

        public StubSourceDisassemblyData(String title, IPath workingDir) {
            super(title, workingDir);
        }

        @Override
        public String[] getCommand(String workingDir) {
            List<String> ret = new ArrayList<>();
            // return the same command with 'echo' prepended
            ret.add("echo"); //$NON-NLS-1$
            ret.addAll(Arrays.asList(super.getCommand(workingDir)));
            return ret.toArray(new String[ret.size()]);
        }
    }

    /**
     * Used for testing StatData
     */
    private static class StubStatData extends StatData {

        public StubStatData(String title, String cmd, String[] args,
                int runCount, String[] events) {
            super(title, Path.fromOSString(""), cmd, args, runCount, events);
        }

        @Override
        public String[] getCommand(String command, String[] args, String file) {
            // return the same command with 'echo' prepended
            List<String> ret = new ArrayList<>();
            ret.add("echo"); //$NON-NLS-1$
            ret.addAll(Arrays.asList(super.getCommand(command, args, file)));
            return ret.toArray(new String[ret.size()]);
        }

        @Override
        public void parse() {
            String[] cmd = getCommand(getProgram(), getArguments(), output); //$NON-NLS-1$
            // echo will print to standard out
            performCommand(cmd, 1);
        }
    }

    /**
     * Used for testing ReportComparisonData
     */
    private static class StubReportDiffData extends ReportComparisonData{

        public StubReportDiffData(String title, IPath oldFile, IPath newFile) {
            super(title, oldFile, newFile, null);
        }

        @Override
        protected String[] getCommand() {
            // return the same command with 'echo' prepended
            List<String> ret = new ArrayList<>();
            ret.add("echo"); //$NON-NLS-1$
            ret.addAll(Arrays.asList(super.getCommand()));
            return ret.toArray(new String[ret.size()]);
        }

    }

}
