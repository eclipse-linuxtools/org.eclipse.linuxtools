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
package org.eclipse.linuxtools.valgrind.history;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.valgrind.core.utils.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.ui.XMLMemento;

public class HistoryAction extends Action {
	protected HistoryEntry entry;

	public HistoryAction(HistoryEntry entry) {
		super("", IAction.AS_RADIO_BUTTON); //$NON-NLS-1$
		this.entry = entry;
		setText(entry.getDescription());
	}

	@Override
	public void run() {
		try {
			restoreState();
			HistoryFile.getInstance().moveToEnd(entry);
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	protected void restoreState() throws CoreException, IOException {
		XMLMemento memento = entry.getMemento();
		String configMemento = memento.getString(MementoConstants.ELEMENT_CONFIG);
		
		ILaunchConfiguration config = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(configMemento);
		String tool = config.getAttribute(LaunchConfigurationConstants.ATTR_TOOL, (String) null);
		
		// create view
		ValgrindUIPlugin.getDefault().createView(entry.getDescription(), tool);
		
		// restore contents
		IValgrindPersistable persistable = ValgrindHistoryPlugin.getDefault().getPersistable(tool);
		if (persistable != null) {
			persistable.restoreState(memento);
		}
		
		// refresh view
		ValgrindUIPlugin.getDefault().refreshView();
	}
}
