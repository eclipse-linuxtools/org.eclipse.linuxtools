/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileCompletionProcessor;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.jupiter.api.Test;

public class SpecfileCompletionProcessorTest extends FileTestCase {

	public static final String ONE_SOURCE = "Source0: text.zip\n";
	public static final String NO_SOURCE = "Patch3: somefilesomewhere.patch" + "\n" + "Patch2: someotherfile.patch\n";
	public static final String THREE_SOURCE_SEPARATED = "Source0: text.zip" + "\n" + "Patch0: first.patch" + "\n"
			+ "Source2: ant.jar" + "\n" + "Source3: main.tar.gz";

	private static final String BUILD_REQUIRES = "BuildRequires: p";

	private static final String NON_ALPHA_DOT = "Requires: java-1.";
	private static final String NON_ALPHA_PLUS = "Requires: libstdc+";

	private SpecfileEditor initEditor(String contents) throws Exception {
		newFile(contents);
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				testFile);

		return (SpecfileEditor) openEditor;
	}

	private synchronized void computeCompletionProposals(String specContent, int occurances) throws Exception {
		SpecfileEditor editor = initEditor(specContent);
		testProject.refresh();
		// This is needed so the changes in the testFile are loaded in the
		// editor
		editor.doRevertToSaved();
		SpecfileCompletionProcessor complProcessor = new SpecfileCompletionProcessor();
		assertNotNull(complProcessor);
		editor.getSpecfileSourceViewer().setSelectedRange(0, 0);
		ICompletionProposal[] proposals = complProcessor.computeCompletionProposals(editor.getSpecfileSourceViewer(),
				0);
		int sourceComplCount = 0;
		for (ICompletionProposal proposal : proposals) {
			if (proposal.getDisplayString().startsWith("%{SOURCE")) {
				++sourceComplCount;
			}
		}
		assertEquals(occurances, sourceComplCount);
	}

	@Test
	public void testComputeComplProposalsOneSource() throws Exception {
		computeCompletionProposals(ONE_SOURCE, 1);
	}

	@Test
	public void testComputeComplProposalsNoSource() throws Exception {
		computeCompletionProposals(NO_SOURCE, 0);
	}

	@Test
	public void testComputeComplProposalsThreeSourceSeparated() throws Exception {
		computeCompletionProposals(THREE_SOURCE_SEPARATED, 3);
	}

	@Test
	public void testBRCompletionOrder() throws Exception {
		setPackageList(new String[] { "package3", "package2", "package4", "package1" });
		SpecfileEditor editor = initEditor(BUILD_REQUIRES);
		testProject.refresh();
		editor.doRevertToSaved();

		editor.getSpecfileSourceViewer().setSelectedRange(BUILD_REQUIRES.length(), 0);
		SpecfileCompletionProcessor processor = new SpecfileCompletionProcessor();

		ICompletionProposal[] proposals = processor.computeCompletionProposals(editor.getSpecfileSourceViewer(),
				BUILD_REQUIRES.length());

		assertTrue(proposals.length > 1, "Cannot perform test; not enough proposals");

		ICompletionProposal previous = proposals[0];

		for (int i = 1; i < proposals.length; i++) {
			ICompletionProposal current = proposals[i];
			assertTrue(previous.getDisplayString().compareToIgnoreCase(current.getDisplayString()) < 0,
					"Proposals are not in alphabetical order");
			previous = current;
		}

	}

	@Test
	public void testBRCompletionNonAlphaDot() throws Exception {
		setPackageList(new String[] { "java-1.5.0-gcj", "java-1.7.0-openjdk", "java-1.7.0-openjdk-devel",
				"java-1.7.0-openjdk-javadoc" });
		SpecfileEditor editor = initEditor(NON_ALPHA_DOT);
		testProject.refresh();
		// This is needed so the changes in the testFile are loaded in the
		// editor
		editor.doRevertToSaved();
		SpecfileCompletionProcessor complProcessor = new SpecfileCompletionProcessor();
		assertNotNull(complProcessor);
		editor.getSpecfileSourceViewer().setSelectedRange(NON_ALPHA_DOT.length(), 0);
		ICompletionProposal[] proposals = complProcessor.computeCompletionProposals(editor.getSpecfileSourceViewer(),
				NON_ALPHA_DOT.length());
		int sourceComplCount = 0;
		for (ICompletionProposal proposal : proposals) {
			if (proposal.getDisplayString().startsWith("java-1.")) {
				++sourceComplCount;
			}
		}
		assertEquals(4, sourceComplCount);
	}

	@Test
	public void testBRCompletionNonAlphaPlus() throws Exception {
		setPackageList(new String[] { "libstdc++", "libstdc++-devel" });
		SpecfileEditor editor = initEditor(NON_ALPHA_PLUS);
		testProject.refresh();
		// This is needed so the changes in the testFile are loaded in the
		// editor
		editor.doRevertToSaved();
		SpecfileCompletionProcessor complProcessor = new SpecfileCompletionProcessor();
		assertNotNull(complProcessor);
		editor.getSpecfileSourceViewer().setSelectedRange(NON_ALPHA_PLUS.length(), 0);
		ICompletionProposal[] proposals = complProcessor.computeCompletionProposals(editor.getSpecfileSourceViewer(),
				NON_ALPHA_PLUS.length());
		int sourceComplCount = 0;
		for (ICompletionProposal proposal : proposals) {
			if (proposal.getDisplayString().startsWith("libstdc+")) {
				++sourceComplCount;
			}
		}
		assertEquals(2, sourceComplCount);
	}

	/**
	 * Set the potential rpm package list to the given list. Useful for testing
	 * package proposals.
	 *
	 * @param packages
	 */
	private void setPackageList(String[] packages) {
		ScopedPreferenceStore prefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		prefStore.setValue(PreferenceConstants.P_RPM_LIST_FILEPATH, "/tmp/pkglist1");
		prefStore.setValue(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD, false);

		try (BufferedWriter out = new BufferedWriter(new FileWriter("/tmp/pkglist1"))) {
			for (String packageName : packages) {
				out.write(packageName + "\n");
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
		Activator.packagesList = null;
	}
}
