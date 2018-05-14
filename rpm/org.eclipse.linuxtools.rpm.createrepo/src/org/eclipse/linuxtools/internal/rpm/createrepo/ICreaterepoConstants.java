/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
    String RPM_FILE_EXTENSION = "rpm";     //$NON-NLS-1$

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
