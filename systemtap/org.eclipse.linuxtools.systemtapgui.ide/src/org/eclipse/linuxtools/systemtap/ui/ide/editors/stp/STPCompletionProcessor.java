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

package org.eclipse.linuxtools.systemtap.ui.ide.editors.stp;

import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.linuxtools.systemtap.ui.editor.WhitespaceDetector;
import org.eclipse.linuxtools.systemtap.ui.ide.internal.IDEPlugin;
import org.eclipse.linuxtools.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;



public class STPCompletionProcessor implements IContentAssistProcessor {
	/**
	 * Build the probe tree.
	 */
	public void getProbes(TreeNode level, LinkedList<String> list) {
		TreeNode child;
		for(int i=0; i<level.getChildCount(); i++) {
			child = level.getChildAt(i);
			if(child.getData().toString().startsWith("probe"))
				list.add(child.toString());
			else
				getProbes(child, list);
		}
	}
	
	/**
	 * Initialization -- also disable code completion if probe tree broke to prevent crashing.
	 */
	public STPCompletionProcessor() {
		LogManager.logDebug("Start STPCompletionProcessor:", this);
		
		fValidator = new Validator();
		TreeNode tNode = TapsetLibrary.getProbes();
		
		//Turn off code completion if there is any problem reading the probes
		if (tNode == null) {
			IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
			p.setValue(IDEPreferenceConstants.P_USE_CODE_ASSIST, false);
			IDEPlugin.getDefault().savePluginPreferences();
			return;
		}
		
		LinkedList<String> list = new LinkedList<String>();
		getProbes(tNode, list);
		//LinkedList list = getProbes(tNode);
		for(int i=0; i<permanentProbes.length; i++)
			list.add(permanentProbes[i]);
		availableProbes = new String[list.size()];
		list.toArray(availableProbes);
		
		LogManager.logDebug("End STPCompletionProcessor:", this);
	}
	
	/**
	 * Simple content assist tip closer. The tip is valid in a range
	 * of 5 characters around its popup location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {
		protected int fInstallOffset;

		public boolean isContextInformationValid(int offset) {
			LogManager.logDebug("Start/End isContextInformationValid: offset-" + offset, this);
			return Math.abs(fInstallOffset - offset) < 5;
		}

		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			LogManager.logDebug("Start install: info-" + info + ", viewer-" + viewer + ", offset-" + offset, this);
			fInstallOffset= offset;
			LogManager.logDebug("End install:", this);
		}
		
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			LogManager.logDebug("Start/End updatePresentaion: documentPosition-" + documentPosition + ", presentation-" + presentation, this);
			return false;
		}
	}

	/**
	 * This method does the actual work in code completion. It checks the word prior to the character 
	 * that triggered code completion (generally '.') against the probe tree and also looks for if. If
	 * the previous word is a probe alias or if the completion code is invoked.
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		LogManager.logDebug("Start computeCompletionProposals: viewer-" + viewer + ", documentOffset-" + documentOffset, this);
		IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
		if (!p.getBoolean(IDEPreferenceConstants.P_USE_CODE_ASSIST)) return null;

		try {
			LinkedList<CompletionProposal> proposals = new LinkedList<CompletionProposal>();
			WhitespaceDetector white = new WhitespaceDetector();
			IDocument doc = viewer.getDocument();

			int set = 1;
			while (!(white.isWhitespace(doc.getChar(documentOffset - set)))) 
				set++;
			String pre = doc.get(documentOffset - set + 1, set - 1);

			String[] conditionalFilters = p.getString(IDEPreferenceConstants.P_CONDITIONAL_FILTERS).split(File.pathSeparator);
			
			if(pre.startsWith("if(")) {
				for(int i=0; i<conditionalFilters.length; i++) {
					if(conditionalFilters[i].startsWith(pre))
						proposals.add(new CompletionProposal(conditionalFilters[i], documentOffset-pre.length(), pre.length(), conditionalFilters[i].length()));
				}
			} else {
				for(int i=0; i<availableProbes.length; i++) {
					if(availableProbes[i].startsWith(pre))
						proposals.add(new CompletionProposal(availableProbes[i], documentOffset-pre.length(), pre.length(), availableProbes[i].length()));
				}
			}
			
			CompletionProposal[] props = new CompletionProposal[proposals.size()];
			proposals.toArray(props);
			return props;
		} catch(BadLocationException ble) {
			LogManager.logCritical("BadLocationException computeCompletionProposals: " + ble.getMessage(), this);
		}
		return null;
	}
	
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		LogManager.logDebug("Start computeContextInformation: viewer-" + viewer + ", documentOffset-" + documentOffset, this);
		IContextInformation[] result= new IContextInformation[5];
		for (int i= 0; i < result.length; i++)
			result[i]= new ContextInformation(
					MessageFormat.format("", new Object[] { new Integer(i), new Integer(documentOffset) }),
					MessageFormat.format("", new Object[] { new Integer(i), new Integer(documentOffset - 5), new Integer(documentOffset + 5)}));
		LogManager.logDebug("End computeContextInformation:", this);
		return null;
	}
	
	/**
	 * Return an array of characters that activate code assist.
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		LogManager.logDebug("Start/End getCompletionProposalAutoActivationCharacters:", this);
		return new char[] { '.', '(' };
	}
	
	/**
	 * Return an array of characters that activate context information.
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		LogManager.logDebug("Start/End getContextInformationAutoActivationCharacters:", this);
		return new char[] { '#' };
	}
	
	public IContextInformationValidator getContextInformationValidator() {
		LogManager.logDebug("Start/End getContextInformationValidator: returnVal-" + fValidator, this);
		return fValidator;
	}
	
	public String getErrorMessage() {
		LogManager.logDebug("Start/End getErrorMessage: returnVal-null", this);
		return null;
	}
	
	public void dispose() {
		LogManager.logDebug("Start dispose:", this);
		LogManager.logInfo("Disposing", this);
		availableProbes = null;
		fValidator = null;
		LogManager.logDebug("End dispose:", this);
	}
	
	protected IContextInformationValidator fValidator;
	
	protected static final String[] permanentProbes = {"timer", "timer.jiffies()", "timer.jiffies().randomize()", 
										"timer.ms()", "timer.ms().randomize()", "timer.profile", "kernel",
										"kernel.function()", "kernel.function.return", "kernel.inline()",
										"kernel.statement()", "module()", "module().function()",
										"module().function().return", "module().inline()", "module().statement()"};

	protected String[] availableProbes;
}