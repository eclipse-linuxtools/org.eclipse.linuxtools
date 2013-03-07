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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;

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
				PerfPlugin.getDefault().getProfileView()
						.setContentDescription(newDataLoc.toOSString());

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

	private void copyFile(File src, File dest) {
		InputStream destInput = null;
		OutputStream srcOutput = null;
		try {
			destInput = new FileInputStream(src);
			srcOutput = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = destInput.read(buffer)) != -1) {
				srcOutput.write(buffer, 0, length);
			}
		} catch (FileNotFoundException e) {
			openErroDialog(Messages.PerfSaveSession_failure_title,
					Messages.PerfSaveSession_failure_msg,
					dest.toString());
		} catch (IOException e) {
			openErroDialog(Messages.PerfSaveSession_failure_title,
					Messages.PerfSaveSession_failure_msg,
					dest.toString());
		} finally {
			closeResource(destInput, dest.getName());
			closeResource(srcOutput, src.getName());
		}

	}
}
