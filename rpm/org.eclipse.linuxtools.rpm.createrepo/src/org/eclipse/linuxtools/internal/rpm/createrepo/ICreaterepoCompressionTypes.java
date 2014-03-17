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
