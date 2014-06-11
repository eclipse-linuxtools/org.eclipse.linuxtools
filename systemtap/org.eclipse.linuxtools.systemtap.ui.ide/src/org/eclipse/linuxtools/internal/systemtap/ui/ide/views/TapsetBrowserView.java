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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetParser;
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
    private final TapsetParser parser;

    protected JobChangeAdapter viewUpdater = new JobChangeAdapter() {

        @Override
        public void aboutToRun(IJobChangeEvent event) {
            displayLoadingMessage();
        }

        @Override
        public void done(IJobChangeEvent event) {
            if (event.getResult().isOK()) {
                displayContents();
            } else {
                setViewerInput(null);
                setRefreshable(true);
            }
        }

    };

    /**
     * Create a new {@link BrowserView} for displaying tapset contents, which will
     * be provided by an externally-run {@link TapsetParser}.
     * @param job The parser used to obtain the tapset contents this view will display.
     */
    public TapsetBrowserView(TapsetParser parser) {
        Assert.isNotNull(parser);
        this.parser = parser;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        IStatus result = parser.getResult();
        if (result != null && result.isOK()) {
            displayContents();
        } else {
            displayLoadingMessage();
        }

        parser.addJobChangeListener(viewUpdater);
        makeActions();
    }

    @Override
    protected void displayLoadingMessage() {
        super.displayLoadingMessage();
        setRefreshable(false);
    }

    /**
     * Populates the view with its contents obtained by the most recent run of {@link #parser}.
     */
    abstract protected void displayContents();

    /**
     * Rerun the tapset parser to refresh the list of both probes and functions.
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
