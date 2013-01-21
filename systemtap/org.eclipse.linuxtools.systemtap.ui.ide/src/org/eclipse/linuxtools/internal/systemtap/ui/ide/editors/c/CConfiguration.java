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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.linuxtools.systemtap.ui.editor.DoubleClickStrategy;
import org.eclipse.linuxtools.systemtap.ui.editor.NonRuleBasedDamagerRepairer;
import org.eclipse.swt.graphics.RGB;



/**
 * Configures an instance of <code>CEditor</code>. This class is responsible for starting
 * the Syntax highlighting system.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class CConfiguration extends SourceViewerConfiguration {
	private DoubleClickStrategy doubleClickStrategy;
	private CScanner scanner;
	private ColorManager colorManager;

	/**
	 * The constructor for the <code>CConfiguration</code> class. Takes as its only parameter
	 * the ColorManager to use for syntax highlighting.
	 * @param colorManager	the <code>ColorManager</code> to use for text highlighting
	 */
	public CConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			CPartitionScanner.C_COMMENT};
	}
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		if (doubleClickStrategy == null) {
			doubleClickStrategy = new DoubleClickStrategy();
		}
		return doubleClickStrategy;
	}

	/**
	 * An accessor method for the <code>CScanner</code> associated with this editor. This method is used
	 * in order to dispatch notifications to the <code>CScanner</code> when the color preferences have
	 * been changed. The <code>CEditor</code> class calls this method to get the <code>CScanner</code>
	 * associated with it, and then it reinitializes that <code>CScanner</code> using the
	 * <code>CScanner.initializeScanner</code> method.
	 * @return	the instance of the CScanner associated with this instance
	 */
	protected CScanner getCScanner() {
		if (scanner == null) {
			scanner = new CScanner(colorManager);
		}
		return scanner;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();
		RGB comment = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_C_COMMENT_COLOR);
		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(colorManager.getColor(comment)));
		reconciler.setDamager(ndr, CPartitionScanner.C_COMMENT);
		reconciler.setRepairer(ndr, CPartitionScanner.C_COMMENT);

		return reconciler;
	}
}
