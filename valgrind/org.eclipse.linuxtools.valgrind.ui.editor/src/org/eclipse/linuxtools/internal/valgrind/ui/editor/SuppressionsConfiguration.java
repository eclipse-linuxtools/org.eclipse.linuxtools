/*******************************************************************************
 * Copyright (c) 2008, 2017 Phil Muldoon and others.
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
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class SuppressionsConfiguration extends SourceViewerConfiguration {

    private SuppressionsEditor editor;

    public SuppressionsConfiguration(SuppressionsEditor editor) {
        this.editor = editor;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return SuppressionsPartitionScanner.SUPP_CONTENT_TYPES;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer) {

        return new SuppPresentationReconciler();
    }

    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
        SuppressionsReconcilingStrategy strategy = new SuppressionsReconcilingStrategy(editor);
        return new MonoReconciler(strategy, false);
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
