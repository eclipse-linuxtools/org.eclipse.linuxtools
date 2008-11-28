/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.scanners.SpecfileCompletionProcessor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

public class SpecfileCompletionProcessorTest extends TestCase {

	private IFile testFile;
	private SpecfileTestProject testProject;
	public static final String ONE_SOURCE = "Source0: text.zip\n";
	public static final String NO_SOURCE = "Patch3: somefilesomewhere.patch"
			+ "\n" + "Patch2: someotherfile.patch\n";
	public static final String THREE_SOURCE_SEPARATED = "Source0: text.zip"
			+ "\n" + "Patch0: first.patch" + "\n" + "Source2: ant.jar" + "\n"
			+ "Source3: main.tar.gz";

	private SpecfileEditor initEditor(String contents) throws Exception {
		testFile.setContents(new ByteArrayInputStream(contents.getBytes()),
				true, false, null);
		IEditorPart openEditor = IDE
				.openEditor(Activator.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getActivePage(), testFile,
						"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor", true);
		return (SpecfileEditor) openEditor;
	}

	@Override
	protected void setUp() throws Exception {
		testProject = new SpecfileTestProject();
		testFile = testProject.createFile("testspecfile.spec");
	}

	@Override
	protected void tearDown() throws Exception {
		testProject.dispose();
	}

	private synchronized void computeCompletionProposals(String specContent,
			int occurances) throws Exception {
		SpecfileEditor editor = initEditor(specContent);
		testProject.refresh();
		//This is needed so the changes in the testFile are loaded in the editor
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

	public void testComputeComplProposalsOneSource() throws Exception {
		computeCompletionProposals(ONE_SOURCE, 1);
	}

	public void testComputeComplProposalsNoSource() throws Exception {
		computeCompletionProposals(NO_SOURCE, 0);
	}

	public void testComputeComplProposalsThreeSourceSeparated()
			throws Exception {
		computeCompletionProposals(THREE_SOURCE_SEPARATED, 3);
	}

}
