package org.eclipse.linuxtools.rpm.speceditor.rcp;
/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Helper class used to process delayed events.
 * Events currently supported:
 * <ul>
 * <li>SWT.OpenDoc</li>
 * </ul>
 * @since 3.3
 */
public class DelayedEventsProcessor implements Listener {

	private ArrayList<String> filesToOpen = new ArrayList<String>(1);

	/**
	 * Constructor.
	 * @param display display used as a source of event
	 */
	public DelayedEventsProcessor(Display display) {
		display.addListener(SWT.OpenDocument, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		final String path = event.text;
		if (path == null)
			return;
		// If we start supporting events that can arrive on a non-UI thread, the following
		// line will need to be in a "synchronized" block:
		filesToOpen.add(path); 
	}
	
	/**
	 * Process delayed events.
	 * @param display display associated with the workbench 
	 */
	public void catchUp(Display display) {
		if (filesToOpen.isEmpty())
			return;
		
		// If we start supporting events that can arrive on a non-UI thread, the following
		// lines will need to be in a "synchronized" block:
		String[] filePaths = new String[filesToOpen.size()];
		filesToOpen.toArray(filePaths);
		filesToOpen.clear();

		for(int i = 0; i < filePaths.length; i++) {
			openFile(display, filePaths[i]);
		}
	}

	private void openFile(Display display, final String path) {
		display.asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return;
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
				IFileInfo fetchInfo = fileStore.fetchInfo();
				if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
					IWorkbenchPage page = window.getActivePage();
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (PartInitException e) {
						String msg = "Error on open of: " +	fileStore.getName();
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								"Initial Open",
								msg, SWT.SHEET);
					}
				} else if (!fetchInfo.isDirectory()) {
					IWorkbenchPage page = window.getActivePage();
					try {
						fileStore.openOutputStream(0, null);
						fetchInfo = fileStore.fetchInfo();
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (CoreException e) {
						String msg = "Error on open of: " +	fileStore.getName();
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								"Initial Open",
								msg, SWT.SHEET);
					}
				} else {
					String msg = "File not found: " + path.toString();
					MessageDialog.open(MessageDialog.ERROR, window.getShell(),
							"Initial Open",
							msg, SWT.SHEET);
				}
			}
		});
	}

}
