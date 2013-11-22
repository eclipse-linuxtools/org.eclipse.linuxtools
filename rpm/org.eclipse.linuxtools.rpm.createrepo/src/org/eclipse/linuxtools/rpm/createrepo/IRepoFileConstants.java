/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.createrepo;

/**
 * Valid options available in the repo file.
 */
public interface IRepoFileConstants {

	/*
	 * Mandatory options
	 */

	/**
	 * A unique identifier for the repository.
	 */
	String ID = "id"; 				//$NON-NLS-1$

	/**
	 * A human-readable string describing the repository.
	 */
	String NAME = "name"; 			//$NON-NLS-1$

	/**
	 * The location of the repodata folder. It can point locally (file://),
	 * remotely (http://), or via ftp (ftp://).
	 */
	String BASE_URL = "baseurl"; 	//$NON-NLS-1$

}
