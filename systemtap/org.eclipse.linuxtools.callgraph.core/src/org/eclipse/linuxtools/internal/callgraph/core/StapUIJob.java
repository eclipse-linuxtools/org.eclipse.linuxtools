/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.callgraph.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

/**
 * Initializes and runs a StapGraph and TreeViewer within the SystemTap View
 *
 * @author chwang
 *
 */
public class StapUIJob extends UIJob {
	private SystemTapParser parser;
	private String viewID;
	private SystemTapView viewer;

	public StapUIJob(String name, SystemTapParser parser, String viewID) {
		super(name);
		// CREATE THE SHELL
		this.parser = parser;
		this.viewID = viewID;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (parser.getSecondaryID() != null && parser.getSecondaryID().length() > 0) {
			viewer = ViewFactory.createView(viewID, parser.getSecondaryID());
		} else {
			viewer = ViewFactory.createView(viewID);
		}
		if (!viewer.setParser(parser)) {
			return Status.CANCEL_STATUS;
		}
		if (viewer.initializeView(this.getDisplay(), monitor) == Status.CANCEL_STATUS) {
			return Status.CANCEL_STATUS;
		}

		if (!parser.realTime) {
			viewer.updateMethod();
		}
		viewer.setSourcePath(parser.getFile());
		viewer.setKillButtonEnabled(true);

		return Status.OK_STATUS;
	}

	/**
	 * Returns the viewer object. Viewer is initialized within the run method, and
	 * is not guaranteed to be non-null until the job has terminated.
	 * @return
	 */
	public SystemTapView getViewer() {
		return viewer;
	}
}
