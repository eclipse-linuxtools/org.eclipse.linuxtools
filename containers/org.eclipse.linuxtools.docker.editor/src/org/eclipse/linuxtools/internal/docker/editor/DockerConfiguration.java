/*******************************************************************************
 * Copyright (c) 2015, 2017 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.linuxtools.internal.docker.editor.assist.CompletionProcessor;
import org.eclipse.linuxtools.internal.docker.editor.syntax.SyntaxReconcilingStrategy;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class DockerConfiguration extends TextSourceViewerConfiguration {

	private DockerEditor editor;
	private DockerHover hover;

	public DockerConfiguration(DockerEditor editor) {
		this.editor = editor;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		return new DockerPresentationReconciler();
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
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (hover == null) {
			hover = new DockerHover();
		}
		return hover;
	}

}
