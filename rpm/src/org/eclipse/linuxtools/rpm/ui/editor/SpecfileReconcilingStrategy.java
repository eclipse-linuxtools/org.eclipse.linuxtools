package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.linuxtools.rpm.ui.editor.outline.SpecfileContentOutlinePage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class SpecfileReconcilingStrategy implements IReconcilingStrategy {

	SpecfileContentOutlinePage outline;
	int lastRegionOffset;
	SpecfileEditor editor;
	IDocumentProvider documentProvider;
	
	public SpecfileReconcilingStrategy(SpecfileEditor editor) {
		outline= editor.getOutlinePage();
		lastRegionOffset = Integer.MAX_VALUE;
		this.editor = editor;
		documentProvider = editor.getDocumentProvider();
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

	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// TODO Auto-generated method stub

	}

	public void setDocument(IDocument document) {
		// TODO Auto-generated method stub

	}

}
