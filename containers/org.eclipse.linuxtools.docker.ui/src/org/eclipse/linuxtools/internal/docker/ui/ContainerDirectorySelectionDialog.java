/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.internal.docker.core.ContainerFileProxy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * A standard directory selection dialog which solicits a choice of directory
 * from the user. The <code>getResult</code> method returns the selected
 * directory in a list.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 * 		ProgressMonitorDialog pd = new ProgressMonitorDialog(
 *				Activator.getActiveWorkbenchShell());
 *		ContainerFileSystemProvider provider = new ContainerFileSystemProvider(
 *				connection, container.id());
 *		PopulateContainerFilesOperation sfo = new PopulateContainerFilesOperation(
 *				new ContainerFileProxy("", "", true), //$NON-NLS-1$ //$NON-NLS-2$
 *				null, provider);
 *		try {
 *			pd.run(true, true, sfo); m
 *		} catch (InvocationTargetException | InterruptedException e) {
 *			e.printStackTrace();
 *		}
 *		ContainerDirectorySelectionDialog d = new ContainerDirectorySelectionDialog(
 *				Activator.getActiveWorkbenchShell(), sfo.getResult(), provider,
 *				null);
 *		if (d.open() == IStatus.OK) {
 *			return d.getResult();
 *		}
 * </pre>
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ContainerDirectorySelectionDialog extends SelectionDialog {
    // the root file representative to populate the viewer with
    private FileSystemElement root;

	private IImportStructureProvider structureProvider;
	// static final String FILE_SELECTION_DIALOG =
	// "org.eclipse.ui.ide.file_selection_dialog_context"; //$NON-NLS-1$

    // the visual selection widget group
	ContainerTreeGroup selectionGroup;

    // expand all items in the tree view on dialog open
    private boolean expandAllOnOpen = false;

    // sizing constants
    private static final int SIZING_SELECTION_WIDGET_WIDTH = 500;

    private static final int SIZING_SELECTION_WIDGET_HEIGHT = 250;

	static final String SELECT_ALL_TITLE = "SelectionDialog_selectLabel"; //$NON-NLS-1$
	static final String DESELECT_ALL_TITLE = "SelectionDialog_deselectLabel"; //$NON-NLS-1$

    /**
     * Creates a file selection dialog rooted at the given file system element.
     *
     * @param parentShell the parent shell
     * @param fileSystemElement the root element to populate this dialog with
     * @param message the message to be displayed at the top of this dialog, or
     *    <code>null</code> to display a default message
     */
    public ContainerDirectorySelectionDialog(Shell parentShell,
			FileSystemElement fileSystemElement,
			IImportStructureProvider structureProvider, String message) {
        super(parentShell);
		setTitle(Messages.getString("DirectorySelectionDialog_title")); //$NON-NLS-1$
        root = fileSystemElement;
		this.structureProvider = structureProvider;
        if (message != null) {
			setMessage(message);
		} else {
			setMessage(Messages.getString("DirectorySelectionDialog_message")); //$NON-NLS-1$
		}
    }

    @Override
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
		// TODO: replace with correct help context
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
		// FILE_SELECTION_DIALOG);
    }

    @Override
	public void create() {
        super.create();
        initializeDialog();
    }

    @Override
	protected Control createDialogArea(Composite parent) {
        // page group
        Composite composite = (Composite) super.createDialogArea(parent);

        createMessageArea(composite);

        // Create a fake parent of the root to be the dialog input element.
        // Use an empty label so that display of the element's full name
        // doesn't include a confusing label
        FileSystemElement input = new FileSystemElement("", null, true);//$NON-NLS-1$
        input.addChild(root);
        root.setParent(input);

		selectionGroup = new ContainerTreeGroup(composite, input,
				getFolderProvider(), getDynamicFolderProvider(),
				new WorkbenchLabelProvider(),
				SWT.NONE,
                SIZING_SELECTION_WIDGET_WIDTH, // since this page has no other significantly-sized
                SIZING_SELECTION_WIDGET_HEIGHT); // widgets we need to hardcode the combined widget's
        // size, otherwise it will open too small

		ISelectionChangedListener listener = event -> getOkButton()
				.setEnabled(event.getSelection() != null
						&& !event.getSelection().isEmpty());

        WorkbenchViewerComparator comparator = new WorkbenchViewerComparator();
        selectionGroup.setTreeComparator(comparator);
		selectionGroup.addSelectionChangedListener(listener);
        return composite;
    }

    /**
     * Returns whether the tree view of the file system element
     * will be fully expanded when the dialog is opened.
     *
     * @return true to expand all on dialog open, false otherwise.
     */
    public boolean getExpandAllOnOpen() {
        return expandAllOnOpen;
    }

    /**
     * Returns a content provider for <code>FileSystemElement</code>s that returns
     * only folders as children.
     */
    private ITreeContentProvider getFolderProvider() {
        return new WorkbenchContentProvider() {
            @Override
			public Object[] getChildren(Object o) {
				if (o instanceof FileSystemElement) {
					return ((FileSystemElement) o).getFolders().getChildren(o);
                }

                return new Object[0];
			}
        };
    }

    /**
	 * Returns a content provider for <code>FileSystemElement</code>s that
	 * returns only folders as children.
	 */
	private ITreeContentProvider getDynamicFolderProvider() {
		return new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement) {
					return ((MinimizedFileSystemElement) o)
							.getFolders(structureProvider)
							.getChildren(o);
				} else if (o instanceof FileSystemElement) {
					return ((FileSystemElement) o).getFolders().getChildren(o);
				}

				return new Object[0];
			}
		};
	}

	/**
	 * Initializes this dialog's controls.
	 */
    private void initializeDialog() {
        // initialize page
        if (getInitialElementSelections().isEmpty()) {
			getOkButton().setEnabled(false);
		}
        selectionGroup.aboutToOpen();
    }

    /**
     * The <code>FileSelectionDialog</code> implementation of this
     * <code>Dialog</code> method builds a list of the selected files for later
     * retrieval by the client and closes this dialog.
     */
	@SuppressWarnings({})
	@Override
	protected void okPressed() {
		Object result = selectionGroup.getCurrentSelection();
		if (result != null) {
			MinimizedFileSystemElement selected = (MinimizedFileSystemElement) result;
			List<ContainerFileProxy> list = new ArrayList<>();
			list.add((ContainerFileProxy) selected.getFileSystemObject());
			setResult(list);
		}
        super.okPressed();
    }

    /**
     * Set whether the tree view of the file system element
     * will be fully expanded when the dialog is opened.
     *
     * @param expandAll true to expand all on dialog open, false otherwise.
     */
    public void setExpandAllOnOpen(boolean expandAll) {
        expandAllOnOpen = expandAll;
    }
}
