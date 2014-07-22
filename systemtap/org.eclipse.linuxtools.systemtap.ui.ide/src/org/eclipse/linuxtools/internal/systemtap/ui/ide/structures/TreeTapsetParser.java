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
import org.eclipse.linuxtools.systemtap.structures.TreeNode;

/**
 * A base abstract class to be used for extensions of {@link TapsetParser}
 * that construct a displayable {@link TreeNode}.
 */
public abstract class TreeTapsetParser extends TapsetParser {

    private class TapsetChanges {
        private String[] additions;
        private String[] deletions;
        private TapsetChanges(String[] additions, String[] deletions) {
            this.additions = new String[additions.length];
            this.deletions = new String[deletions.length];
            System.arraycopy(additions, 0, this.additions, 0, additions.length);
            System.arraycopy(deletions, 0, this.deletions, 0, deletions.length);
        }
    }

    protected TreeNode tree = null;
    private TreeNode forcedTree = null;
    private TapsetChanges tapsetChanges = null;

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
            tapsetChanges = null;
            tree = forcedTree;
            forcedTree = null;
            return createStatus(IStatus.OK);
        }
        if (tapsetChanges != null) {
            return performUpdate(monitor);
        }
        tree = new TreeNode(null, false);
        return createStatus(runAction(monitor));
    }

    /**
     * Loads the tapset contents and saves them.
     * @param monitor The progress monitor for the operation.
     * @return An {@link IStatus} severity level for the result of the operation.
     */
    protected abstract int runAction(IProgressMonitor monitor);

    /**
     * After adding / removing tapsets, scheduled the job that
     * loads in / discards the tapsets that were added / removed.
     * @param additions The list of added tapset directories.
     * @param deletions The list of removed tapset directories.
     */
    public synchronized void runUpdate(String[] additions, String[] deletions) {
        tapsetChanges = new TapsetChanges(additions, deletions);
        schedule();
    }

    /**
     * Performs both stages of a tapset update operation, ensuring that the new
     * tapset contents will be available when they are necessary.
     * @param monitor The operation's progress monitor.
     * @return The status of the operation's outcome.
     */
    private IStatus performUpdate(IProgressMonitor monitor) {
        int result = IStatus.OK;
        if (tapsetChanges.deletions.length > 0) {
            result = delTapsets(tapsetChanges.deletions, monitor);
        }
        if (result == IStatus.OK && tapsetChanges.additions.length > 0) {
            if (monitor.isCanceled()) {
                result = IStatus.CANCEL;
            } else {
                String tapsetContents = SharedParser.getInstance().getTapsetContents();
                result = verifyRunResult(tapsetContents);
                if (result == IStatus.OK) {
                    result = addTapsets(tapsetChanges.additions, monitor);
                }
            }
        }
        tapsetChanges = null;
        return createStatus(result);
    }

    /**
     * After changing the list of imported tapsets, discards the tapsets that were removed.
     * @param deletions A non-empty list of removed tapset directories.
     * @param monitor The progress monitor for the operation.
     * @return An {@link IStatus} severity level for the result of the operation.
     */
    protected abstract int delTapsets(String[] deletions, IProgressMonitor monitor);

    /**
     * After changing the list of imported tapsets, loads in the tapsets that were added.
     * Tapset contents are guaranteed to be loaded at the time this method is called.
     * @param additions A non-empty list of added tapset directories.
     * @param monitor The progress monitor for the operation.
     * @return An {@link IStatus} severity level for the result of the operation.
     */
    protected abstract int addTapsets(String[] additions, IProgressMonitor monitor);

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
