/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata;

import java.util.List;

/**
 * An interface for a data object associated with a list of data types.
 */
public interface IMultiTypedNode {
    /**
     * @return A list of the names of all of this object's associated data types.
     */
    List<String> getTypes();
}
