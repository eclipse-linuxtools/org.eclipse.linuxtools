/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IFolder;

/**
 * Represents an RPM configuration associated with an RPM project.
 * An RPM configuration contains information about RPM folder locations 
 * and other RPM macro definitions.
 * TODO make this per project settings.
 */
public interface IRPMConfiguration {

	/**
	 * Returns the workspace folder containing RPM build artifacts.
	 * @return the build folder
	 */
	public IFolder getBuildFolder();

	/**
	 * Returns the workspace folder containing binary RPMs.
	 * @return the RPMs folder
	 */
	public IFolder getRpmsFolder();

	/**
	 * Returns the workspace folder containing RPM sources artifacts.
	 * @return the sources folder
	 */
	public IFolder getSourcesFolder();

	/**
	 * Returns the workspace folder containing RPM spec files.
	 * @return the spec files folder
	 */
	public IFolder getSpecsFolder();

	/**
	 * Returns the workspace folder containing source RPMs.
	 * @return the source RPMs folder
	 */
	public IFolder getSrpmsFolder();

}
