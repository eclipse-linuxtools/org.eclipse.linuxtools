package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.linuxtools.rpm.ui.editor.outline.SpecfileContentOutlinePage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class SpecfileReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private IDocument sDocument;
	private IProgressMonitor sProgressMonitor;
	private SpecfileParser sParser;
	private SpecfileFoldingStructureProvider sFoldingStructureProvider;
	
	SpecfileContentOutlinePage outline;
	int lastRegionOffset;
	SpecfileEditor editor;
	IDocumentProvider documentProvider;

	public SpecfileReconcilingStrategy(SpecfileEditor editor) {
		outline= editor.getOutlinePage();
		lastRegionOffset = Integer.MAX_VALUE;
		this.editor = editor;
		documentProvider = editor.getDocumentProvider();
		sParser= new SpecfileParser();
		sFoldingStructureProvider= new SpecfileFoldingStructureProvider(editor);
	}

	public void reconcile(IRegion partition) {
		try {
			Specfile specfile = editor.getSpecfile();
			SpecfileParser parser = editor.getParser();
			if (specfile != null) {
				editor.setSpecfile(parser.parse(documentProvider
						.getDocument(editor.getEditorInput())));
				outline.update();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setDocument(IDocument document) {
		sDocument= document;
		sFoldingStructureProvider.setDocument(sDocument);
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
		sProgressMonitor= monitor;
		sFoldingStructureProvider.setProgressMonitor(sProgressMonitor);
	}

	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		reconcile();
	}

	public void initialReconcile() {
		reconcile();
	}

	private void reconcile() {
		Specfile specfile= parseSpecfile();
		if (specfile != null) {
			updateEditor(specfile);
			updateFolding(specfile);
		}
	}

	private Specfile parseSpecfile() {
		return sParser.parse(sDocument);
	}

	private void updateEditor(final Specfile specfile) {
		return;
	}

	private void updateFolding(Specfile specfile) {
		sFoldingStructureProvider.updateFoldingRegions(specfile);
	}
}
