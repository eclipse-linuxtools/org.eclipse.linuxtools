/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.ui;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;

public class HistoryAction extends Action {
	protected String toolID;
	protected File dir;
	public HistoryAction(String executable, String toolID, String datadir) {
		super();
		this.toolID = toolID;
		String toolName = ValgrindLaunchPlugin.getDefault().getToolName(toolID);
		setText(executable + " [" + toolName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		dir = new File(ValgrindCommand.DATA_PATH + File.separator + datadir);
	}
	
	@Override
	public void run() {
		if (dir.exists()) {
			try {
				IValgrindLaunchDelegate delegate = ValgrindLaunchPlugin.getDefault().getToolDelegate(toolID);
				delegate.reparseOutput(dir);
			} catch (CoreException e) {
				e.printStackTrace();
			}			
		}
	}
}
