/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.ContainerFileProxy;
import org.eclipse.linuxtools.internal.docker.ui.CheckboxTreeAndListGroup;
import org.eclipse.linuxtools.internal.docker.ui.MinimizedFileSystemElement;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FileSystemElement;
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
public class ContainerCopyFromPage extends WizardPage {

	private final static String NAME = "ContainerCopyFrom.name"; //$NON-NLS-1$
	private final static String TITLE = "ContainerCopyFrom.title"; //$NON-NLS-1$
	private final static String DESC = "ContainerCopyFrom.desc"; //$NON-NLS-1$
	private static final String TARGET_LABEL = "ContainerCopyFrom.target.label"; //$NON-NLS-1$
	private static final String TARGET_TOOLTIP = "ContainerCopyFrom.target.tooltip"; //$NON-NLS-1$
	private static final String SOURCE_LABEL = "ContainerCopyFrom.source.label"; //$NON-NLS-1$
	private static final String SOURCE_TOOLTIP = "ContainerCopyFrom.source.tooltip"; //$NON-NLS-1$
	private static final String NO_TARGET_SPECIFIED = "ContainerCopyFrom.notarget.error"; //$NON-NLS-1$
	private static final String NO_SOURCE_SPECIFIED = "ContainerCopyFrom.nosource.error"; //$NON-NLS-1$
	private static final String BROWSE_LABEL = "ContainerCopyFrom.browse.label"; //$NON-NLS-1$

	// the root file representative to populate the viewer with
    private FileSystemElement root;

	private IImportStructureProvider structureProvider;

	private boolean canBrowseContainer;

    // the visual selection widget group
    CheckboxTreeAndListGroup selectionGroup;

	private Text targetText;
	private Text sourceText;
	private Button browseButton;

	private String target;

    // sizing constants
    private static final int SIZING_SELECTION_WIDGET_WIDTH = 500;

    private static final int SIZING_SELECTION_WIDGET_HEIGHT = 250;
	private static final int COLUMNS = 3;

    /**
	 * Wizard page for copying files from container
	 * 
	 * @param fileSystemElement
	 *            - FileSystemElement of root
	 * @param structureProvider
	 *            - IImportStructureProvider to get file system structure from
	 *            container
	 * @param containerName
	 *            - name of container
	 * @param canBrowseContainer
	 *            - whether we can browse the Container's file system
	 */
	public ContainerCopyFromPage(FileSystemElement fileSystemElement,
			IImportStructureProvider structureProvider, String containerName,
			boolean canBrowseContainer) {
		super(WizardMessages.getString(NAME));
		setDescription(WizardMessages.getFormattedString(DESC,
				containerName));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
        root = fileSystemElement;
		this.structureProvider = structureProvider;
		this.canBrowseContainer = canBrowseContainer;
    }


	/*
	 * Return the host directory for the copy operation
	 */
	public String getTarget() {
		return target;
	}

	/*
	 * Get an iterator for the selected items to copy
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Iterator getValueIterator() {
		if (canBrowseContainer) {
			return selectionGroup.getAllWhiteCheckedItems();
		} else {
			// We need to fake a collection of MinimizedFileSystemElement with
			// one element.
			ArrayList out = new ArrayList();
			String sourceName = sourceText.getText();
			MinimizedFileSystemElement element = new MinimizedFileSystemElement(
					sourceName, null, false);
			element.setFileSystemObject(
					new ContainerFileProxy(sourceName, "", false)); //$NON-NLS-1$
			out.add(element);
			return out.iterator();
		}
	}

	private ModifyListener Listener = e -> validate();
	private ICheckStateListener CheckListener = e -> validate();

	private void validate() {
		boolean complete = true;
		boolean error = false;

		if (targetText.getText().length() == 0) {
			error = true;
			setErrorMessage(WizardMessages.getString(NO_TARGET_SPECIFIED));
		} else {
			File f = new File(targetText.getText());
			if (!f.exists()) {
				error = true;
				setErrorMessage(WizardMessages.getString(NO_TARGET_SPECIFIED));
			}
		}

		if (!error) {
			if (canBrowseContainer) {
				if (selectionGroup.getCheckedElementCount() == 0) {
					error = true;
					setErrorMessage(
							WizardMessages.getString(NO_SOURCE_SPECIFIED));
				}
			} else {
				if (sourceText.getText().length() == 0) {
					error = true;
					setErrorMessage(
							WizardMessages.getString(NO_SOURCE_SPECIFIED));
				}
			}
		}

		if (!error) {
			target = targetText.getText();
			setErrorMessage(null);
		}
		setPageComplete(complete && !error);
	}

    @Override
	public void createControl(Composite parent) {
        // page group
		Composite composite = new Composite(parent, SWT.NULL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(composite);

		Label targetLabel = new Label(composite, SWT.NULL);
		targetLabel.setText(WizardMessages.getString(TARGET_LABEL));

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.grab(false, false).applyTo(targetLabel);

		targetText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		targetText.addModifyListener(Listener);
		targetText.setToolTipText(WizardMessages.getString(TARGET_TOOLTIP));

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(targetText);

		browseButton = new Button(composite, SWT.NONE);
		browseButton.setText(WizardMessages.getString(BROWSE_LABEL));
		browseButton.addSelectionListener(onBrowseSelect());

		GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).span(1, 1)
				.grab(false, false).applyTo(browseButton);

		// Create a fake parent of the root to be the dialog input element.
        // Use an empty label so that display of the element's full name
        // doesn't include a confusing label
        FileSystemElement input = new FileSystemElement("", null, true);//$NON-NLS-1$
        input.addChild(root);
        root.setParent(input);

		Composite selectionComposite = null;

		if (canBrowseContainer) {
			selectionComposite = new Composite(composite, SWT.NULL);
			GridLayout selectionLayout = new GridLayout();
			selectionComposite.setLayout(selectionLayout);

			selectionGroup = new CheckboxTreeAndListGroup(selectionComposite,
					input, getFolderProvider(), getDynamicFolderProvider(),
					new WorkbenchLabelProvider(), getFileProvider(),
					new WorkbenchLabelProvider(), SWT.NONE,
					SIZING_SELECTION_WIDGET_WIDTH, // since this page has no
													// other significantly-sized
					SIZING_SELECTION_WIDGET_HEIGHT); // widgets we need to
														// hardcode the combined
														// widget's
			// size, otherwise it will open too small
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(3, 1)
					.grab(true, false).applyTo(selectionComposite);
		} else {
			Label sourceLabel = new Label(composite, SWT.NONE);
			sourceLabel.setText(WizardMessages.getString(SOURCE_LABEL));

			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
					.span(1, 1).grab(false, false).applyTo(sourceLabel);

			sourceText = new Text(composite, SWT.BORDER | SWT.SINGLE);
			sourceText.addModifyListener(Listener);
			sourceText.setToolTipText(WizardMessages.getString(SOURCE_TOOLTIP));

			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
					.grab(true, false).applyTo(sourceText);

		}

		if (canBrowseContainer) {
			WorkbenchViewerComparator comparator = new WorkbenchViewerComparator();
			selectionGroup.setTreeComparator(comparator);
			selectionGroup.setListComparator(comparator);
			selectionGroup.addCheckStateListener(CheckListener);
		}

		setControl(composite);
		validate();
		setPageComplete(false);

		if (canBrowseContainer) {
			selectionGroup.aboutToOpen();
		}
    }

	private SelectionListener onBrowseSelect() {
		final ContainerCopyFromPage page = this;
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(
						Activator.getActiveWorkbenchShell());
				String x = d.open();
				if (x != null) {
					page.targetText.setText(x);
				}
			}
		};

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

}
