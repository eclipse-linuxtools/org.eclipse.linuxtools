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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
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
public class DownloadJob extends Job {
	private IFile file;
	private URLConnection content;
	private boolean fileOverride;

	/**
	 * Creates the download job.
	 * @param file The file to store the remote content.
	 * @param content The URLConnection to the remote file.
	 * @param override Flag to override file if it exists.
	 */
	public DownloadJob(IFile file, URLConnection content, boolean override) {
		super(NLS.bind(Messages.DownloadJob_0, file.getName()));
		this.file = file;
		this.content = content;
		this.fileOverride = override;
	}

	/**
	 * Creates the download job.
	 * @param file The file to store the remote content.
	 * @param content URLConnection to the remote file.
	 */
	public DownloadJob(IFile file, URLConnection content) {
		this(file, content, false);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(
				NLS.bind(Messages.DownloadJob_0,
						file.getName()), content.getContentLength());
		try {
			File tempFile = File.createTempFile(file.getName(), ""); //$NON-NLS-1$
			FileOutputStream fos = new FileOutputStream(tempFile);
			InputStream is = new BufferedInputStream(content.getInputStream());
			int b;
			boolean canceled = false;
			while ((b = is.read()) != -1) {
				if (monitor.isCanceled()) {
					canceled = true;
					break;
				}
				fos.write(b);
				monitor.worked(1);
			}
			is.close();
			fos.close();
			if (!canceled) {
				if (fileOverride) {
					file.setContents(new FileInputStream(tempFile), true,
							false, monitor);
				} else {
					file.create(new FileInputStream(tempFile), true, monitor);

				}
			}
			tempFile.delete();
		} catch (CoreException e) {
			Platform.getLog(Platform.getBundle(IRPMConstants.RPM_CORE_ID)).log(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID, e.getMessage(), e));
			return Status.CANCEL_STATUS;
		} catch (IOException e) {
			Platform.getLog(Platform.getBundle(IRPMConstants.RPM_CORE_ID)).log(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID, e.getMessage(), e));
			return Status.CANCEL_STATUS;
		}
		monitor.done();
		return Status.OK_STATUS;
	}
}