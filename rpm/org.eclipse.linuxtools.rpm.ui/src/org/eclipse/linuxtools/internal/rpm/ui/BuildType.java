/*******************************************************************************
 * Copyright (c) 2005, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui;

/**
 * The different types of builds.
 */

public enum BuildType {
	/**
	 * Default.
	 */
	NONE,
	/**
	 * Everything - rpmbuild -ba.
	 */
	ALL,
	/**
	 * Binary - rpmbuild -bb.
	 */
	BINARY,
	/**
	 * Source RPM - rpmbuild -bs.
	 */
	SOURCE
}
