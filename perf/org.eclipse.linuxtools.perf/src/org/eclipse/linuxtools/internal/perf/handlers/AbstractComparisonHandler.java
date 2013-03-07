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

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Abstract class providing functionality for comparison command handlers.
 */
public abstract class AbstractComparisonHandler implements IHandler {
	// selected files
	private static ArrayList<IFile> selectedFiles = new ArrayList<IFile>();

	// previous selection
	private ISelection prevSelection;

	// workbench listener
	private ISelectionListener workbenchListener;

	@Override
	public Object execute(ExecutionEvent event) {
		MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
				Messages.MsgSelectionDiff, Messages.MsgSelectFiles);

		// initialize workbench listener
		workbenchListener = new ISelectionListener() {

			@Override
			public void selectionChanged(IWorkbenchPart sourcepart,
					ISelection selection) {
				handleSelection(sourcepart, selection);
			}
		};

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		workbenchWindow.getSelectionService().addSelectionListener(
				workbenchListener);
		return null;
	}

	@Override
	public boolean isEnabled() {
		// if the workbench listener is not null, then it's being used for the
		// current comparison.
		return workbenchListener == null;
	}

	@Override
	public boolean isHandled() {
		return isEnabled();
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/**
	 * Get selected files.
	 *
	 * @return selected files
	 */
	public static ArrayList<IFile> getSelectedFiles() {
		return selectedFiles;
	}

	/**
	 * Get file from specified selection.
	 *
	 * @param selection the selection
	 * @return the selection's associated <code>IFile</code>
	 * @throws ClassCastException
	 */
	private IFile getFile(ISelection selection) throws ClassCastException {
		// Using IFiles in order to support remote files.
		return (IFile) ((IStructuredSelection) selection).getFirstElement();
	}

	/**
	 * Validate specified file. If file is not valid it will not be added to
	 * list of selected files to compare. This method is to be implemented
	 * by extenders fof this class, the most common validation will be to
	 * check for file extension.
	 *
	 * @param file file to validate
	 * @return true if file is valid, false otherwise
	 */
	protected abstract boolean isValidFile(IFile file);

	/**
	 * Handle selected items.
	 *
	 * @param sourcepart the workbench part containing the selection
	 * @param selection current selection
	 */
	private void handleSelection(IWorkbenchPart sourcepart, ISelection selection) {
		if (!(selection.equals(prevSelection))) {
			try {
				IFile file = getFile(selection);

				// check if selected files are set to be compared
				if (isValidFile(file)
						&& selectedFiles.add(file)
						&& selectedFiles.size() == 2) {

					/**
					 * as instructed to the user, first selected file is old
					 * data and second is the new data
					 */
					IFile oldData = selectedFiles.get(0);
					IFile newData = selectedFiles.get(1);

					// confirm selections with user and trigger comparison
					if (confirmSelections(oldData, newData)) {
						handleComparison(oldData, newData);
					}

					clearSelections();
				}
			} catch (ClassCastException ex) {
				// continue, there are other selections
			}
		}
		prevSelection = selection;
	}

	/**
	 * Confirm selected files with user.
	 *
	 * @param oldFile old file to compare
	 * @param newFile new file to compare
	 * @return true if user confirms the selected files, false otherwise
	 */
	private boolean confirmSelections(IFile oldFile, IFile newFile){
		// confirmation message arguments
		Object[] confirmMsgArgs = new String[] {
				oldFile.getName(), newFile.getName() };

		// confirmation message
		String confirmMsg = MessageFormat.format(Messages.MsgConfirm_msg,
				confirmMsgArgs);

		// confirm selections with user
		boolean confirm = MessageDialog.openConfirm(Display.getCurrent()
				.getActiveShell(), Messages.MsgConfirm_title, confirmMsg);

		return confirm;
	}

	/**
	 * Handle comparison of specified files.
	 *
	 * @param oldData old file to compare
	 * @param newData new file to compare
	 */
	protected abstract void handleComparison(IFile oldData, IFile newData);

	/**
	 * Clear selected files list and remove workbench listener.
	 */
	private void clearSelections() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService()
				.removeSelectionListener(workbenchListener);
		workbenchListener = null;
		selectedFiles.clear();
	}
}
