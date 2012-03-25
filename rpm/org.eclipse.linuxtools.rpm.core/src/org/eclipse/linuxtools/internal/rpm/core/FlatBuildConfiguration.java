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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.rpm.core.IProjectConfiguration;

/**
 * Configuration for the flat rpm project layout.
 *
 */
public class FlatBuildConfiguration implements IProjectConfiguration {

	private IProject project;
	
	/**
	 * Creates the configuration for the given project.
	 * @param project The project whose configuration is represented.
	 */
	public FlatBuildConfiguration(IProject project) {
		this.project = project;
	}

	public IContainer getBuildFolder() {
		return project;
	}

	public IContainer getRpmsFolder() {
		return project;
	}

	public IContainer getSourcesFolder() {
		return project;
	}

	public IContainer getSpecsFolder() {
		return project;
	}

	public IContainer getSrpmsFolder() {
		return project;
	}

}
