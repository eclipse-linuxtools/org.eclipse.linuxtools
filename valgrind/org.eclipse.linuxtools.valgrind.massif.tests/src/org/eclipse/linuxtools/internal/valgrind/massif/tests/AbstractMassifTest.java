/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot.SnapshotType;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;


public abstract class AbstractMassifTest extends AbstractValgrindTest {

	@Override
	protected String getToolID() {
		return MassifPlugin.TOOL_ID;
	}

	protected void checkFile(IProject proj, MassifHeapTreeNode node) {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IResource expectedResource = proj.findMember(node.getFilename());
			if (expectedResource != null) {
				File expectedFile = expectedResource.getLocation().toFile();
				File actualFile = fileInput.getFile().getLocation().toFile();
				assertEquals(expectedFile, actualFile);
			}
			else {
				fail();
			}
		}
		else {
			fail();
		}
	}

	protected void checkLine(MassifHeapTreeNode node) {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;

			ISelection selection = textEditor.getSelectionProvider().getSelection();
			if (selection instanceof TextSelection) {
				TextSelection textSelection = (TextSelection) selection;
				int line = textSelection.getStartLine() + 1; // zero-indexed

				assertEquals(node.getLine(), line);
			}
			else {
				fail();
			}
		}
		else {
			fail();
		}
	}

	/**
	 * Check snapshots contain the expected used bytes.
	 *
	 * @param snapshots MassifSnapshots to check
	 * @param usefulBytesDelta scale of useful bytes
	 * @param extraBytesDelta scale of extra bytes
	 */
	protected void checkSnapshots(MassifSnapshot[] snapshots, int usefulBytesDelta, int extraBytesDelta){
		long expectedHeapBytes = 0;
		long expectedHeapExtraBytes = 0;
		boolean pastPeakUsage = false;

		for (MassifSnapshot snapshot : snapshots) {
			if (snapshot.getTime() == 0) {
				// no need to update expected values
			} else if (snapshot.getType().compareTo(SnapshotType.PEAK) == 0) {
				pastPeakUsage = true;
				// no need to update expected values
			} else if (!pastPeakUsage) {
				expectedHeapBytes += usefulBytesDelta;
				expectedHeapExtraBytes += extraBytesDelta;
			} else {
				// past the peak , heap bytes used begin to decrease
				expectedHeapBytes -= usefulBytesDelta;
				expectedHeapExtraBytes -= extraBytesDelta;
			}

			assertEquals(expectedHeapBytes, snapshot.getHeapBytes());
			assertEquals(expectedHeapExtraBytes, snapshot.getHeapExtra());
			assertEquals(expectedHeapBytes + expectedHeapExtraBytes, snapshot.getTotal());
		}
	}

}
