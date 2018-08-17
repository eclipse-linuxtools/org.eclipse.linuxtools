/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.linuxtools.internal.rpm.ui.editor.outline.SpecfileContentOutlinePage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class SpecfileReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private SpecfileFoldingStructureProvider sFoldingStructureProvider;

	SpecfileContentOutlinePage outline;
	SpecfileEditor editor;
	IDocumentProvider documentProvider;

	public SpecfileReconcilingStrategy(SpecfileEditor editor) {
		outline = (SpecfileContentOutlinePage) editor.getAdapter(IContentOutlinePage.class);
		this.editor = editor;
		documentProvider = editor.getDocumentProvider();
		sFoldingStructureProvider = new SpecfileFoldingStructureProvider(editor);
	}

	@Override
	public void setDocument(IDocument document) {
		sFoldingStructureProvider.setDocument(document);
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		sFoldingStructureProvider.setProgressMonitor(monitor);
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		reconcile();
	}

	@Override
	public void initialReconcile() {
		reconcile();
	}

	private void reconcile() {
		Specfile specfile = editor.getSpecfile();
		if (specfile != null) {
			editor.setSpecfile(editor.getParser().parse(documentProvider.getDocument(editor.getEditorInput())));
			outline.update();
			updateFolding();
			updateEditor();
		}
	}

	@Override
	public void reconcile(IRegion partition) {
		reconcile();
	}

	private void updateEditor() {
		Shell shell = editor.getSite().getShell();
		if (!(shell == null || shell.isDisposed())) {
			// Bug 538050
			if (documentProvider != null && documentProvider.getDocument(editor.getEditorInput()) != null) {
				shell.getDisplay().asyncExec(() -> editor
						.setSpecfile(editor.getParser().parse(documentProvider.getDocument(editor.getEditorInput()))));
			}
		}
	}

	private void updateFolding() {
		sFoldingStructureProvider.updateFoldingRegions();
	}
}
