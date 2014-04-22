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
