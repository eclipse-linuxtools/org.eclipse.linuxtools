/*******************************************************************************
 * Copyright (c) 2010, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov (Red Hat) - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.utils.DownloadJob;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

public class SourcesFileDownloadHyperlink implements IHyperlink {
	private String fileName;
	private IFile original;
	private IRegion region;

	/**
	 * Creates hyperlink for the following file name, region and file whether
	 * the file name is found.
	 *
	 * @param original
	 *            The file where the reference to this file name is.
	 * @param fileName
	 *            The name of the file to open.
	 * @param region
	 *            The hyperlink region.
	 */
	public SourcesFileDownloadHyperlink(IFile original, String fileName,
			IRegion region) {
		this.fileName = fileName;
		this.original = original;
		this.region = region;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
	 */
	public IRegion getHyperlinkRegion() {
		return region;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
	 */
	public String getHyperlinkText() {
		return Messages.SourcesFileHyperlink_1 + ' ' + fileName;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel() {
		return null;
	}

	/**
	 * Tries to open the given file name looking for it in the current directory
	 * and in ../SOURCES.
	 *
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
	 */
	public void open() {
		IContainer container = original.getParent();
		IResource saveFolder = container.getParent().findMember(
				IRPMConstants.SOURCES_FOLDER);
		if (saveFolder == null) {
			saveFolder = container.findMember(original.getFullPath()
					.removeLastSegments(1));
		}
		if (saveFolder == null) {
			saveFolder = container.findMember("/"); //$NON-NLS-1$
		}
		try {
			URL url = new URL(fileName);
			URLConnection connection = url.openConnection();
			String savedFileName = fileName
					.substring(fileName.lastIndexOf('/') + 1);
			IFile savedFile = original.getProject().getFile(
					saveFolder.getProjectRelativePath().append(savedFileName));
			if (savedFile.exists()) {
				MessageBox mb = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.ICON_QUESTION | SWT.OK
						| SWT.CANCEL);
				mb.setText(Messages.SourcesFileDownloadHyperlink_0);
				mb.setMessage(NLS.bind(Messages.SourcesFileDownloadHyperlink_1,
						savedFileName));
				int rc = mb.open();
				if (rc == SWT.OK) {
					new DownloadJob(savedFile, connection).schedule();
				}

			} else {
				new DownloadJob(savedFile, connection).schedule();
			}
		} catch (IOException e) {
			MessageBox mb = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setMessage(Messages.SourcesFileDownloadHyperlink_2);
			mb.setText(Messages.SourcesFileDownloadHyperlink_3);
			mb.open();
		}
	}

}