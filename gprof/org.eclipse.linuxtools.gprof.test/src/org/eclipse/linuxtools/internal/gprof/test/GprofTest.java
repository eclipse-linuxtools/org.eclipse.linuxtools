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
package org.eclipse.linuxtools.internal.gprof.test;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.internal.gprof.action.SwitchContentProviderAction;
import org.eclipse.linuxtools.internal.gprof.action.SwitchSampleTimeAction;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.eclipse.linuxtools.internal.gprof.view.CallGraphContentProvider;
import org.eclipse.linuxtools.internal.gprof.view.FileHistogramContentProvider;
import org.eclipse.linuxtools.internal.gprof.view.FlatHistogramContentProvider;
import org.eclipse.linuxtools.internal.gprof.view.FunctionHistogramContentProvider;
import org.eclipse.linuxtools.internal.gprof.view.GmonView;
import org.eclipse.linuxtools.internal.gprof.view.fields.SampleProfField;
import org.eclipse.swt.widgets.TreeColumn;


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
		boolean addr2line2_16 = false;
		try {
			Process p = Runtime.getRuntime().exec("addr2line --version");
			InputStream is = p.getInputStream();
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("addr2line 2.16.")) {
					addr2line2_16 = true;
					break;
				}
			}
		} catch (Exception _) {
		}
		
		File[] testDirs = STJunitUtils.getTestDirs("org.eclipse.linuxtools.gprof.test",
				".*" + GMON_DIRECTORY_SUFFIX);
		for (File testDir : testDirs) {
			final File gmonFile = new File(testDir, GMON_OUTPUT_FILE);
			final File binaryFile = new File(testDir, GMON_BINARY_FILE);

			File view_cg_RefFile_default = new File(testDir, "testCallgraphView.ref");
			File view_cg_RefFile_alternate = new File(testDir, "testCallgraphView.ref.binutils-2.16");
			File view_cg2_RefFile_default = new File(testDir, "testCallgraphTimeView.ref");
			File view_cg2_RefFile_alternate = new File(testDir, "testCallgraphTimeView.ref.binutils-2.16");
			final File view_cg_RefFile;
			final File view_cg2_RefFile;
			if (addr2line2_16 && view_cg_RefFile_alternate.exists()) {
				view_cg_RefFile = view_cg_RefFile_alternate;
			} else {
				view_cg_RefFile = view_cg_RefFile_default;
			}
			if (addr2line2_16 && view_cg2_RefFile_alternate.exists()) {
				view_cg2_RefFile = view_cg2_RefFile_alternate;
			} else {
				view_cg2_RefFile = view_cg2_RefFile_default;
			}
			final File view_cg2_DumpFile = new File(testDir, "testCallgraphTimeView.dump");
			final File view_cg_DumpFile = new File(testDir, "testCallgraphView.dump");

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

			IBinaryObject binary = STSymbolManager.sharedInstance.getBinaryObject(
					new Path(binaryFile.getAbsolutePath()));
			final GmonDecoder gd = new GmonDecoder(binary, null);
			try {
				gd.read(gmonFile.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}

			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-CALLGRAPH") {
						@Override
						public void runTest() {
							testView(gmonFile, gd, view_cg_RefFile, view_cg_DumpFile,
									CallGraphContentProvider.sharedInstance, false);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-CALLGRAPH-TIMED") {
						@Override
						public void runTest() {
							testView(gmonFile, gd, view_cg2_RefFile, view_cg2_DumpFile,
									CallGraphContentProvider.sharedInstance, true);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FILE") {
						@Override
						public void runTest() {
							testView(gmonFile, gd, view_samplesFile_RefFile,
									view_samplesFile_DumpFile,
									FileHistogramContentProvider.sharedInstance, false);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FILE-TIMED") {
						@Override
						public void runTest() {
							testView(gmonFile, gd, view_samplesFileT_RefFile,
									view_samplesFileT_DumpFile,
									FileHistogramContentProvider.sharedInstance, true);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FUNCTION") {
						@Override
						public void runTest() {
							testView(gmonFile, gd, view_samplesFunction_RefFile,
									view_samplesFunction_DumpFile,
									FunctionHistogramContentProvider.sharedInstance, false);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FUNCTION-TIMED") {
						@Override
						public void runTest() {
							testView(gmonFile, gd, view_samplesFunctionT_RefFile,
									view_samplesFunctionT_DumpFile,
									FunctionHistogramContentProvider.sharedInstance, true);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FLAT") {
						@Override
						public void runTest() {
							testView(gmonFile, gd, view_samplesFlat_RefFile,
									view_samplesFlat_DumpFile,
									FlatHistogramContentProvider.sharedInstance, false);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FLAT-TIMED") {
						@Override
						public void runTest() {
							testView(gmonFile, gd, view_samplesFlatT_RefFile,
									view_samplesFlatT_DumpFile,
									FlatHistogramContentProvider.sharedInstance, true);
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
			ITreeContentProvider contentProvider, boolean timeMode) {
		GmonView view = GmonView.displayGprofView(gd, gmonFile.getAbsolutePath());
		SwitchContentProviderAction action = new SwitchContentProviderAction("testAction",
				"icons/ch_callees.png" /*to avoid error*/, view.getSTViewer().getViewer(),
				contentProvider);
		action.run();
		changeMode(view, timeMode);
		STJunitUtils.testCSVExport(view, dumpFile.getAbsolutePath(), refFile.getAbsolutePath());
	}
}
