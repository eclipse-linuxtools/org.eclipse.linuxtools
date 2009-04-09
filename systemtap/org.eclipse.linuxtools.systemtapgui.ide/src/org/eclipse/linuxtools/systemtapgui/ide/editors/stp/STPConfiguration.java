/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtapgui.ide.editors.stp;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.linuxtools.systemtapgui.editor.ColorManager;
import org.eclipse.linuxtools.systemtapgui.editor.DoubleClickStrategy;
import org.eclipse.linuxtools.systemtapgui.editor.NonRuleBasedDamagerRepairer;
import org.eclipse.linuxtools.systemtapgui.ide.internal.IDEPlugin;
import org.eclipse.linuxtools.systemtapgui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtapgui.logging.LogManager;
import org.eclipse.swt.graphics.RGB;



public class STPConfiguration extends SourceViewerConfiguration {
	private DoubleClickStrategy doubleClickStrategy;
	private STPScanner scanner;
	private ColorManager colorManager;

	public STPConfiguration(ColorManager colorManager) {
		LogManager.logDebug("Start STPConfiguration: colorManager-" + colorManager, this);
		this.colorManager = colorManager;
		LogManager.logDebug("End STPConfiguration:", this);
	}
	
	/**
	 * Instantiates and returns a ContentAssistant object and sets the options on it.
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		LogManager.logDebug("Start getContentAssistant: sourceViewer-" + sourceViewer, this);
		ContentAssistant assistant= new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(new STPCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
	
		assistant.enableAutoActivation(true);
		
		assistant.enableAutoInsert(true);
		assistant.enablePrefixCompletion(true);
		
		IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
		int delay = p.getInt(IDEPreferenceConstants.P_ACTIVATION_DELAY);
		
		assistant.setAutoActivationDelay(delay); // 500
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		
		//assistant.setContextInformationPopupBackground(colorManager.getColor(new RGB(150,150,000)));
		
		LogManager.logDebug("End getContentAssistant: returnVal-" + assistant, this);
		return assistant;
	}
	
	/**
	 * Defines the text types used in the editor.
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		LogManager.logDebug("Start/End getConfiguredContentTypes: sourceViewer-" + sourceViewer, this);
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			STPPartitionScanner.STP_COMMENT,
			STPPartitionScanner.STP_EMBEDDEDC,
			STPPartitionScanner.STP_EMBEDDED};
	}

	/**
	 * Instantiates and returns a double click strategy object if one does not exist, and returns the 
	 * current one if it does.
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer,String contentType) {
		LogManager.logDebug("Start getDoubleClickStrategy: sourceViewer-" + sourceViewer + ", contentType-" + contentType, this);
		if (doubleClickStrategy == null)
			doubleClickStrategy = new DoubleClickStrategy();
		LogManager.logDebug("End getDoubleClickStrategy: returnVal-" + doubleClickStrategy, this);
		return doubleClickStrategy;
	}

	/**
	 * Instantiates and returns a scanner object if one does not exist, and returns the 
	 * current one if it does.
	 */
	protected STPScanner getSTPScanner() {
		LogManager.logDebug("Start getSTPScanner:", this);
		if (scanner == null) {
			scanner = new STPScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(colorManager.getColor(ISTPColorConstants.DEFAULT))));
		}
		LogManager.logDebug("End getSTPScanner: returnVal-" + scanner, this);
		return scanner;
	}

	/**
	 * Initiates and sets up damage repair objects in order to repaint specific sections of the editor
	 * when they are dirtied by the user.
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		LogManager.logDebug("Start getPresentationReconciler: sourceViewer-" + sourceViewer, this);
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSTPScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();
		RGB comment = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_STP_COMMENT_COLOR);
		RGB embeddedc = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_STP_EMBEDDED_C_COLOR);
		RGB embedded = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_STP_EMBEDDED_COLOR);
		
		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(colorManager.getColor(comment)));
		reconciler.setDamager(ndr, STPPartitionScanner.STP_COMMENT);
		reconciler.setRepairer(ndr, STPPartitionScanner.STP_COMMENT);
		
		NonRuleBasedDamagerRepairer ndr2 =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(colorManager.getColor(embeddedc)));
		reconciler.setDamager(ndr2, STPPartitionScanner.STP_EMBEDDEDC);
		reconciler.setRepairer(ndr2, STPPartitionScanner.STP_EMBEDDEDC);
		
		NonRuleBasedDamagerRepairer ndr3 =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(colorManager.getColor(embedded)));
		reconciler.setDamager(ndr3, STPPartitionScanner.STP_EMBEDDED);
		reconciler.setRepairer(ndr3, STPPartitionScanner.STP_EMBEDDED);

		LogManager.logDebug("End getPresentationReconciler: returnVal-" + reconciler, this);
		return reconciler;
	}
}