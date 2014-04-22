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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import org.eclipse.linuxtools.systemtap.structures.TreeNode;

/**
 * A base abstract class to be used for extensions of {@link TapsetParser}
 * that construct a displayable {@link TreeNode}.
 */
public abstract class TreeTapsetParser extends TapsetParser {

    protected TreeTapsetParser(String jobTitle) {
        super(jobTitle);
    }

    /**
     * @return The tree that this parser constructs.
     */
    abstract TreeNode getTree();

    /**
     * Clean up everything from the last parse run.
     */
    abstract void dispose();

}
