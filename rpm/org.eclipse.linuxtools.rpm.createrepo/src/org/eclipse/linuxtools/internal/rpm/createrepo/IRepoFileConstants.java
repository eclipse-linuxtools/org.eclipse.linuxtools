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
 * Valid options available in the repo file.
 */
public interface IRepoFileConstants {

    /*
     * Mandatory options
     */

    /**
     * A unique identifier for the repository.
     */
    String ID = "id";                 //$NON-NLS-1$

    /**
     * A human-readable string describing the repository.
     */
    String NAME = "name";             //$NON-NLS-1$

    /**
     * The location of the repodata folder. It can point locally (file://),
     * remotely (http://), or via ftp (ftp://).
     */
    String BASE_URL = "baseurl";     //$NON-NLS-1$

}
