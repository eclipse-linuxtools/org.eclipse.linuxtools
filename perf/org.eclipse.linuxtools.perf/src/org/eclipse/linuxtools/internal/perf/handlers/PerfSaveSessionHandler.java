/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.handlers;

import java.io.File;
import java.io.IOException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.ui.PerfProfileView;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for saving a perf profile session.
 */
public class PerfSaveSessionHandler extends AbstractSaveDataHandler {

	public static final String DATA_EXT = "data"; //$NON-NLS-1$

	@Override
	public File saveData(String filename) {
		// get paths
		IPath newDataLoc = getNewDataLocation(filename, DATA_EXT);
		IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();

		// get files
		File newDataFile = new File(newDataLoc.toOSString());
		File defaultDataFile = defaultDataLoc.toFile();

		if (canSave(newDataFile)) {
			// copy default data into new location
			try {
				newDataFile.createNewFile();
				copyFile(defaultDataFile, newDataFile);
				PerfPlugin.getDefault().setPerfProfileData(newDataLoc);
				try {
					PerfProfileView view = (PerfProfileView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(PerfPlugin.VIEW_ID);
					view.setContentDescription(newDataLoc.toOSString());
				} catch (PartInitException e) {
					// fail silently
				}

				return newDataFile;
			} catch (IOException e) {
				openErroDialog(Messages.PerfSaveSession_failure_title,
						Messages.PerfSaveSession_failure_msg,
						newDataLoc.lastSegment());
			}
		}
		return null;

	}

	@Override
	public boolean verifyData() {
		IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();
		return defaultDataLoc != null && !defaultDataLoc.isEmpty();
	}
}
