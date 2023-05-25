/*******************************************************************************
 * Copyright (c) 2015, 2023 Red Hat Inc. and others.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.linuxtools.docker.core.AbstractRegistry;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.linuxtools.internal.docker.ui.utils.IRunnableWithResult;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageNameValidator;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunNetworkModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ExposedPortModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RunImageMainTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "RunMainTab.name"; //$NON-NLS-1$
	// private static final String ERROR_PULLING_IMAGE = "ImagePullError.msg";
	// //$NON-NLS-1$

	private static final int COLUMNS = 3;

	private final DataBindingContext dbc = new DataBindingContext();
	private final ImageRunSelectionModel model;
	private final ImageRunResourceVolumesVariablesModel volumesModel;
	private final ImageRunNetworkModel networkModel;

	public RunImageMainTab(ImageRunSelectionModel model,
			ImageRunResourceVolumesVariablesModel volumesModel,
			ImageRunNetworkModel networkModel) {
		this.model = model;
		this.volumesModel = volumesModel;
		this.networkModel = networkModel;
	}

	public ImageRunSelectionModel getModel() {
		return model;
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

	@Override
	public void createControl(Composite parent) {
 		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(container);
		if (model == null) {
			setErrorMessage(LaunchMessages.getString("NoConnectionError.msg"));
			return;
		} else {
			setErrorMessage(null);
			setDefaultValues();
			createImageSettingsSection(container);
			createSectionSeparator(container, true);
			createRunOptionsSection(container);
			final IObservableValue connectionSelectionObservable = BeanProperties
					.value(ImageRunSelectionModel.class,
							ImageRunSelectionModel.SELECTED_CONNECTION_NAME)
					.observe(model);
			connectionSelectionObservable
					.addValueChangeListener(onConnectionSelectionChange());
			final IObservableValue imageSelectionObservable = BeanProperties
					.value(ImageRunSelectionModel.class,
							ImageRunSelectionModel.SELECTED_IMAGE_NAME)
					.observe(model);
			imageSelectionObservable
					.addValueChangeListener(onImageSelectionChange());
		}
		setControl(container);
	}

	private void setDefaultValues() {
		final IDockerImage selectedImage = model.getSelectedImage();
		if (selectedImage == null) {
			return;
		}
		findImageInfo(selectedImage);
	}

	private void createSectionSeparator(final Composite container,
			final boolean separator) {
		final int SECTION_INDENT = 10;
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.indent(SWT.DEFAULT, SECTION_INDENT)
				.applyTo(new Label(container, separator
						? (SWT.SEPARATOR | SWT.HORIZONTAL) : SWT.NONE));
	}

	/**
	 * Creates the {@link Composite} container that will display widgets to
	 * select an {@link IDockerImage}, name it and specify the command to run.
	 *
	 * @param container
	 *            the parent {@link Composite}
	 */
	private void createImageSettingsSection(final Composite container) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1)
				.applyTo(new Label(container, SWT.NONE));
		final Label connectionSelectionLabel = new Label(container, SWT.NONE);
		connectionSelectionLabel
				.setText(WizardMessages.getString("Connection.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(connectionSelectionLabel);
		final Combo connectionSelectionCombo = new Combo(container, SWT.BORDER);
		connectionSelectionCombo.setToolTipText(
				LaunchMessages.getString("RunMainTabSelectConnection.tooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(connectionSelectionCombo);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.applyTo(new Label(container, SWT.NONE));
		new ControlDecoration(connectionSelectionCombo, SWT.TOP | SWT.LEFT);
		final ComboViewer connectionSelectionComboViewer = new ComboViewer(
				connectionSelectionCombo);
		connectionSelectionComboViewer
				.setContentProvider(new ArrayContentProvider());
		connectionSelectionComboViewer.setInput(DockerConnectionManager
				.getInstance().getConnectionNames().toArray());
		dbc.bindValue(
				WidgetProperties.widgetSelection().observe(connectionSelectionCombo),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.SELECTED_CONNECTION_NAME)
						.observe(model));
		// Image selection name
		final Label imageSelectionLabel = new Label(container, SWT.NONE);
		imageSelectionLabel.setText(WizardMessages.getString("Image.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageSelectionLabel);
		final Combo imageSelectionCombo = new Combo(container, SWT.BORDER);
		final ComboViewer imageSelectionComboViewer = new ComboViewer(
				imageSelectionCombo);
		imageSelectionCombo.setToolTipText(WizardMessages
				.getString("ImageRunSelectionPage.selectTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(imageSelectionCombo);
		new ControlDecoration(imageSelectionCombo, SWT.TOP | SWT.LEFT);
		new ContentProposalAdapter(imageSelectionCombo,
				new ComboContentAdapter() {
					@Override
					public void insertControlContents(Control control,
							String text, int cursorPosition) {
						final Combo combo = (Combo) control;
						final Point selection = combo.getSelection();
						combo.setText(text);
						selection.x = text.length();
						selection.y = selection.x;
						combo.setSelection(selection);
					}
				}, getImageNameContentProposalProvider(imageSelectionCombo),
				null, null);
		// image search
		final Button searchImageButton = new Button(container, SWT.NONE);
		searchImageButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.search")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.hint(LaunchConfigurationUtils
						.getButtonWidthHint(searchImageButton), SWT.DEFAULT)
				.applyTo(searchImageButton);
		searchImageButton.addSelectionListener(onSearchImage());
		imageSelectionComboViewer
				.setContentProvider(new ObservableListContentProvider<>());
		dbc.bindList(WidgetProperties.items().observe(imageSelectionCombo),
				BeanProperties
						.list(ImageRunSelectionModel.class,
								ImageRunSelectionModel.IMAGE_NAMES)
						.observe(model));
		dbc.bindValue(WidgetProperties.widgetSelection().observe(imageSelectionCombo),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.SELECTED_IMAGE_NAME)
						.observe(model));
		// Container name (optional)
		final Label containerNameLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageSelectionLabel);
		containerNameLabel.setText(WizardMessages
				.getString("ImageRunSelectionPage.containerName")); //$NON-NLS-1$
		final Text containerNameText = new Text(container, SWT.BORDER);
		containerNameText.setToolTipText(WizardMessages
				.getString("ImageRunSelectionPage.containerTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(containerNameText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.applyTo(new Label(container, SWT.NONE));
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(containerNameText),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.CONTAINER_NAME)
						.observe(model));
		// EntryPoint (optional)
		final Label entrypointLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageSelectionLabel);
		entrypointLabel.setText(
				WizardMessages.getString("ImageRunSelectionPage.entrypoint")); //$NON-NLS-1$
		// TODO: include SWT.SEARCH | SWT.ICON_SEARCH to support value reset
		final Text entrypointText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(entrypointText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.applyTo(new Label(container, SWT.NONE));
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(entrypointText),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.ENTRYPOINT)
						.observe(model));

		// Command (optional)
		final Label commandLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageSelectionLabel);
		commandLabel.setText(
				WizardMessages.getString("ImageRunSelectionPage.command")); //$NON-NLS-1$
		final Text commandText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(commandText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.applyTo(new Label(container, SWT.NONE));
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(commandText),
				BeanProperties.value(ImageRunSelectionModel.class,
						ImageRunSelectionModel.COMMAND).observe(model));
	}

	/**
	 * Creates an {@link IContentProposalProvider} to propose
	 * {@link IDockerImage} names based on the current text.
	 *
	 * @param items
	 * @return
	 */
	private IContentProposalProvider getImageNameContentProposalProvider(
			final Combo imageSelectionCombo) {
		return (contents, position) -> {
			final List<IContentProposal> proposals = new ArrayList<>();
			for (String imageName : imageSelectionCombo.getItems()) {
				if (imageName.contains(contents)) {
					proposals.add(new ContentProposal(imageName, imageName,
							imageName, position));
				}
			}
			return proposals.toArray(new IContentProposal[0]);
		};
	}

	private IValueChangeListener onImageSelectionChange() {
		return event -> {
			final IDockerImage selectedImage = model.getSelectedImage();
			// skip if the selected image does not exist in the local Docker
			// host
			if (selectedImage == null) {
				model.setExposedPorts(new ArrayList<>());
				return;
			}
			findImageInfo(selectedImage);
			volumesModel.setSelectedImage(selectedImage);
		};
	}

	private IValueChangeListener onConnectionSelectionChange() {
		return event -> {
			// do this first as we might return and not reset connection
			networkModel.setConnection(model.getSelectedConnection());
			IDockerImage selectedImage = model.getSelectedImage();
			// skip if the selected image does not exist in the local Docker
			// host
			if (selectedImage == null) {
				List<String> imageNames = model.getImageNames();
				if (imageNames.size() > 0) {
					model.setSelectedImageName(imageNames.get(0));
					selectedImage = model.getSelectedImage();
				} else {
					model.setExposedPorts(new ArrayList<>());
					return;
				}
			}
			findImageInfo(selectedImage);
			volumesModel.setSelectedImage(selectedImage);
		};
	}

	private static final class FindImageInfoRunnable
			implements IRunnableWithResult<IDockerImageInfo> {
		private final IDockerImage selectedImage;
		private IDockerImageInfo selectedImageInfo;

		private FindImageInfoRunnable(IDockerImage selectedImage) {
			this.selectedImage = selectedImage;
		}

		@Override
		public void run(final IProgressMonitor monitor) {
			selectedImageInfo = selectedImage.getConnection()
					.getImageInfo(selectedImage.id());
		}

		@Override
		public IDockerImageInfo getResult() {
			return selectedImageInfo;
		}
	}

	private void findImageInfo(final IDockerImage selectedImage) {
		try {
			final FindImageInfoRunnable findImageInfoRunnable = new FindImageInfoRunnable(
					selectedImage);
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					this.getShell());
			dialog.run(true, true, findImageInfoRunnable);
			final IDockerImageInfo selectedImageInfo = findImageInfoRunnable
					.getResult();
			final Set<String> exposedPortInfos = selectedImageInfo.config()
					.exposedPorts();
			final List<ExposedPortModel> availablePorts = new ArrayList<>();
			if (exposedPortInfos != null) {
				for (String exposedPortInfo : exposedPortInfos) {
					final String privatePort = exposedPortInfo.substring(0,
							exposedPortInfo.indexOf('/'));
					final String type = exposedPortInfo
							.substring(exposedPortInfo.indexOf('/')); // $NON-NLS-1$
					final ExposedPortModel exposedPort = new ExposedPortModel(
							privatePort, type, "", privatePort);
					availablePorts.add(exposedPort); // $NON-NLS-1$
				}
			}
			model.setExposedPorts(availablePorts);
			model.setEntrypoint(selectedImageInfo.config().entrypoint());
			model.setCommand(selectedImageInfo.config().cmd());

		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}
	}

	private void createRunOptionsSection(final Composite container) {
		// interactive/show in console mode
		final Button interactiveButton = new Button(container, SWT.CHECK);
		interactiveButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.openStdin")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(interactiveButton);
		dbc.bindValue(WidgetProperties.widgetSelection().observe(interactiveButton),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.INTERACTIVE_MODE)
						.observe(model));
		// allocate pseudo-TTY
		final Button allocatePseudoTTY = new Button(container, SWT.CHECK);
		allocatePseudoTTY
				.setText(WizardMessages.getString("ImageRunSelectionPage.tty")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(allocatePseudoTTY);
		dbc.bindValue(WidgetProperties.widgetSelection().observe(allocatePseudoTTY),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.ALLOCATE_PSEUDO_TTY)
						.observe(model));

		// remove when exits
		final Button removeWhenExitsButton = new Button(container, SWT.CHECK);
		removeWhenExitsButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.autoRemove")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(removeWhenExitsButton);
		dbc.bindValue(
				WidgetProperties.widgetSelection().observe(removeWhenExitsButton),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.REMOVE_WHEN_EXITS)
						.observe(model));

		// privileged
		final Button privilegedButton = new Button(container, SWT.CHECK);
		privilegedButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.privileged")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(privilegedButton);
		dbc.bindValue(WidgetProperties.widgetSelection().observe(privilegedButton),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.PRIVILEGED)
						.observe(model));
	}

	private SelectionListener onSearchImage() {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ImageSearch imageSearchWizard = new ImageSearch(
					RunImageMainTab.this.model.getSelectedConnection(),
					RunImageMainTab.this.model.getSelectedImageName(),
					new RegistryInfo(AbstractRegistry.DOCKERHUB_REGISTRY,
							true));
			final boolean completed = CommandUtils.openWizard(imageSearchWizard,
					getShell());
			if (completed) {
				model.setSelectedImageName(
						imageSearchWizard.getSelectedImage());
			}
		});
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_MAIN_TAB);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		// Don't attempt to continue if model is null since it means there are
		// no connections
		if (model == null)
			return;
		try {
			final String defaultConnectionName = DockerConnectionManager
					.getInstance().getAllConnections().stream()
					.filter(connection -> connection.isOpen())
					.map(connection -> connection.getName()).findFirst()
					.orElse(""); //$NON-NLS-1$

			// Mark in error if there is no active connection
			if (defaultConnectionName.equals("")) { //$NON-NLS-1$
				setErrorMessage(WizardMessages
						.getString("ErrorNoActiveConnection.msg")); //$NON-NLS-1$
			}
			String connectionName = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
					defaultConnectionName);
			model.setSelectedConnectionName(connectionName);
			String imageName = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME, ""); //$NON-NLS-1$
			model.setSelectedImageName(imageName);
			String entryPoint = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.ENTRYPOINT, ""); //$NON-NLS-1$
			model.setEntrypoint(entryPoint);
			String command = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.COMMAND, ""); //$NON-NLS-1$
			model.setCommand(command);
			String containerName = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME,
					""); //$NON-NLS-1$
			model.setContainerName(containerName);
			boolean removeContainer = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.AUTO_REMOVE,
					false);
			model.setRemoveWhenExits(removeContainer);
			boolean interactive = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.INTERACTIVE,
					false);
			model.setInteractiveMode(interactive);
			boolean useTTY = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.ALLOCATE_PSEUDO_CONSOLE,
					false);
			model.setAllocatePseudoTTY(useTTY);
			boolean privileged = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.PRIVILEGED,
					false);
			model.setPrivileged(privileged);
		} catch (CoreException e) {
			Activator.logErrorMessage(
					LaunchMessages.getString(
							"RunDockerImageLaunchConfiguration.load.failure"), //$NON-NLS-1$
					e);
		}
		// update the underlying launch config working copy on model
		// changes.
		model.addPropertyChangeListener(
				new LaunchConfigurationChangeListener());

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (model == null)
			return;
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				model.getSelectedConnectionName());
		if (model.getSelectedImage() != null) {
			configuration.setAttribute(
					IRunDockerImageLaunchConfigurationConstants.IMAGE_ID,
					model.getSelectedImage().id());
			configuration.setAttribute(
					IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME,
					model.getSelectedImageName());
		}
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.COMMAND,
				model.getCommand());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.ENTRYPOINT,
				"".equals(model.getEntrypoint()) ? null //$NON-NLS-1$
						: model.getEntrypoint());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME,
				"".equals(model.getContainerName()) ? null //$NON-NLS-1$
						: model.getContainerName());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.ALLOCATE_PSEUDO_CONSOLE,
				model.isAllocatePseudoTTY());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.AUTO_REMOVE,
				model.isRemoveWhenExits());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.INTERACTIVE,
				model.isInteractiveMode());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.PRIVILEGED,
				model.isPrivileged());
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			if (model == null)
				return false;
			String connectionName = launchConfig.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
					""); //$NON-NLS-1$
			if (connectionName.isEmpty()) {
				return false;
			} else {
				// Verify Connection is active
				IDockerConnection connection = DockerConnectionManager
						.getInstance().findConnection(connectionName);
				if (connection == null || !connection.isOpen()) {
					setErrorMessage(WizardMessages
							.getFormattedString("ErrorInactiveConnection.msg", //$NON-NLS-1$
									connectionName));
					return false;
				}
			}
			final IStatus imageSelectionValidationStatus = new ImageSelectionValidator()
					.validate(model.getSelectedImageName());
			if (!imageSelectionValidationStatus.isOK()) {
				setErrorMessage(imageSelectionValidationStatus.getMessage());
				return false;
			}
			final IStatus imageNameValidationStatus = new ImageNameValidator()
					.validate(model.getSelectedImageName());
			if (!imageNameValidationStatus.isOK()) {
				setErrorMessage(imageNameValidationStatus.getMessage());
				return false;
			}
			final IStatus containerNameValidationStatus = new ContainerNameValidator()
					.validate(model.getContainerName());
			if (!containerNameValidationStatus.isOK()) {
				setErrorMessage(containerNameValidationStatus.getMessage());
				return false;
			}

		} catch (CoreException e) {
			Activator.log(e);
		}
		setErrorMessage(null);
		return true;
	}

	private class LaunchConfigurationChangeListener
			implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			updateLaunchConfigurationDialog();
		}
	}

	private class ImageSelectionValidator implements IValidator<String> {

		@Override
		public IStatus validate(final String selectedImageName) {
			if (selectedImageName.isEmpty()) {
				model.setSelectedImageNeedsPulling(false);
				return ValidationStatus.error(WizardMessages
						.getString("ImageRunSelectionPage.specifyImageMsg")); //$NON-NLS-1$
			}
			if (model.getSelectedImage() != null) {
				model.setSelectedImageNeedsPulling(false);
				return ValidationStatus.ok();
			}
			model.setSelectedImageNeedsPulling(true);
			return ValidationStatus.warning(WizardMessages.getFormattedString(
					"ImageRunSelectionPage.imageNotFoundMessage", //$NON-NLS-1$
					selectedImageName));
		}

	}

	private class ContainerNameValidator implements IValidator<String> {

		@Override
		public IStatus validate(String containerName) {

			for (IDockerContainer container : model.getSelectedConnection()
					.getContainers()) {
				if (container.name().equals(containerName)) {
					return ValidationStatus.error(WizardMessages.getString(
							"ImageRunSelectionPage.containerWithSameName")); //$NON-NLS-1$
				}
			}
			return ValidationStatus.ok();
		}

	}

}
