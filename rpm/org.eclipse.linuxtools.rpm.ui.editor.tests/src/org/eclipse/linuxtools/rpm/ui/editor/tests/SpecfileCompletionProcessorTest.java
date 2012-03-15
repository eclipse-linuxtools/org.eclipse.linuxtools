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

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileCompletionProcessor;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;

public class SpecfileCompletionProcessorTest extends FileTestCase {

	public static final String ONE_SOURCE = "Source0: text.zip\n";
	public static final String NO_SOURCE = "Patch3: somefilesomewhere.patch"
			+ "\n" + "Patch2: someotherfile.patch\n";
	public static final String THREE_SOURCE_SEPARATED = "Source0: text.zip"
			+ "\n" + "Patch0: first.patch" + "\n" + "Source2: ant.jar" + "\n"
			+ "Source3: main.tar.gz";

	private SpecfileEditor initEditor(String contents) throws Exception {
		newFile(contents);
		IEditorPart openEditor = IDE.openEditor(Activator.getDefault()
				.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				testFile);
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

}
