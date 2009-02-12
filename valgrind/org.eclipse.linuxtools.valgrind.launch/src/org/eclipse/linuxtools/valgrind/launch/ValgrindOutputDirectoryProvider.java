/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.launch;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;

public class ValgrindOutputDirectoryProvider implements IValgrindOutputDirectoryProvider {
	protected IPath outputPath;

	public ValgrindOutputDirectoryProvider() {
		outputPath = ValgrindLaunchPlugin.getDefault().getStateLocation();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider#getOutputPath()
	 */
	public IPath getOutputPath() throws IOException {
		createDirectory();		
		return outputPath;
	}

	protected void createDirectory() throws IOException {
		File outputDir = outputPath.toFile();
		
		if (outputDir.exists()) {
			// delete any preexisting files
			for (File outputFile : outputDir.listFiles()) {
				if (!outputFile.delete()) {
					throw new IOException(NLS.bind(Messages.getString("ValgrindOutputDirectory.Couldnt_delete"), outputFile.getAbsolutePath())); //$NON-NLS-1$
				}
			}
		}
		else if (!outputDir.mkdir()) {
			throw new IOException(NLS.bind(Messages.getString("ValgrindOutputDirectory.Couldnt_create"), outputDir.getAbsolutePath())); //$NON-NLS-1$
		}
	}
}
