/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
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
package org.eclipse.linuxtools.rpm.core;

/**
 * Project layouts are used to determine what settings need to be passed to
 * rpmbuild in order to make it recognize the FS layout.
 *
 */
public enum RPMProjectLayout {
    /**
     * Default rpmbuild layout with separate SOURCES, SPECS, RPMS, SRPMS and BUILD directories.
     */
    RPMBUILD,
    /**
     * Flat layout - all sources, spec file, buildroot and binary rpms are in the same directory.
     */
    FLAT;
}
