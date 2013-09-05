/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Visitor keeping a list of spec files only.
 *
 */
public class SpecfileVisitor implements IResourceVisitor {

	private List<IResource> paths = new ArrayList<IResource>();

	public boolean visit(IResource resource) throws CoreException {
		if (resource.getType() == IResource.FILE
				&& resource.getFileExtension() != null
				&& resource.getFileExtension().equals("spec")) { //$NON-NLS-1$
			paths.add(resource);
		}
		return true;
	}

	/**
	 * Returns a list of all spec files found.
	 * 
	 * @return The found spec files.
	 */
	public List<IResource> getSpecFiles() {
		return paths;
	}
}
