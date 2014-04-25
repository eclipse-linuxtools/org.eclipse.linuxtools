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
package org.eclipse.linuxtools.changelog.core;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * @author klee (Kyu Lee)
 */
public interface IEditorChangeLogContrib {

    /**
     * Set TextEditor that this configuration is going to be used.
     *
     * @param editor The text editor for this configuration.
     */
    void setTextEditor(TextEditor editor);

    /**
     * Set default content type. GNU Changelog only has one type.
     *
     * @return default content type.
     */
    String[] getConfiguredContentTypes(ISourceViewer sourceViewer);

    /**
     * Detects hyperlinks in GNU formatted changelogs.
     *
     * @return link detector for GNU format.
     */
    IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer);

    /**
     * Hyperlink presenter (decorator).
     *
     * @return default presenter.
     */
    IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer);

    /**
     * Highlights GNU format changelog syntaxes.
     *
     * @return reconciler for GNU format changelog.
     */
    IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer);
}
