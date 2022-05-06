/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.DOCKERFILE_NAME;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.DOCKER_CONNECTION;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.FORCE_RM_INTERMEDIATE_CONTAINERS;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.NO_CACHE;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.QUIET_BUILD;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.REPO_NAME;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.RM_INTERMEDIATE_CONTAINERS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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

public class BuildDockerImageLaunchConfigurationMainTab
		extends AbstractLaunchConfigurationTab {

	private final String TAB_NAME = "BuildDockerImageLaunchConfigurationMainTab.name"; //$NON-NLS-1$
	private final String CONNECTION_LABEL = "BuildDockerImageLaunchConfigurationMainTab.connection.group.label"; //$NON-NLS-1$
	private final String CONNECTION_TOOLTIP = "BuildDockerImageLaunchConfigurationMainTab.connection.group.tooltip"; //$NON-NLS-1$
	private final String CONNECTION_MISSING = "BuildDockerImageLaunchConfigurationMainTab.connection.missing"; //$NON-NLS-1$
	private final String BUILD_CONTEXT_PATH_LABEL = "BuildDockerImageLaunchConfigurationMainTab.buildContextPath.group.label"; //$NON-NLS-1$
	private final String BUILD_CONTEXT_PATH_MISSING = "BuildDockerImageLaunchConfigurationMainTab.buildContextPath.missing"; //$NON-NLS-1$
	private final String BUILD_CONTEXT_PATH_MISSING_DOCKERFILE = "BuildDockerImageLaunchConfigurationMainTab.buildContextPath.missingDockerfile"; //$NON-NLS-1$
	private final String BROWSE_WORKSPACE = "BuildDockerImageLaunchConfigurationMainTab.buildContextPath.browseworkspace.button.label"; //$NON-NLS-1$
	private final String BROWSE_WORKSPACE_DIALOG_TITLE = "BuildDockerImageLaunchConfigurationMainTab.buildContextPath.browseworkspace.dialog.title"; //$NON-NLS-1$
	private final String BROWSE_FILESYSTEM = "BuildDockerImageLaunchConfigurationMainTab.buildContextPath.browsefilesystem.button.label"; //$NON-NLS-1$
	private final String DOCKERFILE_NAME_LABEL = "BuildDockerImageLaunchConfigurationMainTab.dockerfileName.label"; //$NON-NLS-1$
	private final String REPO_NAME_LABEL = "BuildDockerImageLaunchConfigurationMainTab.repoName.label"; //$NON-NLS-1$
	private final String REPO_NAME_MISSING = "BuildDockerImageLaunchConfigurationMainTab.repoName.missing"; //$NON-NLS-1$
	private final String OPTIONS_LABEL = "BuildDockerImageLaunchConfigurationMainTab.options.group.label"; //$NON-NLS-1$
	private final String OPTION_QUIET_LABEL = "BuildDockerImageLaunchConfigurationMainTab.options.quiet.button.label"; //$NON-NLS-1$
	private final String OPTION_NOCACHE_LABEL = "BuildDockerImageLaunchConfigurationMainTab.options.noCache.button.label"; //$NON-NLS-1$
	private final String OPTION_RM_LABEL = "BuildDockerImageLaunchConfigurationMainTab.options.rm.button.label"; //$NON-NLS-1$
	private final String OPTION_FORCERM_LABEL = "BuildDockerImageLaunchConfigurationMainTab.options.forceRM.button.label"; //$NON-NLS-1$

	/** the Docker daemon to use for the image build. */
	private ComboViewer connectionSelectionComboViewer;
	/** the path to the build context . */
	private Text buildContextPathText;
	/** whether buildContextPath is relative */
	private boolean buildContextPathWorkspaceRelative;
	/** build option: name and optional tag. */
	private Text repoNameText;
	/** build option: dockerfile name. */
	private Text dockerfileNameText;
	/** build option: do not use cache. */
	private Button noCacheButton;
	/** build option: quiet mode. */
	private Button quietBuildButton;
	/** build option: remove intermediate after successful build only. */
	private Button removeIntermediateContainersButton;
	/** build option: always remove intermediate. */
	private Button alwaysRemoveIntermediateContainersButton;

	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
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
		connectionGroup.setText(LaunchMessages.getString(CONNECTION_LABEL));
		connectionGroup
				.setToolTipText(LaunchMessages.getString(CONNECTION_TOOLTIP));
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
		// build context path
		createBuildContextPathGroup(container);
		// repository name
		createRepoNameGroup(container);
		// dockerfile name
		createDockerfileNameGroup(container);
		// build options
		createBuildOptionsGroup(container);
	}

	private void createBuildContextPathGroup(final Composite container) {
		final Group buildContextPathLocationGroup = new Group(container,
				SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(buildContextPathLocationGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(3)
				.applyTo(buildContextPathLocationGroup);
		buildContextPathLocationGroup
				.setText(LaunchMessages.getString(BUILD_CONTEXT_PATH_LABEL));
		this.buildContextPathText = new Text(
				buildContextPathLocationGroup, SWT.BORDER);
		this.buildContextPathText
				.addModifyListener(new LaunchConfigurationChangeListener());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(3, 1)
				.applyTo(this.buildContextPathText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false)
				.applyTo(new Label(buildContextPathLocationGroup, SWT.NONE));
		final Button browseWorkspaceButton = new Button(
				buildContextPathLocationGroup, SWT.NONE);
		browseWorkspaceButton
				.setText(LaunchMessages.getString(BROWSE_WORKSPACE));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(browseWorkspaceButton);
		browseWorkspaceButton
				.addSelectionListener(onBrowseWorkspace(buildContextPathText, IContainer.class));
		final Button browseFileSystemButton = new Button(
				buildContextPathLocationGroup, SWT.NONE);
		browseFileSystemButton
				.setText(LaunchMessages.getString(BROWSE_FILESYSTEM));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(browseFileSystemButton);
		browseFileSystemButton.addSelectionListener(
				onBrowseFileSystemForDirectory(this.buildContextPathText));
	}

	private void createRepoNameGroup(final Composite container) {
		final Group repoNameGroup = new Group(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(repoNameGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(1)
				.applyTo(repoNameGroup);
		repoNameGroup.setText(LaunchMessages.getString(REPO_NAME_LABEL));

		this.repoNameText = new Text(repoNameGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(this.repoNameText);
		this.repoNameText
				.addModifyListener(new LaunchConfigurationChangeListener());
	}

	private void createDockerfileNameGroup(final Composite container) {
		final Group dockerfileNameGroup = new Group(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(dockerfileNameGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(1).applyTo(dockerfileNameGroup);
		dockerfileNameGroup.setText(LaunchMessages.getString(DOCKERFILE_NAME_LABEL));

		this.dockerfileNameText = new Text(dockerfileNameGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(this.dockerfileNameText);
		this.dockerfileNameText.addModifyListener(new LaunchConfigurationChangeListener());
	}

	private void createBuildOptionsGroup(final Composite container) {
		final Group optionsGroup = new Group(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(optionsGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(2)
				.applyTo(optionsGroup);
		optionsGroup.setText(LaunchMessages.getString(OPTIONS_LABEL));

		this.quietBuildButton = new Button(optionsGroup, SWT.CHECK);
		this.quietBuildButton
				.setText(LaunchMessages.getString(OPTION_QUIET_LABEL));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false).applyTo(this.quietBuildButton);
		this.quietBuildButton
				.addSelectionListener(new LaunchConfigurationChangeListener());

		this.noCacheButton = new Button(optionsGroup, SWT.CHECK);
		this.noCacheButton
				.setText(LaunchMessages.getString(OPTION_NOCACHE_LABEL));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false).applyTo(this.noCacheButton);
		this.noCacheButton
				.addSelectionListener(new LaunchConfigurationChangeListener());

		this.removeIntermediateContainersButton = new Button(optionsGroup,
				SWT.CHECK);
		this.removeIntermediateContainersButton
				.setText(LaunchMessages.getString(OPTION_RM_LABEL));
		this.removeIntermediateContainersButton
				.addSelectionListener(new LaunchConfigurationChangeListener());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false)
				.applyTo(this.removeIntermediateContainersButton);

		this.alwaysRemoveIntermediateContainersButton = new Button(optionsGroup,
				SWT.CHECK);
		this.alwaysRemoveIntermediateContainersButton
				.setText(LaunchMessages.getString(OPTION_FORCERM_LABEL));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false)
				.applyTo(this.alwaysRemoveIntermediateContainersButton);
		this.alwaysRemoveIntermediateContainersButton
				.addSelectionListener(onAlwaysRemoveIntermediateContainers());
		this.alwaysRemoveIntermediateContainersButton
				.addSelectionListener(new LaunchConfigurationChangeListener());
	}

	private SelectionListener onAlwaysRemoveIntermediateContainers() {
		return SelectionListener.widgetSelectedAdapter(
				e -> toggleRemoveIntermediateContainersButtonState());
	}

	/**
	 * Opens a dialog to browse the workspace
	 *
	 * @return
	 */
	private SelectionListener onBrowseWorkspace(final Text pathText,
			final Class<?> expectedType) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
					getShell(), new WorkbenchLabelProvider(),
					new WorkbenchContentProvider());
			dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
			dialog.setTitle(
					LaunchMessages.getString(BROWSE_WORKSPACE_DIALOG_TITLE));
			dialog.setComparator(
					new ResourceComparator(ResourceComparator.NAME));
			dialog.setAllowMultiple(false);
			dialog.setValidator(selection -> {
				if (selection.length == 1 && expectedType
						.isAssignableFrom(selection[0].getClass())) {
					return Status.OK_STATUS;
				}
				return Status.error(null);
			});
			if (dialog.open() == IDialogConstants.OK_ID) {
				final IResource selection = (IResource) dialog.getFirstResult();
				pathText.setText(selection.getFullPath().toOSString());
				buildContextPathWorkspaceRelative = true;
			}
		});
	}

	/**
	 * Opens a dialog to browse the file system and select a directory
	 *
	 * @return
	 */
	private SelectionListener onBrowseFileSystemForDirectory(final Text pathText) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final DirectoryDialog dialog = new DirectoryDialog(getShell());
			final String selection = dialog.open();
			if (selection != null) {
				pathText.setText(selection);
				buildContextPathWorkspaceRelative = false;
			}
		});
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
			this.buildContextPathText.setText(
					configuration.getAttribute(SOURCE_PATH_LOCATION, ""));
			this.buildContextPathWorkspaceRelative = configuration.getAttribute(
					SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION, false);
			this.dockerfileNameText.setText(configuration.getAttribute(DOCKERFILE_NAME, "Dockerfile")); //$NON-NLS-1$
			this.repoNameText
					.setText(configuration.getAttribute(REPO_NAME, ""));
			this.quietBuildButton.setSelection(
					configuration.getAttribute(QUIET_BUILD, false));
			this.noCacheButton
					.setSelection(configuration.getAttribute(NO_CACHE, false));
			this.removeIntermediateContainersButton.setSelection(configuration
					.getAttribute(RM_INTERMEDIATE_CONTAINERS, false));
			this.alwaysRemoveIntermediateContainersButton.setSelection(
					configuration.getAttribute(FORCE_RM_INTERMEDIATE_CONTAINERS,
							false));
			toggleRemoveIntermediateContainersButtonState();
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
					.getAttribute(SOURCE_PATH_LOCATION, ""); // $NON-NLS-1$
			final String dockerfileName = launchConfig.getAttribute(DOCKERFILE_NAME, "Dockerfile"); //$NON-NLS-1$
			final boolean sourcePathWorkspaceRelativeLocation = launchConfig.getAttribute(SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION, false);
			final IPath sourcePath = BuildDockerImageUtils.getPath(
					sourcePathLocation, sourcePathWorkspaceRelativeLocation);
			if (dockerConnection.isEmpty() || dockerConnection == null
					|| DockerConnectionManager.getInstance()
							.findConnection(dockerConnection) == null) {
				setErrorMessage(LaunchMessages.getString(CONNECTION_MISSING));
				return false;
			} else if (sourcePathLocation.isEmpty() || sourcePath == null) {
				setErrorMessage(
						LaunchMessages.getString(BUILD_CONTEXT_PATH_MISSING));
				return false;
			} else if (!sourcePath.append(dockerfileName).toFile().exists()) {
				setErrorMessage(LaunchMessages.getString(BUILD_CONTEXT_PATH_MISSING_DOCKERFILE));
				return false;
			} else {
				setErrorMessage(null);
			}
			final String repoName = launchConfig.getAttribute(REPO_NAME, ""); // $NON-NLS-1$
			if (repoName.isEmpty()) {
				setWarningMessage(LaunchMessages.getString(REPO_NAME_MISSING));
			} else {
				setWarningMessage(null);
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
		configuration.setAttribute(SOURCE_PATH_LOCATION,
				this.buildContextPathText.getText());
		configuration.setAttribute(SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION,
				this.buildContextPathWorkspaceRelative);
		if (!this.repoNameText.getText().isEmpty()) {
			configuration.setAttribute(REPO_NAME, this.repoNameText.getText());
		}
		if (!this.dockerfileNameText.getText().isEmpty()) {
			configuration.setAttribute(DOCKERFILE_NAME, this.dockerfileNameText.getText());
		}
		configuration.setAttribute(QUIET_BUILD,
				this.quietBuildButton.getSelection());
		configuration.setAttribute(NO_CACHE, this.noCacheButton.getSelection());
		configuration.setAttribute(RM_INTERMEDIATE_CONTAINERS,
				this.removeIntermediateContainersButton.getSelection());
		configuration.setAttribute(FORCE_RM_INTERMEDIATE_CONTAINERS,
				this.alwaysRemoveIntermediateContainersButton.getSelection());

	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

	/**
	 * Enables or disables the
	 * {@link BuildDockerImageLaunchConfigurationMainTab#removeIntermediateContainersButton}
	 * given the selection of
	 * {@link BuildDockerImageLaunchConfigurationMainTab#alwaysRemoveIntermediateContainersButton}
	 */
	private void toggleRemoveIntermediateContainersButtonState() {
		if (BuildDockerImageLaunchConfigurationMainTab.this.alwaysRemoveIntermediateContainersButton
				.getSelection()) {
			BuildDockerImageLaunchConfigurationMainTab.this.removeIntermediateContainersButton
					.setEnabled(false);
		} else {
			BuildDockerImageLaunchConfigurationMainTab.this.removeIntermediateContainersButton
					.setEnabled(true);
		}
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
