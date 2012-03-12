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
package org.eclipse.linuxtools.internal.rpm.rpmlint.builder;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.internal.rpm.rpmlint.parser.RpmlintParser;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;

/**
 * Removes all the markers created by rpmlint.
 *
 */
public class RpmlintMarkerRemoveVisitor implements IResourceVisitor {

	/**
	 * Removes all rpmlint markers for spec and rpm files.
	 * 
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 */
	public boolean visit(IResource resource) throws CoreException {
		if (Activator.SPECFILE_EXTENSION.equals(resource.getFileExtension())
				|| Activator.RPMFILE_EXTENSION.equals(resource
						.getFileExtension())) {
			RpmlintParser.getInstance().deleteMarkers(resource);
			// remove internal marks
			resource.deleteMarkers(
					SpecfileErrorHandler.SPECFILE_ERROR_MARKER_ID, false,
					IResource.DEPTH_ZERO);
		}
		return true;
	}
}
