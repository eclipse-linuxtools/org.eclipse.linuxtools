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

import static org.eclipse.linuxtools.internal.gprof.test.STJunitUtils.BINARY_FILE;
import static org.eclipse.linuxtools.internal.gprof.test.STJunitUtils.OUTPUT_FILE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
@RunWith(Parameterized.class)
public class GprofTest {

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> params = new ArrayList<>();
		boolean addr2line2_16 = false;
		try {
			Process p = Runtime.getRuntime().exec("addr2line --version");
			InputStream is = p.getInputStream();
			LineNumberReader reader = new LineNumberReader(
					new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("addr2line 2.16.")) {
					addr2line2_16 = true;
					break;
				}
			}
		} catch (IOException _) {
		}

		for (File testDir : STJunitUtils.getTestDirs()) {
			final File gmonFile = new File(testDir, OUTPUT_FILE);
			final File binaryFile = new File(testDir, BINARY_FILE);

			File view_cg_RefFile_default = new File(testDir,
					"testCallgraphView.ref");
			File view_cg_RefFile_alternate = new File(testDir,
					"testCallgraphView.ref.binutils-2.16");
			File view_cg2_RefFile_default = new File(testDir,
					"testCallgraphTimeView.ref");
			File view_cg2_RefFile_alternate = new File(testDir,
					"testCallgraphTimeView.ref.binutils-2.16");
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
			final File view_cg2_DumpFile = new File(testDir,
					"testCallgraphTimeView.dump");
			final File view_cg_DumpFile = new File(testDir,
					"testCallgraphView.dump");

			final File view_samplesFile_RefFile = new File(testDir,
					"testSampleView.ref");
			final File view_samplesFile_DumpFile = new File(testDir,
					"testSampleView.dump");
			final File view_samplesFileT_RefFile = new File(testDir,
					"testTimeView.ref");
			final File view_samplesFileT_DumpFile = new File(testDir,
					"testTimeView.dump");

			final File view_samplesFunction_RefFile = new File(testDir,
					"testFunctionSampleView.ref");
			final File view_samplesFunction_DumpFile = new File(testDir,
					"testFunctionSampleView.dump");
			final File view_samplesFunctionT_RefFile = new File(testDir,
					"testFunctionTimeView.ref");
			final File view_samplesFunctionT_DumpFile = new File(testDir,
					"testFunctionTimeView.dump");
			final File view_samplesFlat_RefFile = new File(testDir,
					"testFlatSampleView.ref");
			final File view_samplesFlat_DumpFile = new File(testDir,
					"testFlatSampleView.dump");
			final File view_samplesFlatT_RefFile = new File(testDir,
					"testFlatTimeView.ref");
			final File view_samplesFlatT_DumpFile = new File(testDir,
					"testFlatTimeView.dump");

			IBinaryObject binary = STSymbolManager.sharedInstance
					.getBinaryObject(new Path(binaryFile.getAbsolutePath()));
			final GmonDecoder gd = new GmonDecoder(binary, null);
			try {
				gd.read(gmonFile.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			params.add(new Object[] { gmonFile, gd, view_cg_RefFile,
					view_cg_DumpFile, CallGraphContentProvider.sharedInstance,
					false });
			params.add(new Object[] { gmonFile, gd, view_cg2_RefFile,
					view_cg2_DumpFile, CallGraphContentProvider.sharedInstance,
					true });
			params.add(new Object[] { gmonFile, gd, view_samplesFile_RefFile,
					view_samplesFile_DumpFile,
					FileHistogramContentProvider.sharedInstance, false });
			params.add(new Object[] { gmonFile, gd, view_samplesFileT_RefFile,
					view_samplesFileT_DumpFile,
					FileHistogramContentProvider.sharedInstance, true });
			params.add(new Object[] { gmonFile, gd,
					view_samplesFunction_RefFile,
					view_samplesFunction_DumpFile,
					FunctionHistogramContentProvider.sharedInstance, false });
			params.add(new Object[] { gmonFile, gd,
					view_samplesFunctionT_RefFile,
					view_samplesFunctionT_DumpFile,
					FunctionHistogramContentProvider.sharedInstance, true });
			params.add(new Object[] { gmonFile, gd, view_samplesFlat_RefFile,
					view_samplesFlat_DumpFile,
					FlatHistogramContentProvider.sharedInstance, false });
			params.add(new Object[] { gmonFile, gd, view_samplesFlatT_RefFile,
					view_samplesFlatT_DumpFile,
					FlatHistogramContentProvider.sharedInstance, true });
		}
		return params;
	}

	private File gmonFile;
	private GmonDecoder gd;
	private File refFile;
	private File dumpFile;
	private ITreeContentProvider contentProvider;
	private boolean timeMode;

	public GprofTest(File gmonFile, GmonDecoder gd, File refFile,
			File dumpFile, ITreeContentProvider contentProvider,
			boolean timeMode) {
		this.gmonFile = gmonFile;
		this.gd = gd;
		this.refFile = refFile;
		this.dumpFile = dumpFile;
		this.contentProvider = contentProvider;
		this.timeMode = timeMode;

	}

	private void changeMode(GmonView view, boolean timeModeRequested) {
		AbstractSTTreeViewer gmonViewer = (AbstractSTTreeViewer) view
				.getSTViewer();
		GmonDecoder decoder = (GmonDecoder) gmonViewer.getInput();
		int prof_rate = decoder.getHistogramDecoder().getProfRate();
		if (prof_rate == 0) {
			return;
		}

		TreeColumn tc = gmonViewer.getViewer().getTree().getColumn(1);
		SampleProfField spf = (SampleProfField) tc.getData();

		if (spf.getColumnHeaderText().endsWith("Samples") ^ !timeModeRequested) {
			new SwitchSampleTimeAction(view).run();
		}
	}

	@Test
	public void testView() {
		GmonView view = GmonView.displayGprofView(gd,
				gmonFile.getAbsolutePath());
		SwitchContentProviderAction action = new SwitchContentProviderAction(
				"testAction", "icons/ch_callees.png" /* to avoid error */, view
						.getSTViewer().getViewer(), contentProvider);
		action.run();
		changeMode(view, timeMode);
		STJunitUtils.testCSVExport(view, dumpFile.getAbsolutePath(),
				refFile.getAbsolutePath());
	}
}
