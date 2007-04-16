package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.internal.text.link.contentassist.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class SpecfileConfiguration extends SourceViewerConfiguration {
	private SpecfileDoubleClickStrategy doubleClickStrategy;
	private SpecfileScanner scanner;
	private SpecfileChangelogScanner changelogScanner;
	private SpecfilePackagesScanner packagesScanner;
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
	
	protected SpecfilePackagesScanner getSpecfilePackagesScanner() {
		if  (packagesScanner == null) {
			packagesScanner = new SpecfilePackagesScanner(colorManager);
			packagesScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(ISpecfileColorConstants.DEFAULT))));
		}
		return packagesScanner;
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

		dr = new DefaultDamagerRepairer(getSpecfilePackagesScanner());
		reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_PACKAGES);
		reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_PACKAGES);
		
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

	/*
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (editor != null && editor.isEditable()) {
			SpecfileReconcilingStrategy strategy= new SpecfileReconcilingStrategy(editor);
			MonoReconciler reconciler= new MonoReconciler(strategy, false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant= new ContentAssistant();
		IContentAssistProcessor processor= new SpecfileCompletionProcessor(editor);
		// add content assistance to all the supported contentType
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_SCRIPT);
		assistant.setContentAssistProcessor(processor,SpecfilePartitionScanner.SPEC_FILES);
		assistant.setContentAssistProcessor(processor,SpecfilePartitionScanner.SPEC_CHANGELOG);		
		assistant.setContentAssistProcessor(processor,SpecfilePartitionScanner.SPEC_PACKAGES);	
		// configure content assistance
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        IInformationControlCreator controlCreator= getInformationControlCreator();
		assistant.setInformationControlCreator(controlCreator);
		assistant.enableAutoInsert(true);
		assistant.setStatusLineVisible(true);
		assistant.setStatusMessage("Press Ctrl+Space to see proposals");
		return assistant;
	}
	
	private IInformationControlCreator getInformationControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.H_SCROLL
						| SWT.V_SCROLL, new HTMLTextPresenter(false));
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null)
			return null;
		return new IHyperlinkDetector[] { new URLHyperlinkWithMacroDetector(editor.getSpecfile())};
	}
		
}