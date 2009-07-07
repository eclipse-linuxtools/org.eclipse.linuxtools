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



import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.linuxtools.systemtap.ui.editor.SimpleDocumentProvider;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;



public class STPDocumentProvider extends SimpleDocumentProvider {
	
	/**
	 * Sets up a IDocumentPartitioner object provided the document isn't empty and specifies the partition 
	 * scanners involved.
	 */
	protected void setupDocument(IDocument document) {
		LogManager.logDebug("Start setupDocument: document-" + document, this);
		if (document != null) {
			IDocumentPartitioner partitioner =
				new FastPartitioner(
					new STPPartitionScanner(),
					new String[] {
						STPPartitionScanner.STP_COMMENT,
						STPPartitionScanner.STP_EMBEDDEDC,
						STPPartitionScanner.STP_EMBEDDED});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		LogManager.logDebug("End setupDocument:", this);
	}
	
		
    /*public IDocument getDocument(Object element) {
        document = super.getDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner =
				new FastPartitioner(
					new STPPartitionScanner(),
					new String[] {
						STPPartitionScanner.STP_COMMENT,
						STPPartitionScanner.STP_EMBEDDEDC,
						STPPartitionScanner.STP_EMBEDDED});
			partitioner.connect(document);
                if (document.getDocumentPartitioner() == null)
                        document.setDocumentPartitioner(partitioner);
        }
        return document;
}*/


	/**
	 * Instantiates and returns a new AnnotationModel object.
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		LogManager.logDebug("Start/End createAnnotationModel: element-" + element, this);
		return new AnnotationModel();
	}
	
//	private IDocument document;
}