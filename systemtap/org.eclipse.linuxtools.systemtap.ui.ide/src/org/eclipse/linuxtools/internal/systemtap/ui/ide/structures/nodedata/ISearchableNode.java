/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat, Inc.
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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata;

/**
 * An interface for a data object that stores a <code>String</code> token
 * that should be searched for in a text file.
 */
public interface ISearchableNode {
    /**
     * @return The token to search a text file for.
     */
    String getSearchToken();

    /**
     * @return <code>true</code> if {@link #getSearchToken()} is a regular expression,
     * or <code>false</code> if it is a plain-text search token.
     */
    boolean isRegexSearch();
}
