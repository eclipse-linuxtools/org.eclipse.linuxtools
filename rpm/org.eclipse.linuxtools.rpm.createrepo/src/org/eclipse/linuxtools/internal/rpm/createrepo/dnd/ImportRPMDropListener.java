/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.dnd;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Drop listener that only accepts a copy drag of a file.
 */
public class ImportRPMDropListener extends ViewerDropAdapter {

	private CreaterepoProject project;

	/**
	 * Default constructor.
	 *
	 * @param viewer The viewer to listen to.
	 * @param project The createrepo project.
	 */
	public ImportRPMDropListener(Viewer viewer, CreaterepoProject project) {
		super(viewer);
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragEnter(DropTargetEvent event) {
		// only support file transfer types
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			// change the detail to a drag copy if is file transfer
			event.detail = DND.DROP_COPY;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		// true if it is a file transfer and is a copy action
		if (FileTransfer.getInstance().isSupportedType(transferType) && operation == DND.DROP_COPY) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@Override
	public boolean performDrop(Object data) {
		// data should be an array of paths to the file being transferred
		if (data instanceof String[]) {
			String[] dragData = (String[]) data;
			for (String str : dragData) {
				IPath path = new Path(str);
				try {
					project.importRPM(path.toFile());
				} catch (CoreException e) {
					MessageConsoleStream os = CreaterepoUtils.findConsole(Messages.CreaterepoProject_consoleName)
							.newMessageStream();
					os.print(NLS.bind(Messages.ImportRPMDropListener_errorCopyingFileToProject,
							path.segment(path.segmentCount()-1)));
				}
			}
			return true;
		}
		return false;
	}

}
