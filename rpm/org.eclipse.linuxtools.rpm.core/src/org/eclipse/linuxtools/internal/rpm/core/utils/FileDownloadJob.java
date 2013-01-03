/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Alexander Kurtakov (Red Hat) - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.osgi.util.NLS;

/**
 * Eclipse job to ease downloading remote files.
 *
 */
public class FileDownloadJob extends Job {
	private File file;
	private URLConnection content;

	/**
	 * Creates the download job.
	 * @param file The file to store the remote content.
	 * @param content The URLConnection to the remote file.
	 */
	public FileDownloadJob(File file, URLConnection content) {
		super(NLS.bind(Messages.DownloadJob_0, file.getName()));
		this.file = file;
		this.content = content;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(
				NLS.bind(Messages.DownloadJob_0,
						file.getName()), content.getContentLength());
		try {
			FileOutputStream fos = new FileOutputStream(file);
			InputStream is = new BufferedInputStream(content.getInputStream());
			int b;
			while ((b = is.read()) != -1) {
				if (monitor.isCanceled()) {
					break;
				}
				fos.write(b);
				monitor.worked(1);
			}
			is.close();
			fos.close();
		} catch (IOException e) {
			Platform.getLog(Platform.getBundle(IRPMConstants.RPM_CORE_ID)).log(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID, e.getMessage(), e));
			return Status.CANCEL_STATUS;
		}
		monitor.done();
		return Status.OK_STATUS;
	}
}