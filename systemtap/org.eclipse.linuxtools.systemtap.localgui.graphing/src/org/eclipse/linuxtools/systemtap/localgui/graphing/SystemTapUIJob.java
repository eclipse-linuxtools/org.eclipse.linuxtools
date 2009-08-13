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

package org.eclipse.linuxtools.systemtap.localgui.graphing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

/**
 *	Responsible for setting focus for the SystemTap View, writing to the view.
 *	Similar to a job and is used so that writing to the view does not hang up
 *	any other processes.
 */
public class SystemTapUIJob extends UIJob {
	private SystemTapView stapview;
	private SystemTapCommandParser stapcmd;
	private static boolean firstRun;
	private boolean useColours;

	public SystemTapUIJob(String name, SystemTapCommandParser cmd, boolean useColours) {
		super(name);
		stapcmd = cmd;
		stapview = cmd.stapview;
		this.useColours = useColours;
		firstRun = true;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (firstRun) {
			firstRun = false;
			SystemTapView.forceDisplay();
			stapview.clearAll();
			SystemTapView.disposeGraph();
		}

		String temp = stapcmd.getText();
		if (temp.length() > 0) {
			if (useColours)
				stapview.prettyPrintln(temp);
			else
				stapview.println(temp);
		}
		stapcmd.setDone();
		
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

}
