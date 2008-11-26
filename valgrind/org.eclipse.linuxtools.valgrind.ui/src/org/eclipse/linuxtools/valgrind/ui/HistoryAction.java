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
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.valgrind.core.HistoryEntry;
import org.eclipse.linuxtools.valgrind.core.HistoryFile;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;

public class HistoryAction extends Action {
	protected HistoryEntry entry;
	protected File dir;
	public HistoryAction(HistoryEntry entry) {
		super("", IAction.AS_RADIO_BUTTON); //$NON-NLS-1$
		this.entry = entry;
		String toolName = ValgrindLaunchPlugin.getDefault().getToolName(entry.getTool());
		setText(entry.getConfigName() + " [" + toolName + "] " + entry.getProcessLabel()); //$NON-NLS-1$ //$NON-NLS-2$
		dir = new File(ValgrindCommand.DATA_PATH + File.separator + entry.getDatadir());
	}
	
	@Override
	public void run() {
		if (dir.exists()) {
			try {
				IValgrindLaunchDelegate delegate = ValgrindLaunchPlugin.getDefault().getToolDelegate(entry.getTool());
				delegate.reparseOutput(dir);
				HistoryFile.getInstance().moveToEnd(entry);
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
}
