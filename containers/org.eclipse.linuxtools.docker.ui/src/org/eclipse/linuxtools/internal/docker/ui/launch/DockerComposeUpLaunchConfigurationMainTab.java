/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.eclipse.linuxtools.internal.docker.ui.launch.IDockerComposeLaunchConfigurationConstants.DOCKER_CONNECTION;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IDockerComposeLaunchConfigurationConstants.WORKING_DIR;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IDockerComposeLaunchConfigurationConstants.WORKING_DIR_WORKSPACE_RELATIVE_LOCATION;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * The main launch configuration tab for the {@code docker-compose up} process.
 */
public class DockerComposeUpLaunchConfigurationMainTab
		extends AbstractLaunchConfigurationTab {

	/** the Docker daemon to use for the image build. */
	private ComboViewer connectionSelectionComboViewer;
	/** the path to the docker compose config file. */
	private Text dockerComposeWorkingDirPathText;
	/**
	 * whether path to the docker compose config file is relative to the
	 * workspace or not.
	 */
	private boolean dockerComposeFilePathWorkspaceRelative;

	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(container);
		setControl(container);

		// connection selection
		final Group connectionGroup = new Group(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(connectionGroup);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(6, 6)
				.applyTo(connectionGroup);
		connectionGroup.setText(LaunchMessages.getString(
				"DockerComposeUpLaunchConfigurationMainTab.connection.group.label")); //$NON-NLS-1$
		connectionGroup.setToolTipText(LaunchMessages.getString(
				"DockerComposeUpLaunchConfigurationMainTab.connection.group.tooltip")); //$NON-NLS-1$
		final Combo connectionSelectionCombo = new Combo(connectionGroup,
				SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(connectionSelectionCombo);
		this.connectionSelectionComboViewer = new ComboViewer(
				connectionSelectionCombo);
		this.connectionSelectionComboViewer
				.setContentProvider(new ArrayContentProvider());
		this.connectionSelectionComboViewer.setInput(
				DockerConnectionManager.getInstance().getConnectionNames());
		connectionSelectionCombo
				.addSelectionListener(new LaunchConfigurationChangeListener());
		// docker compose config file
		createDockerComposeWorkingDirLocationGroup(container);
	}

	private void createDockerComposeWorkingDirLocationGroup(final Composite container) {
		final Group dockerComposeWorkingDirLocationGroup = new Group(container,
				SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(dockerComposeWorkingDirLocationGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(3)
				.applyTo(dockerComposeWorkingDirLocationGroup);
		dockerComposeWorkingDirLocationGroup
				.setText(LaunchMessages.getString(
						"DockerComposeUpLaunchConfigurationMainTab.dockerComposePath.group.label")); //$NON-NLS-1$
		this.dockerComposeWorkingDirPathText = new Text(dockerComposeWorkingDirLocationGroup,
				SWT.BORDER);
		this.dockerComposeWorkingDirPathText
				.addModifyListener(new LaunchConfigurationChangeListener());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(3, 1)
				.applyTo(this.dockerComposeWorkingDirPathText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false)
				.applyTo(new Label(dockerComposeWorkingDirLocationGroup, SWT.NONE));
		final Button browseWorkspaceButton = new Button(
				dockerComposeWorkingDirLocationGroup, SWT.NONE);
		browseWorkspaceButton.setText(LaunchMessages.getString(
				"DockerComposeUpLaunchConfigurationMainTab.dockerComposePath.browseworkspace.button.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(browseWorkspaceButton);
		browseWorkspaceButton.addSelectionListener(
				onBrowseWorkspace(dockerComposeWorkingDirPathText, IContainer.class));
		final Button browseFileSystemButton = new Button(
				dockerComposeWorkingDirLocationGroup, SWT.NONE);
		browseFileSystemButton.setText(LaunchMessages.getString(
				"DockerComposeUpLaunchConfigurationMainTab.dockerComposePath.browsefilesystem.button.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(browseFileSystemButton);
		browseFileSystemButton.addSelectionListener(
				onBrowseFileSystemForDirectory(this.dockerComposeWorkingDirPathText));
	}

	/**
	 * Opens a dialog to browse the workspace
	 * 
	 * @return
	 */
	private SelectionListener onBrowseWorkspace(final Text pathText,
			final Class<?> expectedType) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
						getShell(), new WorkbenchLabelProvider(),
						new WorkbenchContentProvider());
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				dialog.setTitle(LaunchMessages.getString(
						"DockerComposeUpLaunchConfigurationMainTab.dockerComposePath.browseworkspace.dialog.title")); //$NON-NLS-1$
				dialog.setComparator(
						new ResourceComparator(ResourceComparator.NAME));
				dialog.setAllowMultiple(false);
				dialog.setValidator(selection -> {
					if (selection.length == 1 && expectedType
							.isAssignableFrom(selection[0].getClass())) {
						return new Status(IStatus.OK, Activator.PLUGIN_ID,
								null);
					}
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, null);
				});
				if (dialog.open() == IDialogConstants.OK_ID) {
					final IResource selection = (IResource) dialog
							.getFirstResult();
					pathText.setText(selection.getFullPath().toOSString());
					dockerComposeFilePathWorkspaceRelative = true;
				}
			}
		};
	}

	/**
	 * Opens a dialog to browse the file system and select a directory
	 * 
	 * @return
	 */
	private SelectionListener onBrowseFileSystemForDirectory(
			final Text pathText) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final DirectoryDialog dialog = new DirectoryDialog(getShell());
				final String selection = dialog.open();
				if (selection != null) {
					pathText.setText(selection);
					dockerComposeFilePathWorkspaceRelative = false;
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
			this.connectionSelectionComboViewer
					.setSelection(new StructuredSelection(
							configuration.getAttribute(DOCKER_CONNECTION, "")));
			this.dockerComposeWorkingDirPathText
					.setText(configuration.getAttribute(WORKING_DIR, ""));
			this.dockerComposeFilePathWorkspaceRelative = configuration
					.getAttribute(WORKING_DIR_WORKSPACE_RELATIVE_LOCATION,
							false);
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public boolean isValid(final ILaunchConfiguration launchConfig) {
		try {
			// verify the connection
			final String dockerConnection = launchConfig
					.getAttribute(DOCKER_CONNECTION, ""); // $NON-NLS-1$
			// verify the source path
			final String sourcePathLocation = launchConfig
					.getAttribute(WORKING_DIR, ""); // $NON-NLS-1$
			final boolean sourcePathWorkspaceRelativeLocation = launchConfig
					.getAttribute(WORKING_DIR_WORKSPACE_RELATIVE_LOCATION,
							false);
			final IPath sourcePath = BuildDockerImageUtils.getPath(
					sourcePathLocation, sourcePathWorkspaceRelativeLocation);
			if (dockerConnection.isEmpty() || dockerConnection == null
					|| DockerConnectionManager.getInstance()
							.findConnection(dockerConnection) == null) {
				setErrorMessage(LaunchMessages.getString(
						"DockerComposeUpLaunchConfigurationMainTab.connection.missing")); //$NON-NLS-1$
				return false;
			} else if (sourcePathLocation.isEmpty() || sourcePath == null) {
				setErrorMessage(LaunchMessages.getString(
						"DockerComposeUpLaunchConfigurationMainTab.dockerComposePath.missing")); //$NON-NLS-1$
				return false;
			} else if (!sourcePath.append("docker-compose.yml").toFile()
					.exists()) {
				setErrorMessage(LaunchMessages.getString(
						"DockerComposeUpLaunchConfigurationMainTab.dockerComposePath.missingDockerComposeFile")); //$NON-NLS-1$
				return false;
			} else {
				setErrorMessage(null);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return super.isValid(launchConfig);
	}

	@Override
	public void performApply(
			final ILaunchConfigurationWorkingCopy configuration) {
		final IStructuredSelection connectionSelection = (IStructuredSelection) this.connectionSelectionComboViewer
				.getSelection();
		if (connectionSelection.getFirstElement() != null) {
			configuration.setAttribute(DOCKER_CONNECTION,
					connectionSelection.getFirstElement().toString());
		}
		configuration.setAttribute(WORKING_DIR,
				this.dockerComposeWorkingDirPathText.getText());
		configuration.setAttribute(WORKING_DIR_WORKSPACE_RELATIVE_LOCATION,
				this.dockerComposeFilePathWorkspaceRelative);
	}

	@Override
	public String getName() {
		return LaunchMessages
				.getString("DockerComposeUpLaunchConfigurationMainTab.name"); //$NON-NLS-1$
	}

	private class LaunchConfigurationChangeListener extends SelectionAdapter
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
