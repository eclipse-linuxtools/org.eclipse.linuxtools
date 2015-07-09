/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.DOCKER_CONNECTION;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class BuildDockerImageLaunchConfigurationMainTab
		extends AbstractLaunchConfigurationTab {

	private final String TAB_NAME = "BuildDockerImageLaunchConfigurationMainTab.name"; //$NON-NLS-1$
	private final String SOURCE_PATH_LOCATION_LABEL = "BuildDockerImageLaunchConfigurationMainTab.sourcePathLocation.group.label"; //$NON-NLS-1$
	private final String CONNECTION_LABEL = "BuildDockerImageLaunchConfigurationMainTab.connection.group.label"; //$NON-NLS-1$
	private final String BROWSE_WORKSPACE = "BuildDockerImageLaunchConfigurationMainTab.browseworkspace.button.label"; //$NON-NLS-1$
	private final String BROWSE_WORKSPACE_DIALOG_TITLE = "BuildDockerImageLaunchConfigurationMainTab.browseworkspace.dialog.title"; //$NON-NLS-1$
	private final String BROWSE_WORKSPACE_DIALOG_MESSAGE = "BuildDockerImageLaunchConfigurationMainTab.browseworkspace.dialog.message"; //$NON-NLS-1$
	private final String BROWSE_FILESYSTEM = "BuildDockerImageLaunchConfigurationMainTab.browsefilesystem.button.label"; //$NON-NLS-1$

	private Text sourcePathLocationText;
	private boolean sourcePathWorkspaceRelativeLocation;
	private ComboViewer connectionSelectionComboViewer;

	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(container);
		setControl(container);

		// source path location
		final Group sourcePathLocationGroup = new Group(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(sourcePathLocationGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(3)
				.applyTo(sourcePathLocationGroup);
		sourcePathLocationGroup
				.setText(LaunchMessages.getString(SOURCE_PATH_LOCATION_LABEL));
		sourcePathLocationText = new Text(sourcePathLocationGroup, SWT.BORDER);
		sourcePathLocationText
				.addModifyListener(new LaunchConfigurationChangeListener());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(3, 1).applyTo(sourcePathLocationText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false)
				.applyTo(new Label(sourcePathLocationGroup, SWT.NONE));

		final Button browseWorkspaceButton = new Button(sourcePathLocationGroup,
				SWT.NONE);
		browseWorkspaceButton
				.setText(LaunchMessages.getString(BROWSE_WORKSPACE));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(browseWorkspaceButton);
		browseWorkspaceButton.addSelectionListener(onBrowseWorkspace());
		final Button browseFileSystemButton = new Button(
				sourcePathLocationGroup,
				SWT.NONE);
		browseFileSystemButton
				.setText(LaunchMessages.getString(BROWSE_FILESYSTEM));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(browseFileSystemButton);
		browseFileSystemButton.addSelectionListener(onBrowseFileSystem());

		// connection
		final Group connectionGroup = new Group(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(connectionGroup);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(6, 6)
				.applyTo(connectionGroup);
		connectionGroup.setText(LaunchMessages.getString(CONNECTION_LABEL));
		final Combo connectionSelectionCombo = new Combo(connectionGroup,
				SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(connectionSelectionCombo);
		connectionSelectionComboViewer = new ComboViewer(
				connectionSelectionCombo);
		connectionSelectionComboViewer
				.setContentProvider(new ArrayContentProvider());
		connectionSelectionComboViewer.setInput(getConnectionNames());
		connectionSelectionCombo.addSelectionListener(new LaunchConfigurationChangeListener());
	}

	private List<String> getConnectionNames() {
		final List<String> connectionNames = new ArrayList<>();
		for (IDockerConnection connection : DockerConnectionManager
				.getInstance().getConnections()) {
			connectionNames.add(connection.getName());
		}
		return connectionNames;
	}

	/**
	 * Opens a dialog to browse the workspace
	 * 
	 * @return
	 */
	private SelectionListener onBrowseWorkspace() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
						getShell(), new WorkbenchLabelProvider(),
						new WorkbenchContentProvider());
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				dialog.setTitle(LaunchMessages
						.getString(BROWSE_WORKSPACE_DIALOG_TITLE));
				dialog.setMessage(LaunchMessages
						.getString(BROWSE_WORKSPACE_DIALOG_MESSAGE));
				dialog.setComparator(
						new ResourceComparator(ResourceComparator.NAME));
				dialog.setAllowMultiple(false);
				dialog.setValidator(new ISelectionStatusValidator() {
					// only accept a single file as the valid selection
					@Override
					public IStatus validate(Object[] selection) {
						if (selection.length == 1
								&& selection[0] instanceof IContainer) {
							return new Status(IStatus.OK, Activator.PLUGIN_ID,
									null);
						}
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								null);
					}
				});
				if (dialog.open() == IDialogConstants.OK_ID) {
					final IResource selection = (IResource) dialog
							.getFirstResult();
					sourcePathLocationText
							.setText(selection.getFullPath().toOSString());
					sourcePathWorkspaceRelativeLocation = true;
				}
			}
		};
	}

	/**
	 * Opens a dialog to browse the file system
	 * 
	 * @return
	 */
	private SelectionListener onBrowseFileSystem() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final DirectoryDialog dialog = new DirectoryDialog(getShell());
				final String selection = dialog.open();
				if (selection != null) {
					sourcePathLocationText.setText(selection);
					sourcePathWorkspaceRelativeLocation = false;
				}
			}
		};

	}

	@Override
	public void setDefaults(
			final ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {
		try {
			this.sourcePathLocationText.setText(
					configuration.getAttribute(SOURCE_PATH_LOCATION, ""));
			this.sourcePathWorkspaceRelativeLocation = configuration
					.getAttribute(SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION, false);
			this.connectionSelectionComboViewer
					.setSelection(new StructuredSelection(
							configuration.getAttribute(DOCKER_CONNECTION, "")));
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			if (launchConfig.getAttribute(SOURCE_PATH_LOCATION, "").isEmpty()) {
				return false;
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return super.isValid(launchConfig);
	}

	@Override
	public void performApply(
			final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(SOURCE_PATH_LOCATION,
				this.sourcePathLocationText.getText());
		configuration.setAttribute(SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION,
				this.sourcePathWorkspaceRelativeLocation);
		final IStructuredSelection connectionSelection = (IStructuredSelection) this.connectionSelectionComboViewer
				.getSelection();
		if (connectionSelection.getFirstElement() != null) {
			configuration.setAttribute(DOCKER_CONNECTION,
					connectionSelection.getFirstElement().toString());
		}
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

	protected class LaunchConfigurationChangeListener extends SelectionAdapter
			implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}
	}
}
