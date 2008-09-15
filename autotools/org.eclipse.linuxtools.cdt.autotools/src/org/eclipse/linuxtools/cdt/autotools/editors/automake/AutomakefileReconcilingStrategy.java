/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat, Inc.
 * 
 * Largely copied from MakefileReconcilingStrategy which has the following
 * copyright notice:
 * 
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.cdt.autotools.editors.automake;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.linuxtools.cdt.autotools.internal.editors.automake.IReconcilingParticipant;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class AutomakefileReconcilingStrategy implements IReconcilingStrategy {

	private int fLastRegionOffset;
	private ITextEditor fEditor;	
	private IWorkingCopyManager fManager;
	private IDocumentProvider fDocumentProvider;
	private AutomakefileContentOutlinePage fOutliner;
	private IReconcilingParticipant fMakefileReconcilingParticipant;
	private AutomakeErrorHandler fErrorHandler;
	private IEditorInput input;
	
	public AutomakefileReconcilingStrategy(AutomakeEditor editor) {
		fOutliner= editor.getAutomakeOutlinePage();
		fLastRegionOffset = Integer.MAX_VALUE;
		fEditor= editor;
		input = (IEditorInput) fEditor.getEditorInput();
		fManager= AutomakeEditorFactory.getDefault().getWorkingCopyManager();
		fDocumentProvider= AutomakeEditorFactory.getDefault().getAutomakefileDocumentProvider();
		fErrorHandler= new AutomakeErrorHandler(fDocumentProvider.getDocument(input));
		if (fEditor instanceof IReconcilingParticipant) {
			fMakefileReconcilingParticipant= (IReconcilingParticipant)fEditor;
		}
	}
	
	/**
	 * @see IReconcilingStrategy#reconcile(document)
	 */
	public void setDocument(IDocument document) {
	}	


	/**
	 * @see IReconcilingStrategy#reconcile(region)
	 */
	public void reconcile(IRegion region) {
		// We use a trick to avoid running the reconciler multiple times
		// on a file when it gets changed. This is because this gets called
		// multiple times with different regions of the file, we do a 
		// complete parse on the first region.
		if(region.getOffset() <= fLastRegionOffset) {
			reconcile();
		}
		fLastRegionOffset = region.getOffset();
	}

	/**
	 * @see IReconcilingStrategy#reconcile(dirtyRegion, region)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		// FIXME: This seems to generate too much flashing in
		// the contentoutline viewer.
		//reconcile();
	}
	
	private void reconcile() {
		try {
			IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
			if (makefile != null) {
				String content = fDocumentProvider.getDocument(input).get();
				StringReader reader = new StringReader(content);
				try {
					makefile.parse(makefile.getFileURI(), reader);
				} catch (IOException e) {
				}
				
				fOutliner.update();
				fErrorHandler.update(makefile);
			}
		} finally {
			try {
				if (fMakefileReconcilingParticipant != null) {
					fMakefileReconcilingParticipant.reconciled();
				}
			} finally {
				//
			}
		}
 	}	

}
