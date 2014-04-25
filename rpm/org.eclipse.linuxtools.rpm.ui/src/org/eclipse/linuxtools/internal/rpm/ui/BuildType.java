/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
