/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.profiling.launch.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.0
 */
public class ResourceSelectorWidget {

	public enum ResourceType { FILE, DIRECTORY }

	private static String BROWSE_LABEL = ResourceSelectorWidgetMessages.browseLabelText;

	private ResourceType resourceType;
	private Group mainComp;
	private String sectionLabelText;
	private Label uriLabel;
	private Text uriField;
	private Button browseButton;
	private FileSystemSelectionArea fileSystemSelectionArea;

	/**
	 * Open an appropriate directory browser
	 */
	private void handleURIBrowseButtonPressed() {

		String selectedResource = null;
		String path = getURIText().getText();
		FileSystemElement fileSystem = fileSystemSelectionArea.getSelectedFileSystem();

		IRemoteResourceSelectorProxy resourceSelector = fileSystem.getSelectorProxy();
		if (resourceSelector != null) {
			switch (resourceType) {
			case FILE: {
				URI uri = resourceSelector.selectFile(fileSystem.getScheme(), path, ResourceSelectorWidgetMessages.ResourceSelectorWidget_select + sectionLabelText, browseButton.getShell());
				if (uri != null) {
					selectedResource = uri.toString();
				}
				break;
			}
			case DIRECTORY: {
				URI uri = resourceSelector.selectDirectory(fileSystem.getScheme(), path, ResourceSelectorWidgetMessages.ResourceSelectorWidget_select + sectionLabelText, browseButton.getShell());
				if (uri != null) {
					selectedResource = uri.toString();
				}
				break;
			}
			default:
				ProfileLaunchPlugin.log(IStatus.ERROR, ResourceSelectorWidgetMessages.ResourceSelectorWidget_unrecognized_resourceType);
				return;
			}
		} else {
			ProfileLaunchPlugin.log(IStatus.ERROR, ResourceSelectorWidgetMessages.ResourceSelectorWidget_getSelectorProxy_returned_null);
		}

		if (selectedResource != null) {
			updateURIField(selectedResource);
		}
	}



	/**
	 * Update the filesystem selector, if possible
	 *
	 * @param newPath
	 */
	private void updateFilesystemSelector(String newPath) {
	try {
			URI selectedURI  = new URI(newPath);
			String scheme = selectedURI.getScheme();
			try {
				if (scheme == null) {
					fileSystemSelectionArea.setSelectedFileSystem("local"); //$NON-NLS-1$
				} else {
					fileSystemSelectionArea.setSelectedFileSystem(scheme);
				}
			} catch (CoreException e) {
				// Probably an unrecognized scheme.  Don't change the setting of
				// the filesystem selector.
			}
		} catch (URISyntaxException e) {
			// This error can be ignored because we just won't set the filesystem selector
			// to a anything
		}
	}

	/**
	 * Update the URI field based on the selected path.
	 *
	 * @param selectedPath
	 */
	private void updateURIField(String selectedPath) {
		uriField.setText(TextProcessor.process(selectedPath));
		updateFilesystemSelector(selectedPath);
	}

	/**
	 * Create the file system selection area.
	 *
	 * @param composite
	 */
	private void createFileSystemSelection(Composite composite) {
		fileSystemSelectionArea = new FileSystemSelectionArea();
		fileSystemSelectionArea.createContents(composite);
	}

	/**
	 * Create the area for user entry.
	 *
	 * @param composite
	 * @param defaultEnabled
	 */
	private void createUserEntryArea(Composite composite, String uriLabelText, boolean defaultEnabled) {
		// location label
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		uriLabel = new Label(composite, SWT.NONE);
		if (uriLabelText != null) {
			uriLabel.setText(uriLabelText);
		} else {
			uriLabel.setText(ResourceSelectorWidgetMessages.uriLabelText);
		}

		// project location entry field
		uriField = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);

		data.horizontalSpan = 1;
		uriField.setLayoutData(data);

		// create a blank space to align the filesystem selector with the path box.
		new Label(composite, SWT.NONE);

		Composite browserComp = new Composite(composite, SWT.NONE);
		FillLayout browserLayout = new FillLayout(SWT.HORIZONTAL);
		browserComp.setLayout(browserLayout);

		createFileSystemSelection(browserComp);

		// browse button
		browseButton = new Button(browserComp, SWT.PUSH);
		browseButton.setText(BROWSE_LABEL);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleURIBrowseButtonPressed();
			}
		});

		uriField.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			@Override
			public void modifyText(ModifyEvent e) {
				updateFilesystemSelector(uriField.getText());
			}
		});
	}

	public ResourceSelectorWidget(Composite parent, ResourceType resourceType, int colSpan, String sectionLabelText, String uriLabelText) {
		this.resourceType = resourceType;
		this.sectionLabelText = sectionLabelText;
		mainComp = new Group(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 5;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		mainComp.setLayoutData(gd);
		mainComp.setText(sectionLabelText);
		createUserEntryArea(mainComp, uriLabelText, true);
	}

	public void setEnabled(boolean enabled) {
		if (mainComp != null) {
			mainComp.setEnabled(enabled);
		}
		if (uriLabel != null) {
			uriLabel.setEnabled(enabled);
		}
		if (browseButton != null) {
			browseButton.setEnabled(enabled);
		}
		if (uriField != null) {
			uriField.setEnabled(enabled);
		}
		if (fileSystemSelectionArea != null) {
			fileSystemSelectionArea.setEnabled(enabled);
		}
	}

	public Text getURIText() {
		return uriField;
	}
}
