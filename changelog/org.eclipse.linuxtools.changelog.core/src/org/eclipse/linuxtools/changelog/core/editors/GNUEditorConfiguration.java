/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.linuxtools.changelog.core.IEditorChangeLogContrib;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.TextEditor;


/**
 * GNU format ChangeLog editor configuration.
 * 
 * @author klee (Kyu Lee)
 */
public class GNUEditorConfiguration extends SourceViewerConfiguration implements
		IEditorChangeLogContrib {

	private GNUElementScanner scanner;

	private ColorManager colorManager;

	private GNUHyperlinkDetector linkDetector;

	private final RGB DEFAULT_HYPERLINK_COLOR = new RGB(127, 0, 0);

	private TextEditor parentEditor;

	/**
	 * Prepares configuration.
	 */
	public GNUEditorConfiguration() {
		this.colorManager = new ColorManager();

	}

	/**
	 * Sets TextEditor that this configuration is going to be applied.
	 */
	public void setTextEditor(TextEditor editor) {
		parentEditor = editor;
	}

	/**
	 * Set default content type. GNU Changelog only has one type.
	 * 
	 * @return default content type.
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE };
	}

	protected GNUElementScanner getChangeLogFileScanner() {
		if (scanner == null) {
			scanner = new GNUElementScanner(colorManager);
			scanner.setDefaultReturnToken(new Token(new TextAttribute(
					colorManager.getColor(IChangeLogColorConstants.TEXT))));
		}
		return scanner;
	}

	/**
	 * Detects hyperlinks in GNU formatted changelogs.
	 * 
	 * @return link detector for GNU format.
	 */
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null)
			return null;

		if (linkDetector == null) {
			linkDetector = new GNUHyperlinkDetector(sourceViewer, parentEditor);

		}

		return new IHyperlinkDetector[] { linkDetector };
	}

	/**
	 * Hyperlink presenter (decorator).
	 * 
	 * @return default presenter.
	 */
	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
		return new DefaultHyperlinkPresenter(DEFAULT_HYPERLINK_COLOR);
	}
	
	
	/**
	 * Set content formatter. For ChangeLog, it just wraps lines.
	 */
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
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(
				getChangeLogFileScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

}
