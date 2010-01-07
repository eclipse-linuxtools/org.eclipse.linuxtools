/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.gcov.test;

import java.io.File;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.gcov.Activator;
import org.eclipse.linuxtools.gcov.action.SwitchContentProviderAction;
import org.eclipse.linuxtools.gcov.parser.CovManager;
import org.eclipse.linuxtools.gcov.view.CovFileContentProvider;
import org.eclipse.linuxtools.gcov.view.CovFolderContentProvider;
import org.eclipse.linuxtools.gcov.view.CovFunctionContentProvider;
import org.eclipse.linuxtools.gcov.view.CovView;

public class GcovViewTest extends TestCase {
	
	public GcovViewTest() {
	}
	
	public static Test suite() {
		TestSuite ats = new TestSuite("GCov:View");
		File[] testDirs = STJunitUtils.getTestDirs(Activator.PLUGIN_ID + ".test", "test.*");
		
		for (File testDir : testDirs) {		
			final List<String> covFilesPaths = GcovTestUtils.getGCDAPath(testDir);
			final File binary = GcovTestUtils.getBinary(testDir);
			final File folderRefFile = new File(testDir, "testViewFolder.ref");
			final File folderDumpFile = new File(testDir, "testViewFolder.dump");
			final File fileRefFile = new File(testDir, "testView.ref");
			final File fileDumpFile = new File(testDir, "testView.dump");
			final File functionRefFile = new File(testDir, "testViewFunction.ref");
			final File functionDumpFile = new File(testDir, "testViewFunction.dump");
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-DIRECTORY") {
						public void runTest() throws Throwable {
							testView(
									covFilesPaths, binary,
									CovFolderContentProvider.sharedInstance,
									folderRefFile, folderDumpFile
							);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FILE") {
						public void runTest() throws Throwable {
							testView(
									covFilesPaths, binary,
									CovFileContentProvider.sharedInstance,
									fileRefFile, fileDumpFile
							);
						}
					}
			);
			ats.addTest(
					new TestCase(testDir.getName() + ":CSV-FUNCTION") {
						public void runTest() throws Throwable {
							testView(
									covFilesPaths, binary,
									CovFunctionContentProvider.sharedInstance,
									functionRefFile, functionDumpFile
							);
						}
					}
			);
		}	
		return ats;
	}
	
	
	public static void testView(
			List<String> covFilesPaths, File binaryFile,
			ITreeContentProvider provider,
			File refFile, File dumpFile) throws Exception {
		Locale.setDefault( Locale.US );
		CovManager cvrgeMnger = new CovManager(binaryFile.getAbsolutePath());
		cvrgeMnger.processCovFiles(covFilesPaths);
		// generate model for view
		cvrgeMnger.fillGcovView();
		//load an Eclipse view
		CovView cvrgeView = CovView.displayCovResults(cvrgeMnger);
		
		SwitchContentProviderAction action = new SwitchContentProviderAction(
				"test", "icons/directory_obj.gif",
				cvrgeView.getSTViewer().getViewer(),
				provider
		);
		action.run();
		STJunitUtils.testCSVExport(cvrgeView, dumpFile.getAbsolutePath(), refFile.getAbsolutePath());
		
	}

}