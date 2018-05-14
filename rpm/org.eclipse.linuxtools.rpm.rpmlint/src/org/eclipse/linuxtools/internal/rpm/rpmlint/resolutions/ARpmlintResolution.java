/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.linuxtools.internal.rpm.rpmlint.RpmlintLog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Base class providing common functionality for rpmlint resolution.
 *
 */
public abstract class ARpmlintResolution implements IMarkerResolution2 {

	/**
	 * No image for rpmlint resolutions for now.
	 *
	 * @see org.eclipse.ui.IMarkerResolution2#getImage()
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/**
	 * Returns the SpecfileEditor for the given IMarker if any.
	 *
	 * @param marker The marker to use for retrieving the editor.
	 * @return The IEditorPart this marker is from or null.
	 */
	protected IEditorPart getEditor(IMarker marker) {
		// Open or activate the editor.
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart part;
		try {
			part = IDE.openEditor(page, marker);
		} catch (PartInitException e) {
			RpmlintLog.logError(e);
			return null;
		}
		return part;
	}
}
