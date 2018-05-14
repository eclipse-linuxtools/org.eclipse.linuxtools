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
 * Valid compression types that createrepo command uses.
 */
public interface ICreaterepoCompressionTypes {

    /**
     * Default compression type.
     */
    String COMPAT = "compat"; //$NON-NLS-1$

    /**
     * May not be available.
     */
    String XZ = "xz"; //$NON-NLS-1$

    /**
     * GZ.
     */
    String GZ = "gz"; //$NON-NLS-1$

    /**
     * BZ2.
     */
    String BZ2 = "bz2"; //$NON-NLS-1$

}
