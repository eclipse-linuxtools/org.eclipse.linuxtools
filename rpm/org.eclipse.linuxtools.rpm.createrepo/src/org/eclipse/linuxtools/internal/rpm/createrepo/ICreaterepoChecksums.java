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
 * Valid checksums that createrepo command uses.
 */
public interface ICreaterepoChecksums {

    /**
     * Default checksum.
     */
    String SHA256 = "sha256"; //$NON-NLS-1$

    /**
     * Old default. Older versions of yum (3.0.x) not supported.
     */
    String SHA1 = "sha1"; //$NON-NLS-1$

    /**
     * MD5.
     */
    String MD5 = "md5"; //$NON-NLS-1$

    /**
     * SHA512.
     */
    String SHA512 = "sha512"; //$NON-NLS-1$

}
