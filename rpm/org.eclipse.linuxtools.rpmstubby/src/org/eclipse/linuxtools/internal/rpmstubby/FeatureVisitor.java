/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * Visitor for filtering only feature.xml files.
 *
 */
public class FeatureVisitor implements IResourceVisitor {

	private static final String FEATURE_XML = "feature.xml";
	private List<IFile> featureFiles = new ArrayList<>();

	/**
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 */
	@Override
	public boolean visit(IResource resource) {
		if (resource instanceof IFile
				&& resource.getName().equals(FEATURE_XML)) {
			featureFiles.add((IFile)resource);
		}
		return true;
	}

	/**
	 * @return All the feature.xml files found.
	 */
	public List<IFile> getFeatures() {
		return featureFiles;
	}
}