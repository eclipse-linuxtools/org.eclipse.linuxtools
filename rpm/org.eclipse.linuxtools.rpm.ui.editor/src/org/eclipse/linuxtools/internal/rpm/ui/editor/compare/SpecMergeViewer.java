/*******************************************************************************
 * Copyright (c) 2009, 2017 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileConfiguration;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.swt.widgets.Composite;

/**
 * Merge viewer for the files.
 *
 */
public class SpecMergeViewer extends TextMergeViewer {

	/**
	 * Creates a new SpecMergeViewer.
	 *
	 * @param parent
	 *            The parent control.
	 * @param configuration
	 *            The compare configuration.
	 *
	 * @see TextMergeViewer#TextMergeViewer(Composite, CompareConfiguration)
	 */
	public SpecMergeViewer(Composite parent, CompareConfiguration configuration) {
		super(parent, configuration);
	}

	@Override
	public String getTitle() {
		return Messages.SpecMergeViewer_0;
	}

	@Override
	protected IDocumentPartitioner getDocumentPartitioner() {
		return new FastPartitioner(new SpecfilePartitionScanner(), SpecfilePartitionScanner.SPEC_PARTITION_TYPES);
	}

	@Override
	protected String getDocumentPartitioning() {
		return SpecfilePartitionScanner.SPEC_FILE_PARTITIONING;
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			SpecfileEditor editor = new SpecfileEditor();
			((SourceViewer) textViewer).configure(new SpecfileConfiguration(editor));
		}
	}

}
