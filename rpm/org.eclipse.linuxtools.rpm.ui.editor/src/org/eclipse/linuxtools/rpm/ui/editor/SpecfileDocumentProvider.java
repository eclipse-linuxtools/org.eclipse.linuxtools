/*******************************************************************************
 * Copyright (c) 2007-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class SpecfileDocumentProvider extends TextFileDocumentProvider {

	private IDocument document;

	@Override
	public IDocument getDocument(Object element) {
		document = super.getDocument(element);
		if (document != null && document.getDocumentPartitioner() == null) {
			SpecfilePartitioner partitioner = new SpecfilePartitioner(
					new SpecfilePartitionScanner(),
					SpecfilePartitionScanner.SPEC_PARTITION_TYPES);
			partitioner.connect(document, false);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
}