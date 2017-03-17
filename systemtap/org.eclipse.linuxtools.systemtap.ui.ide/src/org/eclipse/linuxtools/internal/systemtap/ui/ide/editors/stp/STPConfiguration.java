/*******************************************************************************
 * Copyright (c) 2008, 2017 Phil Muldoon and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class STPConfiguration extends SourceViewerConfiguration {

    private STPEditor editor;
    private DoubleClickStrategy doubleClickStrategy;
    private STPCompletionProcessor processor;

    public STPConfiguration(STPEditor editor) {
        this.editor = editor;
        this.processor = new STPCompletionProcessor();
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {
                IDocument.DEFAULT_CONTENT_TYPE,
                STPPartitionScanner.STP_COMMENT,
                STPPartitionScanner.STP_CONDITIONAL};
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant = new ContentAssistant();

        assistant.enableAutoActivation(true);
        assistant.setAutoActivationDelay(500);
        assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
        assistant
                .setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);

        assistant.setContentAssistProcessor(processor,IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(processor,STPPartitionScanner.STP_CONDITIONAL);

        assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

        return assistant;
    }

    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer)
    {
        STPReconcilingStrategy strategy = new STPReconcilingStrategy();
        strategy.setEditor(editor);
        return new MonoReconciler(strategy,false);
    }

    /**
     * Instantiates and returns a double click strategy object if one does not exist, and returns the
     * current one if it does.
     */
    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer,String contentType) {
        if (doubleClickStrategy == null) {
            doubleClickStrategy = new DoubleClickStrategy();
        }
        return doubleClickStrategy;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer) {
       return new STPPresentationReconciler();
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(
            ISourceViewer sourceViewer, String contentType) {
        return new IAutoEditStrategy[] {new STPAutoEditStrategy(STPPartitionScanner.STP_PARTITIONING, null)};
    }

    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return processor;
    }

    @Override
    public String[] getDefaultPrefixes(ISourceViewer sourceViewer,
            String contentType) {
        return new String[] { "//", "" };  //$NON-NLS-1$//$NON-NLS-2$
    }

}