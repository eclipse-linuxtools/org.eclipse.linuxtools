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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.Messages;
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

    private final Object lock = new Object();

    protected TreeNode tree = new TreeNode(null, false);
    private TreeNode forcedTree = null;
    private TapsetChanges tapsetChanges = null;

    protected TreeTapsetParser(String jobTitle) {
        super(jobTitle);
    }

    /**
     * Adds a listener to this job and returns this job's latest result, which will
     * not be pre-empted by another job completing. Clients should call this instead of
     * {@link #addJobChangeListener(IJobChangeListener)} only when knowing the previous
     * job result is required for synchronization purposes (such as UI updating).
     * @param listener the listener to be added.
     * @return The result of {@link #getLatestResult()}.
     * @see #addJobChangeListener(IJobChangeListener)
     * @see #getLatestResult()
     */
    public IStatus safelyAddJobChangeListener(IJobChangeListener listener) {
        synchronized (lock) {
            super.addJobChangeListener(listener);
            return getLatestResult();
        }
    }

    /**
     * Gets the result of the last job that was started, rather than the last job
     * that was finished. This should only be used when a {@link IJobChangeListener}
     * is already registered with this job, so true job completion can be caught.
     * @return The result of this job's last run, or null if this job has either
     * never finished running or is currently running.
     * @see #getResult()
     */
    public IStatus getLatestResult() {
        synchronized (lock) {
            return getState() != Job.RUNNING ? getResult() : null;
        }
    }

    /**
     * Prepares the parser for a run. Clients must override this method to perform
     * actions during the run; a call to super.run() is necessary.
     */
    @Override
    protected final synchronized IStatus run(IProgressMonitor monitor) {
        IStatus result;
        if (forcedTree != null) {
            if (isValidTree(forcedTree)) {
                tree = forcedTree;
                result = createStatus(IStatus.OK);
            } else {
                result = createStatus(IStatus.ERROR, Messages.TapsetParser_ErrorInvalidTapsetTree);
            }
            forcedTree = null;
        } else if (tapsetChanges != null) {
            result = performUpdate(monitor);
        } else {
            tree = new TreeNode(null, false);
            result = createStatus(runAction(monitor));
        }
        synchronized (lock) {
            return result;
        }
    }

    /**
     * Loads the tapset contents and saves them.
     * @param monitor The progress monitor for the operation.
     * @return An {@link IStatus} severity level for the result of the operation.
     */
    protected abstract int runAction(IProgressMonitor monitor);

    /**
     * After adding / removing tapsets, schedule the job that
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
                    result = addTapsets(tapsetContents, tapsetChanges.additions, monitor);
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
     * @param tapsetContents the set of tapset contents as generated by {@link SharedParser#getTapsetContents()}.
     * Included as a parameter to have the caller manage the tapset instead of the callee.
     * @param additions A non-empty list of added tapset directories.
     * @param monitor The progress monitor for the operation.
     * @return An {@link IStatus} severity level for the result of the operation.
     */
    protected abstract int addTapsets(String tapsetContents, String[] additions, IProgressMonitor monitor);

    /**
     * @return The tree that this parser constructs. Guaranteed not be null.
     */
    public final TreeNode getTree() {
        return tree;
    }

    /**
     * Forcefully set this parser's tree, and subsequently fire update events
     * that normally get called when a parse operation completes.
     * @param tree The tree to put into this parser.
     */
    public final void setTree(TreeNode tree) {
        cancel();
        forcedTree = tree;
        schedule();
    }

    /**
     * Check if the provided tree a valid tree for this parser.
     * Called internally by {@link #setTree(TreeNode)}.
     * @param tree The tree to check for validity.
     * @return <code>true</code> if the tree is valid, <code>false</code> otherwise.
     */
    protected boolean isValidTree(TreeNode tree) {
        return tree != null;
    }

}
