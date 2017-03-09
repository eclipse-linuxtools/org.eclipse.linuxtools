/*******************************************************************************
 * Copyright (c) 2006, 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.linuxtools.changelog.core.IEditorChangeLogContrib;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * GNU format ChangeLog editor configuration.
 */
public class GNUEditorConfiguration extends TextSourceViewerConfiguration implements IEditorChangeLogContrib {

	public static final String CHANGELOG_PARTITIONING = "gnu_changelog_partitioning"; //$NON-NLS-1$

	/**
	 * Prepares configuration.
	 */
	public GNUEditorConfiguration() {
	}

	/**
	 * Get configured content types.
	 *
	 * @return array of configured content types.
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, GNUPartitionScanner.CHANGELOG_EMAIL,
				GNUPartitionScanner.CHANGELOG_SRC_ENTRY };
	}

	/**
	 * Detects hyperlinks in GNU formatted changelogs.
	 *
	 * @return link detector for GNU format.
	 */
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null)
			return null;

		return getRegisteredHyperlinkDetectors(sourceViewer);
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return CHANGELOG_PARTITIONING;
	}

	/**
	 * Set content formatter. For ChangeLog, it just wraps lines.
	 */
	@Override
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {

		ContentFormatter cf = new ContentFormatter();

		// no partitions
		cf.enablePartitionAwareFormatting(false);

		ChangeLogFormattingStrategy cfs = new ChangeLogFormattingStrategy();

		cf.setFormattingStrategy(cfs, IDocument.DEFAULT_CONTENT_TYPE);

		return cf;
	}

	/**
	 * Highlights GNU format changelog syntaxes.
	 *
	 * @return reconciler for GNU format changelog.
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		return new GNUPresentationReconciler();
	}

	/**
	 * Perform documentation setup to set up partitioning.
	 *
	 * @param document
	 *            to set up partitioning on.
	 */
	@Override
	public void setup(IDocument document) {
		FastPartitioner partitioner = new FastPartitioner(new GNUPartitionScanner(),
				GNUPartitionScanner.CHANGELOG_PARTITION_TYPES);
		partitioner.connect(document);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(CHANGELOG_PARTITIONING, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
	}

}
