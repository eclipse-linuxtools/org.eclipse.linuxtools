/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others.
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
import java.util.Iterator;

import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * A standard file selection dialog which solicits a list of files from the user.
 * The <code>getResult</code> method returns the selected files.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 *	FileSelectionDialog dialog =
 *		new FileSelectionDialog(getShell(), rootElement, msg);
 *	dialog.setInitialSelections(selectedResources);
 *	dialog.open();
 *	return dialog.getResult();
 * </pre>
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ContainerFileSelectionDialog extends SelectionDialog {
    // the root file representative to populate the viewer with
    private FileSystemElement root;

	private IImportStructureProvider structureProvider;
	static final String FILE_SELECTION_DIALOG = "org.eclipse.ui.ide.file_selection_dialog_context"; //$NON-NLS-1$

    // the visual selection widget group
    CheckboxTreeAndListGroup selectionGroup;

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
    public ContainerFileSelectionDialog(Shell parentShell,
			FileSystemElement fileSystemElement,
			IImportStructureProvider structureProvider, String message) {
        super(parentShell);
		setTitle(Messages.getString("FileSelectionDialog_title")); //$NON-NLS-1$
        root = fileSystemElement;
		this.structureProvider = structureProvider;
        if (message != null) {
			setMessage(message);
		} else {
			setMessage(Messages.getString("FileSelectionDialog_message")); //$NON-NLS-1$
		}
    }

    /**
     * Add the selection and deselection buttons to the dialog.
     * @param composite org.eclipse.swt.widgets.Composite
     */
    private void addSelectionButtons(Composite composite) {

        Composite buttonComposite = new Composite(composite, SWT.RIGHT);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        buttonComposite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        composite.setData(data);

        Button selectButton = new Button(buttonComposite, SWT.PUSH);
		selectButton.setText(Messages.getString(SELECT_ALL_TITLE));
		SelectionListener listener = SelectionListener.widgetSelectedAdapter(
				e -> selectionGroup.setAllSelections(true));
        selectButton.addSelectionListener(listener);

        Button deselectButton = new Button(buttonComposite, SWT.PUSH);
		deselectButton.setText(Messages.getString(DESELECT_ALL_TITLE));
		listener = SelectionListener.widgetSelectedAdapter(
				e -> selectionGroup.setAllSelections(false));
        deselectButton.addSelectionListener(listener);

    }

    /**
     * Visually checks the previously-specified elements in the container (left)
     * portion of this dialog's file selection viewer.
     */
    private void checkInitialSelections() {
		@SuppressWarnings("rawtypes")
		Iterator itemsToCheck = getInitialElementSelections().iterator();

        while (itemsToCheck.hasNext()) {
            FileSystemElement currentElement = (FileSystemElement) itemsToCheck
                    .next();

            if (currentElement.isDirectory()) {
				selectionGroup.initialCheckTreeItem(currentElement);
			} else {
				selectionGroup.initialCheckListItem(currentElement);
			}
        }
    }

    @Override
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				FILE_SELECTION_DIALOG);
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

        selectionGroup = new CheckboxTreeAndListGroup(composite, input,
				getFolderProvider(), getDynamicFolderProvider(),
				new WorkbenchLabelProvider(),
                getFileProvider(), new WorkbenchLabelProvider(), SWT.NONE,
                SIZING_SELECTION_WIDGET_WIDTH, // since this page has no other significantly-sized
                SIZING_SELECTION_WIDGET_HEIGHT); // widgets we need to hardcode the combined widget's
        // size, otherwise it will open too small

        ICheckStateListener listener = event -> getOkButton().setEnabled(
		        selectionGroup.getCheckedElementCount() > 0);

        WorkbenchViewerComparator comparator = new WorkbenchViewerComparator();
        selectionGroup.setTreeComparator(comparator);
        selectionGroup.setListComparator(comparator);
        selectionGroup.addCheckStateListener(listener);

        addSelectionButtons(composite);

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
     * only files as children.
     */
    private ITreeContentProvider getFileProvider() {
        return new WorkbenchContentProvider() {
            @Override
			public Object[] getChildren(Object o) {
				if (o instanceof FileSystemElement) {
					return ((FileSystemElement) o).getFiles().getChildren(o);
                }

                return new Object[0];
            }
        };
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
		} else {
			checkInitialSelections();
		}
        selectionGroup.aboutToOpen();
        if (expandAllOnOpen) {
			selectionGroup.expandAll();
		}
    }

    /**
     * The <code>FileSelectionDialog</code> implementation of this
     * <code>Dialog</code> method builds a list of the selected files for later
     * retrieval by the client and closes this dialog.
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void okPressed() {
        Iterator resultEnum = selectionGroup.getAllCheckedListItems();
        ArrayList list = new ArrayList();
        while (resultEnum.hasNext()) {
			list.add(resultEnum.next());
		}
        setResult(list);
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
