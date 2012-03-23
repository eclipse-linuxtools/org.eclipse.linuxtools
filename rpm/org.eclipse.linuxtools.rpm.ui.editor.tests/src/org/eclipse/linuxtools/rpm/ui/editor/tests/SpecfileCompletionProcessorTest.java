/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileCompletionProcessor;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;

public class SpecfileCompletionProcessorTest extends FileTestCase {

	public static final String ONE_SOURCE = "Source0: text.zip\n";
	public static final String NO_SOURCE = "Patch3: somefilesomewhere.patch"
			+ "\n" + "Patch2: someotherfile.patch\n";
	public static final String THREE_SOURCE_SEPARATED = "Source0: text.zip"
			+ "\n" + "Patch0: first.patch" + "\n" + "Source2: ant.jar" + "\n"
			+ "Source3: main.tar.gz";

	private static final String BUILD_REQUIRES =  "BuildRequires: p";
	
	private SpecfileEditor initEditor(String contents) throws Exception {
		newFile(contents);
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), testFile);

		return (SpecfileEditor) openEditor;
	}

	private synchronized void computeCompletionProposals(String specContent,
			int occurances) throws Exception {
		SpecfileEditor editor = initEditor(specContent);
		testProject.refresh();
		// This is needed so the changes in the testFile are loaded in the
		// editor
		editor.doRevertToSaved();
		SpecfileCompletionProcessor complProcessor = new SpecfileCompletionProcessor(
				editor);
		assertNotNull(complProcessor);
		editor.getSpecfileSourceViewer().setSelectedRange(0, 0);
		ICompletionProposal[] proposals = complProcessor
				.computeCompletionProposals(editor.getSpecfileSourceViewer(), 0);
		int sourceComplCount = 0;
		for (int i = 0; i < proposals.length; i++) {
			ICompletionProposal proposal = proposals[i];
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
	public void testComputeComplProposalsThreeSourceSeparated()
			throws Exception {
		computeCompletionProposals(THREE_SOURCE_SEPARATED, 3);
	}

	@Test
	public void testBRCompletionOrder() throws Exception {
		setPackageList(new String[]{"package3", "package2", "package4", "package1"});
		SpecfileEditor editor = initEditor(BUILD_REQUIRES);
		testProject.refresh();
		editor.doRevertToSaved();

		editor.getSpecfileSourceViewer().setSelectedRange(BUILD_REQUIRES.length(), 0);
		SpecfileCompletionProcessor processor = new SpecfileCompletionProcessor(
				editor);
		
		ICompletionProposal[] proposals = processor.computeCompletionProposals(
				editor.getSpecfileSourceViewer(), BUILD_REQUIRES.length());
		
		assertTrue("Cannot perform test; not enough proposals", proposals.length > 1);

		ICompletionProposal previous = proposals[0];
		
		for (int i = 1; i < proposals.length; i++){
			ICompletionProposal current = proposals[i];
			assertTrue("Proposals are not in alphabetical order",
						previous.getDisplayString().compareToIgnoreCase (current.getDisplayString()) < 0);
			previous = current;
		}

	}
}
