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
package org.eclipse.linuxtools.rpm.rpmlint.builder;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.rpmlint.parser.RpmlintParser;

public class RpmlintDeltaVisitor implements IResourceDeltaVisitor {

	private ArrayList paths = new ArrayList();

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		if (delta.getResource() instanceof IFile
				&& delta.getResource().getName().endsWith(".spec")) {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			// we first visiting resources to be able to run the rpmlint command
			// only once. That improve drastically the performance.
			case IResourceDelta.ADDED:
				paths.add(resource.getLocation().toOSString());
				break;
			case IResourceDelta.CHANGED:
				RpmlintParser.getInstance().deleteMarkers(resource);
				paths.add(resource.getLocation().toOSString());
				break;
			}
		}
		return true;
	}
	
	public ArrayList getVisitedPaths() {
		return paths;
	}

}
