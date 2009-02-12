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
package org.eclipse.linuxtools.valgrind.massif.tests;

import java.io.File;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.valgrind.massif.MassifTreeViewer;
import org.eclipse.linuxtools.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class DoubleClickTest extends AbstractMassifTest {
	private MassifHeapTreeNode node;

	@Override
	public void setUp() throws Exception {
		proj = createProject("alloctest"); //$NON-NLS-1$
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		config.launch(ILaunchManager.PROFILE_MODE, null, true);
		
		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		MassifTreeViewer treeViewer = (MassifTreeViewer) view.getTreeViewer();
		
		MassifSnapshot[] snapshots = view.getSnapshots();
		node = snapshots[1].getRoot(); // first detailed
		TreePath path = new TreePath(new Object[] { node });
		while (node.getChildren().length > 0 && !node.hasSourceFile()) {
			node = node.getChildren()[0];
			path = path.createChildPath(node);
		}
		if (node.hasSourceFile()) {
			treeViewer.expandToLevel(node, TreeViewer.ALL_LEVELS);
			TreeSelection selection = new TreeSelection(path);

			// do double click
			IDoubleClickListener listener = treeViewer.getDoubleClickListener();
			listener.doubleClick(new DoubleClickEvent(treeViewer, selection));
		}
		else {
			fail();
		}		
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
	}
	
	public void testFile() throws Exception {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IResource expectedResource = proj.getProject().findMember(node.getFilename());
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
	
	public void testLine() throws Exception {
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
}
