/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.view.histogram;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gprof.view.CallGraphContentProvider;

/**
 * Small object model, to be used by {@link CallGraphContentProvider}
 * This class is an object displayed in a tree node.
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public interface TreeElement {

    /**
     * Gets the parent of this tree node;
     * @return a tree node
     */
    TreeElement getParent();

    /**
     * Gets the children of this tree node
     * @return an array of tree nodes
     */
    LinkedList<? extends TreeElement> getChildren();

    /**
     * Checks whether this tree node has children
     * @return <code>true</code> if this tree node has children,
     * <code>false</code> otherwise.
     */
    boolean hasChildren();


    String getName();

    int getSamples();

    int getCalls();

    String getSourcePath();

    int getSourceLine();

    TreeElement getRoot();



}
