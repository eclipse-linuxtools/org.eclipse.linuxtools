/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.linuxtools.internal.docker.editor.scanner.DockerPartitionScanner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class DockerDocumentProvider extends FileDocumentProvider {
	
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			DockerPartitionScanner scanner = new DockerPartitionScanner();
			IDocumentPartitioner partitioner = new FastPartitioner(scanner, DockerPartitionScanner.ALLOWED_CONTENT_TYPES);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
}