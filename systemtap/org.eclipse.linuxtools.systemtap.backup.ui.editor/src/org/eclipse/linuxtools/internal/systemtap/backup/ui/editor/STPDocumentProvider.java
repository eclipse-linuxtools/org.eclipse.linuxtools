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

package org.eclipse.linuxtools.internal.systemtap.backup.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class STPDocumentProvider extends TextFileDocumentProvider {
	private IDocument document;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#getDocument(java.lang.Object)
	 */
	@Override
	public IDocument getDocument(Object element) {
		document = super.getDocument(element);
		if (document != null) {
			STPPartitioner partitioner = new STPPartitioner(
					new STPPartitionScanner(),
					STPPartitionScanner.STP_PARTITION_TYPES);

			partitioner.connect(document, false);
			if (document.getDocumentPartitioner() == null)
				document.setDocumentPartitioner(partitioner);
		}
		return document;
	}

}