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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.linuxtools.systemtap.ui.editor.SimpleDocumentProvider;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;



/**
 * The DocumentProvider class used when handling documents containing C code.
 * @see org.eclipse.linuxtools.systemtap.ui.editor.SimpleDocumentProvider
 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class CDocumentProvider extends SimpleDocumentProvider {
	protected void setupDocument(IDocument document) {
		LogManager.logDebug("Start setupDocument: document-" + document, this); //$NON-NLS-1$
		if (document != null) {
			IDocumentPartitioner partitioner =
				new FastPartitioner(
					new CPartitionScanner(),
					new String[] {CPartitionScanner.C_COMMENT});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		LogManager.logDebug("End setupDocument:", this); //$NON-NLS-1$
	}

	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		LogManager.logDebug("Start/End createAnnotationModel: element-" + element, this); //$NON-NLS-1$
		return new AnnotationModel();
	}
}
