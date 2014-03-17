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
package org.eclipse.linuxtools.internal.rpm.createrepo;

/**
 * Common constants used in createrepo.
 */
public interface ICreaterepoConstants {

	/**
	 * The folder which contains the repodata folder as well as the
	 * RPMs.
	 */
	String CONTENT_FOLDER = "content"; //$NON-NLS-1$

	/**
	 * The file extension of RPM files.
	 */
	String REPO_FILE_EXTENSION = "repo"; //$NON-NLS-1$

	/**
	 * The delimiter of preferences.
	 */
	String DELIMITER = ";"; //$NON-NLS-1$

	/**
	 * The file extension of RPM files.
	 */
	String RPM_FILE_EXTENSION = "rpm"; 	//$NON-NLS-1$

	/**
	 * An empty string.
	 */
	String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * The main createrepo command name.
	 */
	String CREATEREPO_COMMAND = "createrepo"; //$NON-NLS-1$

	/**
	 * Dashes used for commands.
	 */
	String DASH = "--"; //$NON-NLS-1$

}
