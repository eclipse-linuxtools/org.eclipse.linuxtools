/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *     Red Hat Inc. - modified for use with Docker Tooling
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.docker.core.ContainerFileProxy;
import org.eclipse.linuxtools.internal.docker.ui.ContainerDirectorySelectionDialog;
import org.eclipse.linuxtools.internal.docker.ui.ContainerFileSystemProvider;
import org.eclipse.linuxtools.internal.docker.ui.IDEFileSystemStructureProvider;
import org.eclipse.linuxtools.internal.docker.ui.MinimizedFileSystemElement;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.dialogs.WizardResourceImportPage;
import org.eclipse.ui.ide.dialogs.IElementFilter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * Page 1 of the Copy To Container Wizard
 */
public class ContainerCopyToPage
		extends WizardResourceImportPage {

	public static final String FILE_SYSTEM_IMPORT_WIZARD_PAGE = "org.eclipse.ui." //$NON-NLS-1$
			+ "file_system_import_wizard_page"; //$NON-NLS-1$

	// widgets
	protected Combo sourceNameField;

	protected String pathVariable;

	protected Button sourceBrowseButton;

	protected Button selectTypesButton;

	protected Button selectAllButton;

	protected Button deselectAllButton;

	// A boolean to indicate if the user has typed anything
	private boolean entryChanged = false;

	private IDEFileSystemStructureProvider fileSystemStructureProvider = new IDEFileSystemStructureProvider();

	// dialog store id constants
	private final static String STORE_SOURCE_NAMES_ID = "WizardFileSystemResourceImportPage1.STORE_SOURCE_NAMES_ID";//$NON-NLS-1$

	private static final String SELECT_TYPES_TITLE = CopyToContainerMessages.DataTransfer_selectTypes;

	private static final String SELECT_ALL_TITLE = CopyToContainerMessages.DataTransfer_selectAll;

	private static final String DESELECT_ALL_TITLE = CopyToContainerMessages.DataTransfer_deselectAll;

	private static final String SELECT_SOURCE_TITLE = CopyToContainerMessages.ContainerCopyTo_selectSourceTitle;

	private static final String SELECT_SOURCE_MESSAGE = CopyToContainerMessages.ContainerCopyTo_selectSource;

	protected static final String SOURCE_EMPTY_MESSAGE = CopyToContainerMessages.ContainerCopyTo_sourceEmpty;

	protected static final String DESTINATION_EMPTY_MESSAGE = CopyToContainerMessages.ContainerCopyTo_destinationEmpty;

	protected static final String INTO_FOLDER_LABEL = CopyToContainerMessages.ContainerCopyTo_intoFolder;

	protected static final String CONTAINER_DIRECTORY_MSG = CopyToContainerMessages.ContainerCopyTo_containerDirectoryMsg;

	private FileSystemElement root;

	private boolean canBrowseContainer;

	private ContainerFileSystemProvider provider;

	private String containerName;

	private Text containerNameField;

	private List<Object> fileSystemObjects = new ArrayList<>();

	private IPath destination;

	/**
	 * Constructor
	 * 
	 * @param root
	 *            - file system element root of the Container file system ("/")
	 * @param provider
	 *            - file system provider that can dynamically traverse Container
	 *            directories
	 * @param containerName
	 *            - name of Container
	 * @param canBrowseContainer
	 *            - whether we can browse Container's file system or not
	 */
	public ContainerCopyToPage(FileSystemElement root,
			ContainerFileSystemProvider provider, String containerName,
			boolean canBrowseContainer) {
		super("ContainerCopyToPage1", StructuredSelection.EMPTY);//$NON-NLS-1$
		this.root = root;
		this.provider = provider;
		this.containerName = containerName;
		this.canBrowseContainer = canBrowseContainer;
		setTitle(NLS.bind(CopyToContainerMessages.ContainerCopyTo_title,
				this.containerName));
		setDescription(
				NLS.bind(CopyToContainerMessages.ContainerCopyTo_description,
						containerName));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
    }

	/**
	 * Creates a new button with the given id.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates a
	 * standard push button, registers for selection events including button
	 * presses and registers default buttons with its shell. The button id is
	 * stored as the buttons client data. Note that the parent's layout is
	 * assumed to be a GridLayout and the number of columns in this layout is
	 * incremented. Subclasses may override.
	 * </p>
	 *
	 * @param parent
	 *            the parent composite
	 * @param id
	 *            the id of the button (see <code>IDialogConstants.*_ID</code>
	 *            constants for standard dialog button ids)
	 * @param label
	 *            the label from the button
	 * @param defaultButton
	 *            <code>true</code> if thdocker stop vs pausee button is to be
	 *            the default button, and <code>false</code> otherwise
	 */
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;

		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());

		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(buttonData);

		button.setData(id);
		button.setText(label);

		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
			button.setFocus();
		}
		return button;
	}

	/**
	 * Creates the buttons for selecting specific types or selecting all or none
	 * of the elements.
	 *
	 * @param parent
	 *            the parent control
	 */
	protected final void createButtonsGroup(Composite parent) {
		// top level group
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(layout);
		buttonComposite.setFont(parent.getFont());
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonComposite.setLayoutData(buttonData);

		// types edit button
		selectTypesButton = createButton(buttonComposite,
				IDialogConstants.SELECT_TYPES_ID, SELECT_TYPES_TITLE, false);

		SelectionListener listener = SelectionListener
				.widgetSelectedAdapter(e -> handleTypesEditButtonPressed());
		selectTypesButton.addSelectionListener(listener);
		setButtonLayoutData(selectTypesButton);

		selectAllButton = createButton(buttonComposite,
				IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false);

		listener = SelectionListener.widgetSelectedAdapter(e -> {
			setAllSelections(true);
			updateWidgetEnablements();
		});
		selectAllButton.addSelectionListener(listener);
		setButtonLayoutData(selectAllButton);

		deselectAllButton = createButton(buttonComposite,
				IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false);

		listener = SelectionListener.widgetSelectedAdapter(e -> {
			setAllSelections(false);
			updateWidgetEnablements();
		});
		deselectAllButton.addSelectionListener(listener);
		setButtonLayoutData(deselectAllButton);

	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		validateSourceGroup();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				FILE_SYSTEM_IMPORT_WIZARD_PAGE);
	}

	@Override
	protected void createOptionsGroup(Composite parent) {
		// do nothing
	}

	@Override
	protected void createDestinationGroup(Composite parent) {
		if (canBrowseContainer) {
			super.createDestinationGroup(parent);
		} else {
			// container specification group
			Composite containerGroup = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			containerGroup.setLayout(layout);
			containerGroup.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
			containerGroup.setFont(parent.getFont());

			// container label
			Label resourcesLabel = new Label(containerGroup, SWT.NONE);
			resourcesLabel.setText(INTO_FOLDER_LABEL);
			resourcesLabel.setFont(parent.getFont());

			// container name entry field
			containerNameField = new Text(containerGroup,
					SWT.SINGLE | SWT.BORDER);
			BidiUtils.applyBidiProcessing(containerNameField, "file"); //$NON-NLS-1$

			containerNameField.addListener(SWT.Modify, this);
			GridData data = new GridData(
					GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			data.widthHint = SIZING_TEXT_FIELD_WIDTH;
			containerNameField.setLayoutData(data);
			containerNameField.setFont(parent.getFont());

			Label dummyLabel = new Label(containerGroup, SWT.NONE);
			dummyLabel.setText(" "); //$NON-NLS-1$
		}
	}
	/**
	 * Create the group for creating the root directory
	 */
	protected void createRootDirectoryGroup(Composite parent) {
		Composite sourceContainerGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		sourceContainerGroup.setLayout(layout);
		sourceContainerGroup.setFont(parent.getFont());
		sourceContainerGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		Label groupLabel = new Label(sourceContainerGroup, SWT.NONE);
		groupLabel.setText(getSourceLabel());
		groupLabel.setFont(parent.getFont());

		// source name entry field
		sourceNameField = new Combo(sourceContainerGroup, SWT.BORDER);
		GridData data = new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		sourceNameField.setLayoutData(data);
		sourceNameField.setFont(parent.getFont());
		BidiUtils.applyBidiProcessing(sourceNameField, "file");

		sourceNameField.addSelectionListener(SelectionListener
				.widgetSelectedAdapter(e -> updateFromSourceField()));

		sourceNameField.addKeyListener(KeyListener.keyPressedAdapter(e -> {
			if (e.character == SWT.CR) {
				entryChanged = false;
				updateFromSourceField();
			}
		}));

		sourceNameField.addModifyListener(e -> entryChanged = true);

		sourceNameField.addFocusListener(FocusListener.focusLostAdapter(e -> {
			// Clear the flag to prevent constant update
			if (entryChanged) {
				entryChanged = false;
				updateFromSourceField();
			}

		}));

		// source browse button
		sourceBrowseButton = new Button(sourceContainerGroup, SWT.PUSH);
		sourceBrowseButton.setText(CopyToContainerMessages.DataTransfer_browse);
		sourceBrowseButton.addListener(SWT.Selection, this);
		sourceBrowseButton
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		sourceBrowseButton.setFont(parent.getFont());
		setButtonLayoutData(sourceBrowseButton);
	}

	/**
	 * Update the receiver from the source name field.
	 */

	private void updateFromSourceField() {

		setSourceName(sourceNameField.getText());
		// Update enablements when this is selected
		updateWidgetEnablements();
		fileSystemStructureProvider.clearVisitedDirs();
		selectionGroup.setFocus();
	}

	/**
	 * Creates and returns a <code>FileSystemElement</code> if the specified
	 * file system object merits one. The criteria for this are: Also create the
	 * children.
	 */
	protected MinimizedFileSystemElement createRootElement(
			Object fileSystemObject, IImportStructureProvider provider) {
		boolean isContainer = provider.isFolder(fileSystemObject);
		String elementLabel = provider.getLabel(fileSystemObject);

		// Use an empty label so that display of the element's full name
		// doesn't include a confusing label
		MinimizedFileSystemElement dummyParent = new MinimizedFileSystemElement(
				"", null, true);//$NON-NLS-1$
		dummyParent.setPopulated();
		MinimizedFileSystemElement result = new MinimizedFileSystemElement(
				elementLabel, dummyParent, isContainer);
		result.setFileSystemObject(fileSystemObject);

		// Get the files for the element so as to build the first level
		result.getFiles(provider);

		return dummyParent;
	}

	/**
	 * Create the import source specification widgets
	 */
	@Override
	protected void createSourceGroup(Composite parent) {

		createRootDirectoryGroup(parent);
		createFileSelectionGroup(parent);
		createButtonsGroup(parent);
	}

	/**
	 * Enable or disable the button group.
	 */
	protected void enableButtonGroup(boolean enable) {
		selectTypesButton.setEnabled(enable);
		selectAllButton.setEnabled(enable);
		deselectAllButton.setEnabled(enable);
	}

	/**
	 * Answer a boolean indicating whether the specified source currently exists
	 * and is valid
	 */
	protected boolean ensureSourceIsValid() {
		if (new File(getSourceDirectoryName()).isDirectory()) {
			return true;
		}

		setErrorMessage(CopyToContainerMessages.ContainerCopyTo_invalidSource);
		return false;
	}


	/**
	 * The Finish button was pressed. Set up results so caller can access them.
	 * Return a boolean indicating success. If false is returned then the wizard
	 * will not close.
	 *
	 * @return boolean
	 */
	@SuppressWarnings({ "rawtypes" })
	public boolean finish() {
		if (!ensureSourceIsValid()) {
			return false;
		}

		saveWidgetValues();

		Iterator resourcesEnum = this.selectionGroup.getAllWhiteCheckedItems()
				.iterator();
		while (resourcesEnum.hasNext()) {
			fileSystemObjects.add(((FileSystemElement) resourcesEnum.next())
					.getFileSystemObject());
		}

		destination = getResourcePath();

		if (fileSystemObjects.size() > 0 && !destination.isEmpty()) {
			return true;
		}

		MessageDialog.openInformation(getContainer().getShell(),
				CopyToContainerMessages.DataTransfer_information,
				CopyToContainerMessages.ContainerCopyTo_noneSelected);

		return false;
	}

	@Override
	protected IPath getResourcePath() {
		if (canBrowseContainer) {
			return super.getResourcePath();
		}
		return new Path(containerNameField.getText());
	}

	/**
	 * Return the destination path chosen by the user.
	 * 
	 * @return IPath for destination in Container
	 */
	public IPath getDestination() {
		return destination;
	}

	/**
	 * Return the list of files to copy to the Container.
	 * 
	 * @return List of files to copy
	 */
	public List<Object> getFilesToCopy() {
		return fileSystemObjects;
	}

	/**
	 * Returns a content provider for <code>FileSystemElement</code>s that
	 * returns only files as children.
	 */
	@Override
	protected ITreeContentProvider getFileProvider() {
		return new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement element) {
					return element.getFiles(fileSystemStructureProvider)
							.getChildren(element);
				}
				return new Object[0];
			}
		};
	}

	/**
	 * Answer the root FileSystemElement that represents the contents of the
	 * currently-specified source. If this FileSystemElement is not currently
	 * defined then create and return it.
	 */
	protected MinimizedFileSystemElement getFileSystemTree() {

		File sourceDirectory = getSourceDirectory();
		if (sourceDirectory == null) {
			return null;
		}

		return selectFiles(sourceDirectory, fileSystemStructureProvider);
	}

	/**
	 * Returns a content provider for <code>FileSystemElement</code>s that
	 * returns only folders as children.
	 */
	@Override
	protected ITreeContentProvider getFolderProvider() {
		return new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement element) {
					return element.getFolders(fileSystemStructureProvider)
							.getChildren(element);
				}
				return new Object[0];
			}

			@Override
			public boolean hasChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement element) {
					if (element.isPopulated()) {
						return getChildren(element).length > 0;
					}

					// If we have not populated then wait until asked
					return true;
				}
				return false;
			}
		};
	}

	/**
	 * Returns a File object representing the currently-named source directory
	 * iff it exists as a valid directory, or <code>null</code> otherwise.
	 */
	protected File getSourceDirectory() {
		return getSourceDirectory(this.sourceNameField.getText());
	}

	/**
	 * Returns a File object representing the currently-named source directory
	 * iff it exists as a valid directory, or <code>null</code> otherwise.
	 *
	 * @param path
	 *            a String not yet formatted for java.io.File compatability
	 */
	private File getSourceDirectory(String path) {
		File sourceDirectory = new File(getSourceDirectoryName(path));
		if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
			return null;
		}

		return sourceDirectory;
	}

	/**
	 * Answer the directory name specified as being the import source. Note that
	 * if it ends with a separator then the separator is first removed so that
	 * java treats it as a proper directory
	 */
	private String getSourceDirectoryName() {
		return getSourceDirectoryName(this.sourceNameField.getText());
	}

	/**
	 * Answer the directory name specified as being the import source. Note that
	 * if it ends with a separator then the separator is first removed so that
	 * java treats it as a proper directory
	 */
	private String getSourceDirectoryName(String sourceName) {
		IPath result = new Path(sourceName.trim());

		if (result.getDevice() != null && result.segmentCount() == 0) {
			result = result.addTrailingSeparator();
		} else {
			result = result.removeTrailingSeparator();
		}

		return result.toOSString();
	}

	/**
	 * Answer the string to display as the label for the source specification
	 * field
	 */
	protected String getSourceLabel() {
		return CopyToContainerMessages.ContainerCopyTo_fromDirectory;
	}

	/**
	 * Handle all events and enablements for widgets in this dialog
	 *
	 * @param event
	 *            Event
	 */
	@Override
	public void handleEvent(Event event) {
		if (event.widget == sourceBrowseButton) {
			handleSourceBrowseButtonPressed();
		}

		super.handleEvent(event);
	}

	/**
	 * Open an appropriate source browser so that the user can specify a source
	 * to import from
	 */
	protected void handleSourceBrowseButtonPressed() {

		String currentSource = this.sourceNameField.getText();
		DirectoryDialog dialog = new DirectoryDialog(sourceNameField.getShell(),
				SWT.SAVE | SWT.SHEET);
		dialog.setText(SELECT_SOURCE_TITLE);
		dialog.setMessage(SELECT_SOURCE_MESSAGE);
		dialog.setFilterPath(getSourceDirectoryName(currentSource));

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			// Just quit if the directory is not valid
			if ((getSourceDirectory(selectedDirectory) == null)
					|| selectedDirectory.equals(currentSource)) {
				return;
			}
			// If it is valid then proceed to populate
			setErrorMessage(null);
			setSourceName(selectedDirectory);
			selectionGroup.setFocus();
		}
	}

	/**
	 * Open a registered type selection dialog and note the selections in the
	 * receivers types-to-export field., Added here so that inner classes can
	 * have access
	 */
	@Override
	protected void handleTypesEditButtonPressed() {

		super.handleTypesEditButtonPressed();
	}


	@Override
	protected void handleContainerBrowseButtonPressed() {
		ContainerDirectorySelectionDialog dialog = new ContainerDirectorySelectionDialog(
				sourceNameField.getShell(), this.root, this.provider,
				NLS.bind(CONTAINER_DIRECTORY_MSG, containerName));

		if (dialog.open() == IStatus.OK) {
			Object[] result = dialog.getResult();
			ContainerFileProxy proxy = (ContainerFileProxy) result[0];
			// If it is valid then proceed to populate
			setErrorMessage(null);
			setContainerFieldValue(proxy.getFullPath());
			selectionGroup.setFocus();
		}
		updateWidgetEnablements();
	}

	@Override
	protected boolean determinePageCompletion() {
		boolean complete = validateSourceGroup()
				&& validateDestination();

		// Avoid draw flicker by not clearing the error
		// message unless all is valid.
		if (complete) {
			setErrorMessage(null);
		}

		return complete;
	}

	private boolean validateDestination() {
		IPath containerPath = getResourcePath();
		if (containerPath.isEmpty()) {
			setErrorMessage(DESTINATION_EMPTY_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * Returns whether the extension provided is an extension that has been
	 * specified for export by the user.
	 *
	 * @param extension
	 *            the resource name
	 * @return <code>true</code> if the resource name is suitable for export
	 *         based upon its extension
	 */
	@SuppressWarnings("rawtypes")
	protected boolean isExportableExtension(String extension) {
		if (selectedTypes == null) {
			return true;
		}

		Iterator itr = selectedTypes.iterator();
		while (itr.hasNext()) {
			if (extension.equalsIgnoreCase((String) itr.next())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Repopulate the view based on the currently entered directory.
	 */
	protected void resetSelection() {

		MinimizedFileSystemElement currentRoot = getFileSystemTree();
		this.selectionGroup.setRoot(currentRoot);
	}

	/**
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion
	 */
	@Override
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
			if (sourceNames == null) {
				return; // ie.- no values stored, so stop
			}

			// set filenames history
			for (int i = 0; i < sourceNames.length; i++) {
				sourceNameField.add(sourceNames[i]);
			}

			// // radio buttons and checkboxes
			// overwriteExistingResourcesCheckbox.setSelection(
			// settings.getBoolean(STORE_OVERWRITE_EXISTING_RESOURCES_ID));
			//
			// boolean createStructure = settings
			// .getBoolean(STORE_CREATE_CONTAINER_STRUCTURE_ID);
			// createTopLevelFolderCheckbox.setSelection(createStructure);
			//
			// if (createVirtualFoldersButton != null) {
			// boolean createVirtualFolders = settings
			// .getBoolean(STORE_CREATE_VIRTUAL_FOLDERS_ID);
			// createVirtualFoldersButton.setSelection(createVirtualFolders);
			//
			// boolean createLinkedResources = settings
			// .getBoolean(STORE_CREATE_LINKS_IN_WORKSPACE_ID);
			// createLinksInWorkspaceButton
			// .setSelection(createLinkedResources);
			//
			// boolean pathVariableSelected = settings
			// .getBoolean(STORE_PATH_VARIABLE_SELECTED_ID);
			// relativePathVariableGroup.setSelection(pathVariableSelected);
			//
			// pathVariable = settings.get(STORE_PATH_VARIABLE_NAME_ID);
			// if (pathVariable != null)
			// relativePathVariableGroup.selectVariable(pathVariable);
			// }
			updateWidgetEnablements();
		}
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this wizard page
	 */
	@Override
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// update source names history
			String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
			if (sourceNames == null) {
				sourceNames = new String[0];
			}

			sourceNames = addToHistory(sourceNames, getSourceDirectoryName());
			settings.put(STORE_SOURCE_NAMES_ID, sourceNames);

			// // radio buttons and checkboxes
			// settings.put(STORE_OVERWRITE_EXISTING_RESOURCES_ID,
			// overwriteExistingResourcesCheckbox.getSelection());
			//
			// settings.put(STORE_CREATE_CONTAINER_STRUCTURE_ID,
			// createTopLevelFolderCheckbox.getSelection());
			//
			// if (createVirtualFoldersButton != null) {
			// settings.put(STORE_CREATE_VIRTUAL_FOLDERS_ID,
			// createVirtualFoldersButton.getSelection());
			//
			// settings.put(STORE_CREATE_LINKS_IN_WORKSPACE_ID,
			// createLinksInWorkspaceButton.getSelection());
			//
			// settings.put(STORE_PATH_VARIABLE_SELECTED_ID,
			// relativePathVariableGroup.getSelection());
			//
			// settings.put(STORE_PATH_VARIABLE_NAME_ID, pathVariable);
			// }
		}
	}

	/**
	 * Invokes a file selection operation using the specified file system and
	 * structure provider. If the user specifies files to be imported then this
	 * selection is cached for later retrieval and is returned.
	 */
	protected MinimizedFileSystemElement selectFiles(
			final Object rootFileSystemObject,
			final IImportStructureProvider structureProvider) {

		final MinimizedFileSystemElement[] results = new MinimizedFileSystemElement[1];

		BusyIndicator.showWhile(getShell().getDisplay(),
				() -> results[0] = createRootElement(rootFileSystemObject,
						structureProvider));

		return results[0];
	}

	/**
	 * Set all of the selections in the selection group to value. Implemented
	 * here to provide access for inner classes.
	 * 
	 * @param value
	 *            boolean
	 */
	@Override
	protected void setAllSelections(boolean value) {
		super.setAllSelections(value);
	}

	/**
	 * Sets the source name of the import to be the supplied path. Adds the name
	 * of the path to the list of items in the source combo and selects it.
	 *
	 * @param path
	 *            the path to be added
	 */
	protected void setSourceName(String path) {

		if (path.length() > 0) {

			String[] currentItems = this.sourceNameField.getItems();
			int selectionIndex = -1;
			for (int i = 0; i < currentItems.length; i++) {
				if (currentItems[i].equals(path)) {
					selectionIndex = i;
				}
			}
			if (selectionIndex < 0) {
				int oldLength = currentItems.length;
				String[] newItems = new String[oldLength + 1];
				System.arraycopy(currentItems, 0, newItems, 0, oldLength);
				newItems[oldLength] = path;
				this.sourceNameField.setItems(newItems);
				selectionIndex = oldLength;
			}
			this.sourceNameField.select(selectionIndex);

			resetSelection();
		}
	}

	/**
	 * Update the tree to only select those elements that match the selected
	 * types
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected void setupSelectionsBasedOnSelectedTypes() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(
				getContainer().getShell());
		final Map selectionMap = new Hashtable();

		final IElementFilter filter = new IElementFilter() {

			@Override
			public void filterElements(Collection files,
					IProgressMonitor monitor) throws InterruptedException {
				if (files == null) {
					throw new InterruptedException();
				}
				Iterator filesList = files.iterator();
				while (filesList.hasNext()) {
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
					checkFile(filesList.next());
				}
			}

			@Override
			public void filterElements(Object[] files, IProgressMonitor monitor)
					throws InterruptedException {
				if (files == null) {
					throw new InterruptedException();
				}
				for (int i = 0; i < files.length; i++) {
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
					checkFile(files[i]);
				}
			}

			@SuppressWarnings("unchecked")
			private void checkFile(Object fileElement) {
				MinimizedFileSystemElement file = (MinimizedFileSystemElement) fileElement;
				if (isExportableExtension(file.getFileNameExtension())) {
					List elements = new ArrayList();
					FileSystemElement parent = file.getParent();
					if (selectionMap.containsKey(parent)) {
						elements = (List) selectionMap.get(parent);
					}
					elements.add(file);
					selectionMap.put(parent, elements);
				}
			}

		};

		IRunnableWithProgress runnable = monitor -> {
			monitor.beginTask(CopyToContainerMessages.ImportPage_filterSelections,
					IProgressMonitor.UNKNOWN);
			getSelectedResources(filter, monitor);
		};

		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException exception) {
			// Couldn't start. Do nothing.
			return;
		} catch (InterruptedException exception) {
			// Got interrupted. Do nothing.
			return;
		}
		// make sure that all paint operations caused by closing the progress
		// dialog get flushed, otherwise extra pixels will remain on the screen
		// until
		// updateSelections is completed
		getShell().update();
		// The updateSelections method accesses SWT widgets so cannot be
		// executed
		// as part of the above progress dialog operation since the operation
		// forks
		// a new process.
		if (selectionMap != null) {
			updateSelections(selectionMap);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		resetSelection();
		if (visible) {
			this.selectionGroup.setFocus();
			this.sourceNameField.setFocus();
		}
	}

	/**
	 * Update the selections with those in map . Implemented here to give inner
	 * class visibility
	 * 
	 * @param map
	 *            Map - key tree elements, values Lists of list elements
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected void updateSelections(Map map) {
		super.updateSelections(map);
	}

	/**
	 * Check if widgets are enabled or disabled by a change in the dialog.
	 * Provided here to give access to inner classes.
	 */
	@Override
	protected void updateWidgetEnablements() {
		super.updateWidgetEnablements();
		enableButtonGroup(ensureSourceIsValid());
	}

	/**
	 * Answer a boolean indicating whether self's source specification widgets
	 * currently all contain valid values.
	 */
	@Override
	protected boolean validateSourceGroup() {
		File sourceDirectory = getSourceDirectory();
		if (sourceDirectory == null) {
			setErrorMessage(SOURCE_EMPTY_MESSAGE);
			enableButtonGroup(false);
			return false;
		}

		if (sourceConflictsWithDestination(
				new Path(sourceDirectory.getPath()))) {
			setMessage(null);
			setErrorMessage(getSourceConflictMessage());
			enableButtonGroup(false);
			return false;
		}

		@SuppressWarnings("rawtypes")
		List resourcesToExport = selectionGroup.getAllWhiteCheckedItems();
		if (resourcesToExport.size() == 0) {
			setMessage(null);
			setErrorMessage(
					CopyToContainerMessages.ContainerCopyTo_noneSelected);
			return false;
		}

		enableButtonGroup(true);
		setErrorMessage(null);
		return true;
	}

	/**
	 * Returns whether the source location conflicts with the destination
	 * resource. This can not occur.
	 *
	 * @param sourcePath
	 *            the path to check
	 * @return <code>false</code>
	 */
	@Override
	protected boolean sourceConflictsWithDestination(IPath sourcePath) {
		return false;
	}

}
