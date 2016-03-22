/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.linuxtools.internal.docker.editor.assist.CompletionProcessor;
import org.eclipse.linuxtools.internal.docker.editor.scanner.DockerCommentScanner;
import org.eclipse.linuxtools.internal.docker.editor.scanner.DockerInstructionScanner;
import org.eclipse.linuxtools.internal.docker.editor.scanner.DockerPartitionScanner;
import org.eclipse.linuxtools.internal.docker.editor.syntax.SyntaxReconcilingStrategy;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class DockerConfiguration extends TextSourceViewerConfiguration {

	private DockerEditor editor;
	
	public DockerConfiguration(DockerEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		DefaultDamagerRepairer dr;

		dr = new DefaultDamagerRepairer(new DockerCommentScanner());
		reconciler.setDamager(dr, DockerPartitionScanner.TYPE_COMMENT);
		reconciler.setRepairer(dr, DockerPartitionScanner.TYPE_COMMENT);

		dr = new DefaultDamagerRepairer(new DockerInstructionScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		Reconciler reconciler = new Reconciler();
		IReconcilingStrategy strategy = new SyntaxReconcilingStrategy(editor);
		reconciler.setReconcilingStrategy(strategy, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant ca = new ContentAssistant();
		IContentAssistProcessor cap = new CompletionProcessor();
		ca.setContentAssistProcessor(cap, IDocument.DEFAULT_CONTENT_TYPE);
		ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return ca;
	}
	
	@Override
	protected boolean isShownInText(Annotation annotation) {
		return true;
	}
}
