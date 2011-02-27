package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class SpecfileConfiguration extends SourceViewerConfiguration {
	private SpecfileDoubleClickStrategy doubleClickStrategy;
//	private SpecfileTagScanner tagScanner;
	private SpecfileScanner scanner;
	private SpecfileChangelogScanner changelogScanner;
	private ColorManager colorManager;
	private SpecfileHover specfileHover;
	private SpecfileEditor editor;

	public SpecfileConfiguration(ColorManager colorManager, SpecfileEditor editor) {
		this.colorManager = colorManager;
		this.editor = editor;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return SpecfilePartitionScanner.SPEC_PARTITION_TYPES;
	}
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new SpecfileDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected SpecfileScanner getSpecfileScanner() {
		if (scanner == null) {
			scanner = new SpecfileScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(ISpecfileColorConstants.DEFAULT))));
		}
		return scanner;
	}
	
	protected SpecfileChangelogScanner getSpecfileChangelogScanner() {
		if  (changelogScanner == null) {
			changelogScanner = new SpecfileChangelogScanner(colorManager);
			changelogScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(ISpecfileColorConstants.DEFAULT))));
		}
		return changelogScanner;
	}
	
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (specfileHover == null)
			specfileHover = new SpecfileHover(this.editor);
		return specfileHover;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSpecfileScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
//
//                dr = new DefaultDamagerRepairer(getSpecfileScanner());
//                reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_DEFAULT);
//                reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_DEFAULT);
                
		dr = new DefaultDamagerRepairer(getSpecfileScanner());
		reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_SCRIPT);
		reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_SCRIPT);
		
		dr = new DefaultDamagerRepairer(getSpecfileScanner());
		reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_FILES);
		reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_FILES);
		
		dr = new DefaultDamagerRepairer(getSpecfileChangelogScanner());
		reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_CHANGELOG);
		reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_CHANGELOG);

		return reconciler;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (editor != null && editor.isEditable()) {
			MonoReconciler reconciler= new MonoReconciler(new SpecfileReconcilingStrategy(editor), false);
			reconciler.setDelay(1000);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			return reconciler;
		}
		return null;
	}

}