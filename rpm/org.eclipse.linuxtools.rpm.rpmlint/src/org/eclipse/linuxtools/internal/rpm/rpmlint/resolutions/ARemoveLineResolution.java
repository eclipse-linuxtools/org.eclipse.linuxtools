/*******************************************************************************
 * Copyright (c) 2007, 2018 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.internal.rpm.rpmlint.RpmlintLog;
import org.eclipse.ui.IEditorPart;

/**
 * Defines the common functionallity for resolution which fix is to remove a
 * line.
 */
public abstract class ARemoveLineResolution extends ARpmlintResolution {

	@Override
	public void run(IMarker marker) {

		IEditorPart editor = getEditor(marker);
		if (editor == null) {
			return;
		}
		// Get the document
		IDocument doc = editor.getAdapter(IDocument.class);

		try {
			int index = doc.getLineOffset(marker.getAttribute(IMarker.LINE_NUMBER, 0));
			int lineLength = doc.getLineLength(marker.getAttribute(IMarker.LINE_NUMBER, 0));
			doc.replace(index, lineLength, ""); //$NON-NLS-1$
		} catch (BadLocationException e) {
			RpmlintLog.logError(e);
		}
	}

}
