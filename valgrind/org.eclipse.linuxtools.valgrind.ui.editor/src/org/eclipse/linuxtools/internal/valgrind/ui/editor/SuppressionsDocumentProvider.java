/*******************************************************************************
 * Copyright (c) 2008, 2018 Phil Muldoon <pkmuldoon@picobot.org>.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API.
 *    Red Hat - modifications for use with Valgrind plugins.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class SuppressionsDocumentProvider extends TextFileDocumentProvider {

    @Override
    public IDocument getDocument(Object element) {
        IDocument document = super.getDocument(element);
        if (document != null) {
            FastPartitioner partitioner = new FastPartitioner(
                    new RuleBasedPartitionScanner(),
                SuppressionsPartitionScanner.SUPP_CONTENT_TYPES);

            partitioner.connect(document, false);
            if (document.getDocumentPartitioner() == null) {
                document.setDocumentPartitioner(partitioner);
            }
        }
        return document;

    }

}
