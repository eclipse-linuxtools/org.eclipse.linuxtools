/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Extended implementation of <code>ProjectionViewer</code>
 * 
 */
class SpecfileProjectionViewer extends ProjectionViewer {
	private List<IAutoEditStrategy> textConverters;

	private boolean ignoreTextConverters = false;

	private SourceViewerConfiguration sourceViewerConfiguration;

	/**
	 * Default constructor
	 */
	public SpecfileProjectionViewer(Composite parent, IVerticalRuler ruler,
			IOverviewRuler overviewRuler, boolean showsAnnotationOverview,
			int styles, SourceViewerConfiguration configuration) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		sourceViewerConfiguration = configuration;
	}

	/*
	 * @see ITextOperationTarget#doOperation(int)
	 */
	@Override
	public void doOperation(int operation) {
		if (getTextWidget() == null)
			return;
		switch (operation) {
		case UNDO:
			ignoreTextConverters = true;
			break;
		case REDO:
			ignoreTextConverters = true;
			break;
		default:
			break;
		}
		super.doOperation(operation);
	}

	/**
	 * Add a new text converter for the given <code>textConverter</code>
	 * 
	 * @param textConverter
	 *            to add
	 * 
	 */
	public void addTextConverter(IAutoEditStrategy textConverter) {
		if (textConverters == null) {
			textConverters = new LinkedList<IAutoEditStrategy>();
			textConverters.add(textConverter);
		} else if (!textConverters.contains(textConverter))
			textConverters.add(textConverter);
	}

	/**
	 * Remove the given text converter
	 * 
	 * @param textConverter
	 *            to remove
	 */
	public void removeTextConverter(IAutoEditStrategy textConverter) {
		if (textConverters != null) {
			textConverters.remove(textConverter);
			if (textConverters.size() == 0)
				textConverters = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.text.TextViewer#customizeDocumentCommand(org.eclipse
	 * .jface.text.DocumentCommand)
	 */
	@Override
	protected void customizeDocumentCommand(DocumentCommand command) {
		super.customizeDocumentCommand(command);
		if (!ignoreTextConverters && textConverters != null) {
			for (IAutoEditStrategy converter : textConverters) {
				converter.customizeDocumentCommand(getDocument(), command);
			}
		}
		ignoreTextConverters = false;
	}

	/**
	 * Update indentation prefixes.
	 */
	public void updateIndentationPrefixes() {
		String[] types = sourceViewerConfiguration
				.getConfiguredContentTypes(this);
		for (int i = 0; i < types.length; i++) {
			String[] prefixes = sourceViewerConfiguration.getIndentPrefixes(
					this, types[i]);
			if (prefixes != null && prefixes.length > 0)
				setIndentPrefixes(prefixes, types[i]);
		}
	}
}