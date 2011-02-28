/*******************************************************************************
 * Copyright (c) 2008, 2009 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API. 
 *    Red Hat - modifications for use with Valgrind plugins.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class SuppressionsConfiguration extends SourceViewerConfiguration {
	
	private SuppressionsEditor editor;
	private SuppressionsElementScanner elementScanner;

	public SuppressionsConfiguration(ColorManager colorManager, SuppressionsEditor editor) {
		this.editor = editor;
		elementScanner = new SuppressionsElementScanner(colorManager);
	}
		
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return SuppressionsPartitionScanner.SUPP_CONTENT_TYPES;
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(elementScanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		dr = new DefaultDamagerRepairer(elementScanner);
		reconciler.setDamager(dr, SuppressionsPartitionScanner.SUPP_TOOL);
		reconciler.setRepairer(dr, SuppressionsPartitionScanner.SUPP_TOOL);
		
		dr = new DefaultDamagerRepairer(elementScanner);
		reconciler.setDamager(dr, SuppressionsPartitionScanner.SUPP_TYPE);
		reconciler.setRepairer(dr, SuppressionsPartitionScanner.SUPP_TYPE);
		
		dr = new DefaultDamagerRepairer(elementScanner);
		reconciler.setDamager(dr, SuppressionsPartitionScanner.SUPP_CONTEXT);
		reconciler.setRepairer(dr, SuppressionsPartitionScanner.SUPP_CONTEXT);
		
		return reconciler;
	}
	
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		SuppressionsReconcilingStrategy strategy = new SuppressionsReconcilingStrategy(editor);      
        MonoReconciler reconciler = new MonoReconciler(strategy, false);        
        return reconciler;
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(new SuppressionsContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new SuppressionsContentAssistProcessor(), SuppressionsPartitionScanner.SUPP_TOOL);
		assistant.setContentAssistProcessor(new SuppressionsContentAssistProcessor(), SuppressionsPartitionScanner.SUPP_TYPE);
		assistant.setContentAssistProcessor(new SuppressionsContentAssistProcessor(), SuppressionsPartitionScanner.SUPP_CONTEXT);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		assistant.enableAutoInsert(true);
		return assistant;
	}
	
}
