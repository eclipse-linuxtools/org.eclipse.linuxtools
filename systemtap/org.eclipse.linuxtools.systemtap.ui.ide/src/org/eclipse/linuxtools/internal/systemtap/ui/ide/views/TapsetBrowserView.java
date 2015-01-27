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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers.TapsetParser;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers.TreeTapsetParser;
import org.eclipse.swt.widgets.Composite;

/**
 * A base class for a {@link BrowserView} for displaying tapset information, which
 * relies on the results of an externally-run {@link TapsetParser}. Contents are
 * kept up-to-date with the status of the parser.
 */
public abstract class TapsetBrowserView extends BrowserView {
    /**
     * The parser that the contents of this view rely on.
     */
    private final TreeTapsetParser parser;
    private final Object lock = new Object();

    protected JobChangeAdapter viewUpdater = new JobChangeAdapter() {

        @Override
        public void aboutToRun(IJobChangeEvent event) {
            synchronized (lock) {
                displayLoadingMessage();
            }
        }

        @Override
        public void done(IJobChangeEvent event) {
            synchronized (lock) {
                if (event.getResult().isOK()) {
                    displayContents();
                } else {
                    displayCancelContents();
                }
            }
        }

    };

    /**
     * Creates a new {@link BrowserView} for displaying tapset contents, which will
     * be provided by an externally-run {@link TreeTapsetParser}.
     * @param job The parser used to obtain the tapset contents this view will display.
     */
    public TapsetBrowserView(TreeTapsetParser parser) {
        Assert.isNotNull(parser);
        this.parser = parser;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        makeActions();

        // Add listener and display initial contents based on the state of the last parser job.
        synchronized (lock) {
            IStatus result = parser.safelyAddJobChangeListener(viewUpdater);
            if (result != null) {
                if (result.isOK()) {
                    displayContents();
                } else {
                    displayCancelContents();
                }
            } else {
                displayLoadingMessage();
            }
        }
    }

    /**
     * Displays a loading message in the view and sets the view as refreshable.
     * Automatically called whenever a parse job restarts; should not be called by clients.
     */
    @Override
    protected void displayLoadingMessage() {
        super.displayLoadingMessage();
        setRefreshable(false);
    }

    /**
     * Populates the view with its contents obtained by the most recent run of {@link #parser}.
     * Automatically called whenever a parse job succeeds; should not be called by clients.
     */
    abstract protected void displayContents();

    /**
     * Clears the view and sets it as refreshable when the {@link parser} job fails or is canceled.
     * Automatically called whenever a parse job fails; should not be called by clients.
     */
    protected void displayCancelContents() {
        displayMessage(Localization.getString("BrowserView.TryRefresh")); //$NON-NLS-1$
        setRefreshable(true);
    }

    /**
     * Reruns the tapset parser to refresh the list of both probes and functions.
     */
    @Override
    protected void refresh() {
        TapsetLibrary.runStapParser();
    }

    @Override
    public void dispose() {
        super.dispose();
        parser.removeJobChangeListener(viewUpdater);
    }

}
