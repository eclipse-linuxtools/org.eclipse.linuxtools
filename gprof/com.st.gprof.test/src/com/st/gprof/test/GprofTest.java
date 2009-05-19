/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package com.st.gprof.test;



import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.TreeColumn;

import com.st.dataviewers.abstractviewers.AbstractSTTreeViewer;
import com.st.flexperf.binutils.utils.STSymbolManager;
import com.st.stgprof.action.SwitchContentProviderAction;
import com.st.stgprof.action.SwitchSampleTimeAction;
import com.st.stgprof.parser.GmonDecoder;
import com.st.stgprof.utils.STGprofProgramChecker;
import com.st.stgprof.view.CallGraphContentProvider;
import com.st.stgprof.view.FileHistogramContentProvider;
import com.st.stgprof.view.FlatHistogramContentProvider;
import com.st.stgprof.view.FunctionHistogramContentProvider;
import com.st.stgprof.view.GmonView;
import com.st.stgprof.view.fields.SampleProfField;

/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class GprofTest extends TestCase {
	private static final String GMON_BINARY_FILE = "a.out"; 
	private static final String GMON_OUTPUT_FILE = "gmon.out"; 
	private static final String GMON_DIRECTORY_SUFFIX = "_gprof_input"; 

	public GprofTest() {
	}

	public static Test suite() {
		TestSuite ats = new TestSuite("GProf:View CSV Export");

		File[] testDirs = STJunitUtils.getTestDirs("com.st.gprof.test", ".*" + GMON_DIRECTORY_SUFFIX);
		for (File testDir : testDirs) {
			final File gmonFile = new File(testDir, GMON_OUTPUT_FILE);
			final File binaryFile = new File(testDir, GMON_BINARY_FILE);
			final File view_cg_RefFile = new File(testDir, "testCallgraphView.ref");
			final File view_cg_DumpFile = new File(testDir, "testCallgraphView.dump");
			final File view_cg2_RefFile = new File(testDir, "testCallgraphTimeView.ref");
			final File view_cg2_DumpFile = new File(testDir, "testCallgraphTimeView.dump");

			final File view_samplesFile_RefFile = new File(testDir, "testSampleView.ref");
			final File view_samplesFile_DumpFile = new File(testDir, "testSampleView.dump");
			final File view_samplesFileT_RefFile = new File(testDir, "testTimeView.ref");
			final File view_samplesFileT_DumpFile = new File(testDir, "testTimeView.dump");

			final File view_samplesFunction_RefFile = new File(testDir, "testFunctionSampleView.ref");
			final File view_samplesFunction_DumpFile = new File(testDir, "testFunctionSampleView.dump");
			final File view_samplesFunctionT_RefFile = new File(testDir, "testFunctionTimeView.ref");
			final File view_samplesFunctionT_DumpFile = new File(testDir, "testFunctionTimeView.dump");
			final File view_samplesFlat_RefFile = new File(testDir, "testFlatSampleView.ref");
			final File view_samplesFlat_DumpFile = new File(testDir, "testFlatSampleView.dump");
			final File view_samplesFlatT_RefFile = new File(testDir, "testFlatTimeView.ref");
			final File view_samplesFlatT_DumpFile = new File(testDir, "testFlatTimeView.dump");

			IBinaryObject binary = STSymbolManager.sharedInstance.getBinaryObject(new Path(binaryFile.getAbsolutePath()));
			final GmonDecoder gd = new GmonDecoder(binary);
			try {
				gd.read(gmonFile.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-CALLGRAPH") {
						public void runTest() throws Throwable {
							testView(gmonFile, gd, view_cg_RefFile, view_cg_DumpFile, CallGraphContentProvider.sharedInstance, false);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-CALLGRAPH-TIMED") {
						public void runTest() throws Throwable {
							testView(gmonFile, gd, view_cg2_RefFile, view_cg2_DumpFile, CallGraphContentProvider.sharedInstance, true);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FILE") {
						public void runTest() throws Throwable {
							testView(gmonFile, gd, view_samplesFile_RefFile, view_samplesFile_DumpFile, FileHistogramContentProvider.sharedInstance, false);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FILE-TIMED") {
						public void runTest() throws Throwable {
							testView(gmonFile, gd, view_samplesFileT_RefFile, view_samplesFileT_DumpFile, FileHistogramContentProvider.sharedInstance, true);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FUNCTION") {
						public void runTest() throws Throwable {
							testView(gmonFile, gd, view_samplesFunction_RefFile, view_samplesFunction_DumpFile, FunctionHistogramContentProvider.sharedInstance, false);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FUNCTION-TIMED") {
						public void runTest() throws Throwable {
							testView(gmonFile, gd, view_samplesFunctionT_RefFile, view_samplesFunctionT_DumpFile, FunctionHistogramContentProvider.sharedInstance, true);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FLAT") {
						public void runTest() throws Throwable {
							testView(gmonFile, gd, view_samplesFlat_RefFile, view_samplesFlat_DumpFile, FlatHistogramContentProvider.sharedInstance, false);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FLAT-TIMED") {
						public void runTest() throws Throwable {
							testView(gmonFile, gd, view_samplesFlatT_RefFile, view_samplesFlatT_DumpFile, FlatHistogramContentProvider.sharedInstance, true);
						}
					}
			);
		}	
		return ats;
	}


	public static void changeMode(GmonView view, boolean timeModeRequested) {
		AbstractSTTreeViewer gmonViewer = (AbstractSTTreeViewer)view.getSTViewer();
		GmonDecoder decoder = (GmonDecoder) gmonViewer.getInput();
		int prof_rate = decoder.getHistogramDecoder().getProf_rate();
		if (prof_rate == 0) {
			return;
		}

		TreeColumn tc = gmonViewer.getViewer().getTree().getColumn(1);
		SampleProfField spf = (SampleProfField) tc.getData();

		if (spf.getColumnHeaderText().endsWith("Samples") ^ !timeModeRequested) {
			new SwitchSampleTimeAction(view).run();
		}
	}

	public static void testView(File gmonFile, GmonDecoder gd,
			File refFile, File dumpFile,
			ITreeContentProvider contentProvider, boolean timeMode) throws Exception {
		// execute stgprof until generate view
		GmonView view = GmonView.displayGprofView(gd, gmonFile.getAbsolutePath(), null);
		SwitchContentProviderAction action = new SwitchContentProviderAction("testAction", "icons/ch_callees.png" /*to avoid error*/, view.getSTViewer().getViewer(), contentProvider);
		action.run();
		changeMode(view, timeMode);
		STJunitUtils.testCSVExport(view, dumpFile.getAbsolutePath(), refFile.getAbsolutePath());
	}
}
