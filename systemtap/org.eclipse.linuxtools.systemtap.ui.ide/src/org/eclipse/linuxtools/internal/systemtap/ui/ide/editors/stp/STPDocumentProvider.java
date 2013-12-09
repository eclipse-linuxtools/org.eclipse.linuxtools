/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class STPDocumentProvider extends TextFileDocumentProvider {

	@Override
	public void connect(Object element) throws CoreException {
		super.connect(element);
		setupDocument(this.getDocument(element));
	}

	protected void setupDocument(IDocument document) {
		if (document != null) {
			IDocumentPartitioner partitioner = new FastPartitioner(
					new STPPartitionScanner(), STPPartitionScanner.STP_PARTITION_TYPES);
			partitioner.connect(document);
			IDocumentPartitioner partitioner2 = new FastPartitioner(
					new STPProbeScanner(), STPProbeScanner.STP_PROBE_PARTITION_TYPES);
			partitioner2.connect(document);
			((IDocumentExtension3)document).setDocumentPartitioner(STPPartitionScanner.STP_PARTITIONING, partitioner);
			((IDocumentExtension3)document).setDocumentPartitioner(STPProbeScanner.STP_PROBE_PARTITIONING, partitioner2);
		}
	}

	/**
	 * Instantiates and returns a new AnnotationModel object.
	 */
	@Override
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return new AnnotationModel();
	}

}
