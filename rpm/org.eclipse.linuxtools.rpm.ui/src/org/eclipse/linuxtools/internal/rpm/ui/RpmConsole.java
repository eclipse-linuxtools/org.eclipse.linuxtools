/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.ui.console.IOConsole;

/**
 * RpmConsole is used to output rpm/rpmbuild output.
 * 
 */
public class RpmConsole extends IOConsole {

	/** Id of this console. */
	public static final String ID = "rpmbuild"; //$NON-NLS-1$
	private RPMProject rpmProject;

	/**
	 * Creates the console.
	 * 
	 * @param rpmProject
	 *            The RPM project to use.
	 */
	public RpmConsole(RPMProject rpmProject) {
		super(ID+'('+rpmProject.getSpecFile().getProject().getName()+')', ID, null, true);
		this.rpmProject = rpmProject;
	}

	/**
	 * Returns the spec file for this rpm project.
	 * 
	 * @return The spec file.
	 */
	public IResource getSpecfile() {
		return rpmProject.getSpecFile();
	}
}
