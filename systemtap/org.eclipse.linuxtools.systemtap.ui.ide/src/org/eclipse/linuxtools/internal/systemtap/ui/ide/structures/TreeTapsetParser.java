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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;

/**
 * A base abstract class to be used for extensions of {@link TapsetParser}
 * that construct a displayable {@link TreeNode}.
 */
public abstract class TreeTapsetParser extends TapsetParser {

    protected TreeNode tree = null;
    private TreeNode forcedTree = null;

    protected TreeTapsetParser(String jobTitle) {
        super(jobTitle);
    }

    /**
     * Prepares the parser for a run. Clients must override this method to perform
     * actions during the run; a call to super.run() is necessary.
     */
    @Override
    protected final synchronized IStatus run(IProgressMonitor monitor) {
        if (forcedTree != null) {
            tree = forcedTree;
            forcedTree = null;
            return new Status(IStatus.OK, IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
        } else {
            tree = new TreeNode(null, false);
            return runAction(monitor);
        }
    }

    protected IStatus runAction(IProgressMonitor monitor) {
        return new Status(!monitor.isCanceled() ? IStatus.OK : IStatus.CANCEL,
                IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
    }

    /**
     * @return The tree that this parser constructs.
     */
    public final synchronized TreeNode getTree() {
        return tree;
    }

    /**
     * Forcefully set this parser's tree, and subsequently fire update events
     * that normally get called when a parse operation completes.
     * @param tree The tree to put into this parser.
     */
    final synchronized void setTree(TreeNode tree) {
        String errorMessage = isValidTree(tree);
        if (errorMessage != null) {
            throw new IllegalArgumentException(errorMessage);
        }
        cancel();
        forcedTree = tree;
        schedule();
    }

    /**
     * Check if the provided tree a valid tree for this parser.
     * Called internally by {@link #setTree(TreeNode)}.
     * @param tree The tree to check for validity. 
     * @return <code>null</code> if the tree is valid; otherwise,
     * an error message signifying why the tree is invalid.
     */
    protected String isValidTree(TreeNode tree) {
        return null;
    }

}
