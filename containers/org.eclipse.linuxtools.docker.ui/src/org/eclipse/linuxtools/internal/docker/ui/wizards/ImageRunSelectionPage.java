/*******************************************************************************
 * Copyright (c) 2014, 2020 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ALLOCATE_PSEUDO_CONSOLE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.AUTO_REMOVE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.COMMAND;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENTRYPOINT;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.INTERACTIVE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.LINKS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.PRIVILEGED;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.PUBLISHED_PORTS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.PUBLISH_ALL_PORTS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.READONLY;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.UNUSED_PORTS;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.AbstractRegistry;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.DockerOperationCancelledException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.DefaultImagePullProgressHandler;
import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.linuxtools.internal.docker.ui.jobs.FindImageInfoRunnable;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ContainerLinkModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ExposedPortModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * A {@link WizardPage} to let the user select the {@link IDockerImage} to run
 * and select the most common arguments (container name, port settings, etc.)
 *
 * @author xcoulon
 *
 */
public class ImageRunSelectionPage extends WizardPage {

	private static final int INDENT = 30;

	private static final String ERROR_PULLING_IMAGE = "ImagePullError.msg"; //$NON-NLS-1$

	private final DataBindingContext dbc = new DataBindingContext();
	private final ImageRunSelectionModel model;

	private final ILaunchConfiguration lastLaunchConfiguration;

	private Link pullImageLink;

	private WritableValue<String> writeValue;
	private static final int COLUMNS = 3;

	/**
	 * Default constructor.
	 *
	 * @param selectedImage
	 *            the {@link IDockerImage} to run
	 * @param lastLaunchConfiguration
	 *            the last {@link ILaunchConfiguration} used to run this
	 *            {@link IDockerImage} or <code>null</code> if none exists.
	 *
	 */
	public ImageRunSelectionPage(final IDockerImage selectedImage,
			final ILaunchConfiguration lastLaunchConfiguration) {
		super("ImageSelectionPage", //$NON-NLS-1$
				WizardMessages.getString("ImageSelectionPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("ImageSelectionPage.runImage")); //$NON-NLS-1$
		setPageComplete(true);
		this.model = new ImageRunSelectionModel(selectedImage);
		this.lastLaunchConfiguration = lastLaunchConfiguration;
	}

	/**
	 * Default constructor.
	 *
	 * @param selectedConnection
	 *            the {@link IDockerConnection} to run
	 *
	 */
	public ImageRunSelectionPage(final IDockerConnection selectedConnection) {
		super("ImageSelectionPage", //$NON-NLS-1$
				WizardMessages.getString("ImageSelectionPage.exposedPortTitle"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages
				.getString("ImageRunSelectionPage.exposedPortMsg")); //$NON-NLS-1$
		setPageComplete(false);
		this.model = new ImageRunSelectionModel(selectedConnection);
		this.lastLaunchConfiguration = null;
	}

	/**
	 * @return the {@link ImageRunSelectionModel} model associated
	 */
	public ImageRunSelectionModel getModel() {
		return model;
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	@Override
	public void createControl(final Composite parent) {
		final ScrolledComposite scrollTop = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);
		final Composite container = new Composite(scrollTop, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(container);
		setDefaultValues();
		createImageSettingsSection(container);
		createSectionSeparator(container, true);
		createPortSettingsSection(container);
		// addSectionSeparator(container, false);
		createLinkSettingsSection(container);
		// addSectionSeparator(container, false);
		createRunOptionsSection(container);
		// Observe model changes to propagate to the UI via listeners.
		final IObservableValue imageSelectionObservable = BeanProperties
				.value(ImageRunSelectionModel.class,
						ImageRunSelectionModel.SELECTED_IMAGE_NAME)
				.observe(model);
		imageSelectionObservable
				.addValueChangeListener(onImageSelectionChange());
		writeValue = new WritableValue<>("", String.class); //$NON-NLS-1$
		// setup validation support
		WizardPageSupport.create(this, dbc);

		// set validation
		final ImageSelectionValidator imageSelectionValidator = new ImageSelectionValidator(
				imageSelectionObservable);
		imageSelectionObservable
			.addValueChangeListener(onImageSelectionChange());
		dbc.addValidationStatusProvider(imageSelectionValidator);
		imageSelectionObservable
				.addValueChangeListener(onImageSelectionChange());
		final IObservableValue containerNameObservable = BeanProperties
				.value(ImageRunSelectionModel.class,
						ImageRunSelectionModel.CONTAINER_NAME)
				.observe(model);
		final ContainerNameValidator containerNameValidator = new ContainerNameValidator(
				model.getSelectedConnection(), containerNameObservable);
		dbc.addValidationStatusProvider(containerNameValidator);
		// // force displaying the error message upon startup
		final Object containerstatus = containerNameValidator
				.getValidationStatus().getValue();
		// attach the Databinding context status to this wizard page.
		WizardPageSupport.create(this, this.dbc);
		setStatusMessage(containerstatus);

		scrollTop.setContent(container);
		Point point = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollTop.setSize(point);
		scrollTop.setMinSize(point);
		setControl(scrollTop);
	}

	private void setStatusMessage(final Object containerstatus) {
		if (containerstatus instanceof ValidationStatus) {
			final ValidationStatus validationStatus = (ValidationStatus) containerstatus;
			if (validationStatus.getSeverity() == IStatus.ERROR) {
				setMessage(validationStatus.getMessage(),
						IMessageProvider.ERROR);
			} else if (validationStatus.getSeverity() == IStatus.WARNING) {
				setMessage(validationStatus.getMessage(),
						IMessageProvider.WARNING);
			}
		} else if (containerstatus instanceof IStatus status) {
			if (status.getSeverity() == IStatus.ERROR) {
				setMessage(status.getMessage(), IMessageProvider.ERROR);
			} else
			if (status.getSeverity() == IStatus.WARNING) {
				setMessage(status.getMessage(), IMessageProvider.WARNING);
			}

		}
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
		// Image selection name
		final Label imageSelectionLabel = new Label(container, SWT.NONE);
		imageSelectionLabel.setText(
				WizardMessages.getString("ImageRunSelectionPage.imageName")); //$NON-NLS-1$
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
				.grab(false, false).span(1, 1).applyTo(searchImageButton);
		searchImageButton.addSelectionListener(onSearchImage());
		// link to pull image
		final Label fillerLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1).applyTo(fillerLabel);
		pullImageLink = new Link(container, SWT.NONE);
		pullImageLink.setText(
				WizardMessages.getString("ImageRunSelectionPage.pullImage")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(COLUMNS - 1, 1).applyTo(pullImageLink);
		pullImageLink.addSelectionListener(onPullImage());
		dbc.bindValue(WidgetProperties.enabled().observe(pullImageLink),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.SELECTED_IMAGE_NEEDS_PULLING)
						.observe(model));
		// bind combo with model (for values and selection)
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

	private void createPortSettingsSection(final Composite container) {
		final Button publishAllPortsButton = new Button(container, SWT.CHECK);
		publishAllPortsButton.setText(WizardMessages
				.getString("ImageRunSelectionPage.publishAllPorts")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(publishAllPortsButton);
		dbc.bindValue(
				WidgetProperties.widgetSelection().observe(publishAllPortsButton),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.PUBLISH_ALL_PORTS)
						.observe(model));
		// specify ports
		final Label portSettingsLabel = new Label(container, SWT.NONE);
		portSettingsLabel.setText(
				WizardMessages.getString("ImageRunSelectionPage.portSettings")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(COLUMNS, 1).indent(INDENT, 0)
				.applyTo(portSettingsLabel);
		final CheckboxTableViewer exposedPortsTableViewer = createPortSettingsTable(
				container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).span(COLUMNS - 1, 1).indent(INDENT, 0)
				.hint(200, 70).applyTo(exposedPortsTableViewer.getTable());
		// buttons
		final Composite buttonsContainers = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(false, false).applyTo(buttonsContainers);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.spacing(SWT.DEFAULT, 0).applyTo(buttonsContainers);

		final Button addButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(addButton);
		addButton
				.setText(WizardMessages
						.getString("ImageRunSelectionPage.addButton")); //$NON-NLS-1$
		addButton.addSelectionListener(onAddPort(exposedPortsTableViewer));
		final Button editButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(editButton);
		editButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.editButton")); //$NON-NLS-1$
		editButton.setEnabled(false);
		editButton.addSelectionListener(onEditPort(exposedPortsTableViewer));
		final Button removeButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(removeButton);
		removeButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.remove")); //$NON-NLS-1$
		removeButton
				.addSelectionListener(onRemovePorts(exposedPortsTableViewer));
		BeanProperties
				.value(ImageRunSelectionModel.class,
						ImageRunSelectionModel.PUBLISH_ALL_PORTS)
				.observe(model)
				.addValueChangeListener(onPublishAllPortsChange(
						exposedPortsTableViewer.getTable(), addButton,
						removeButton));
		ViewerSupport.bind(exposedPortsTableViewer, model.getExposedPorts(),
				BeanProperties.values(ExposedPortModel.class,
						ExposedPortModel.CONTAINER_PORT,
						ExposedPortModel.PORT_TYPE,
						ExposedPortModel.HOST_ADDRESS,
						ExposedPortModel.HOST_PORT));
		dbc.bindSet(ViewerProperties.checkedElements(ExposedPortModel.class).observe((Viewer) exposedPortsTableViewer),
				BeanProperties.set(ImageRunSelectionModel.SELECTED_PORTS).observe(model));

		// disable the edit and removeButton if the table is empty
		exposedPortsTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));

		togglePortMappingControls(exposedPortsTableViewer.getTable(), addButton,
				removeButton);
	}

	private ISelectionChangedListener onSelectionChanged(
			final Button... targetButtons) {
		return e -> {
			if (e.getSelection().isEmpty()) {
				setControlsEnabled(targetButtons, false);
			} else {
				setControlsEnabled(targetButtons, true);
			}
		};
	}

	private CheckboxTableViewer createPortSettingsTable(
			final Composite container) {
		final Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		final CheckboxTableViewer tableViewer = new CheckboxTableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageRunSelectionPage.containerPortColumn"), //$NON-NLS-1$
				100);
		createTableViewerColum(tableViewer,
				WizardMessages.getString("ImageRunSelectionPage.typeColumn"), //$NON-NLS-1$
				50);
		createTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageRunSelectionPage.hostAddressColumn"), //$NON-NLS-1$
				100);
		createTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageRunSelectionPage.hostPortColumn"), //$NON-NLS-1$
				100);
		tableViewer.setContentProvider(new ObservableListContentProvider<>());
		return tableViewer;
	}

	private TableViewerColumn createTableViewerColum(
			final TableViewer tableViewer, final String title,
			final int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		if (title != null) {
			column.setText(title);
		}
		column.setWidth(width);
		return viewerColumn;
	}

	private void createLinkSettingsSection(final Composite container) {
		final Label linksLabel = new Label(container, SWT.NONE);
		linksLabel.setText(
				WizardMessages.getString("ImageRunSelectionPage.links"));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(COLUMNS, 1).applyTo(linksLabel);
		final TableViewer linksTableViewer = createLinksTable(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, true)
				.span(COLUMNS - 1, 1).hint(200, 50).indent(INDENT, SWT.DEFAULT)
				.applyTo(linksTableViewer.getTable());
		// buttons
		final Composite buttonsContainers = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(false, false).applyTo(buttonsContainers);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.spacing(SWT.DEFAULT, 0).applyTo(buttonsContainers);

		final Button addButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(addButton);
		addButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.addButton")); //$NON-NLS-1$
		addButton.addSelectionListener(onAddLink());
		final Button editButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(editButton);
		editButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.editButton")); //$NON-NLS-1$
		editButton.setEnabled(false);
		editButton.addSelectionListener(onEditLink(linksTableViewer));
		final Button removeButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(removeButton);
		removeButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.remove")); //$NON-NLS-1$
		removeButton.addSelectionListener(onRemoveLinks(linksTableViewer));
		removeButton.setEnabled(false);
		ViewerSupport.bind(linksTableViewer, model.getLinks(),
				BeanProperties.values(ContainerLinkModel.class,
						ContainerLinkModel.CONTAINER_NAME,
						ContainerLinkModel.CONTAINER_ALIAS));
		// disable the edit and removeButton if the table is empty
		linksTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));

	}

	private TableViewer createLinksTable(final Composite container) {
		final Table table = new Table(container,
				SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		final TableViewer tableViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageRunSelectionPage.containerNameColumn"), //$NON-NLS-1$
				200);
		createTableViewerColum(tableViewer,
				WizardMessages.getString("ImageRunSelectionPage.aliasColumn"), //$NON-NLS-1$
				150);
		return tableViewer;
	}

	private SelectionListener onAddLink() {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ContainerLinkDialog dialog = new ContainerLinkDialog(
					getShell(), model.getSelectedConnection());
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				model.addLink(dialog.getContainerName(),
						dialog.getContainerAlias());
			}
		});
	}

	private SelectionListener onEditLink(final TableViewer linksTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = linksTableViewer
					.getStructuredSelection();

			final ContainerLinkModel selectedContainerLink = (ContainerLinkModel) selection
					.getFirstElement();
			final ContainerLinkDialog dialog = new ContainerLinkDialog(
					getShell(), model.getSelectedConnection(),
					selectedContainerLink);
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				selectedContainerLink
						.setContainerName(dialog.getContainerName());
				selectedContainerLink
						.setContainerAlias(dialog.getContainerAlias());
				linksTableViewer.refresh();
			}
		});
	}

	private SelectionListener onRemoveLinks(
			final TableViewer linksTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = linksTableViewer
					.getStructuredSelection();
			for (@SuppressWarnings("unchecked")
			Iterator<ContainerLinkModel> iterator = selection
					.iterator(); iterator.hasNext();) {
				model.removeLink(iterator.next());
			}
		});
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

		// seccomp:unconfined
		final Button unconfinedButton = new Button(container, SWT.CHECK);
		unconfinedButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.unconfined")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(unconfinedButton);
		dbc.bindValue(WidgetProperties.widgetSelection().observe(unconfinedButton),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.UNCONFINED)
						.observe(model));

		// readonly with tmpfs /run and /tmp
		final Button basicSecurityButton = new Button(container, SWT.CHECK);
		basicSecurityButton.setText(WizardMessages
				.getString("ImageRunSelectionPage.basicSecurity")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(basicSecurityButton);
		dbc.bindValue(WidgetProperties.widgetSelection().observe(basicSecurityButton),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.BASIC_SECURITY)
						.observe(model));
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
				model.setExposedPorts(
						Collections.emptyList());
				return;
			}
			final IDockerImageInfo selectedImageInfo = getImageInfo(
					selectedImage);
			applyImageInfo(selectedImageInfo);
		};
	}

	private SelectionListener onPullImage() {
		return SelectionListener
				.widgetDefaultSelectedAdapter(e -> pullSelectedImage());
	}

	private IValueChangeListener onPublishAllPortsChange(
			final Control... controls) {
		return event -> togglePortMappingControls(controls);
	}

	private SelectionListener onSearchImage() {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ImageSearch imageSearchWizard = new ImageSearch(
					ImageRunSelectionPage.this.model.getSelectedConnection(),
					ImageRunSelectionPage.this.model.getSelectedImageName(),
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

	private SelectionListener onAddPort(
			final CheckboxTableViewer exposedPortsTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ContainerPortDialog dialog = new ContainerPortDialog(
					getShell());
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				final ExposedPortModel port = dialog.getPort();
				port.setSelected(true);
				model.addAvailablePort(port);
				model.getSelectedPorts().add(port);
				exposedPortsTableViewer.setChecked(port, true);
			}
		});
	}

	private SelectionListener onEditPort(
			final CheckboxTableViewer exposedPortsTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = exposedPortsTableViewer
					.getStructuredSelection();
			final ExposedPortModel selectedContainerPort = (ExposedPortModel) selection
					.getFirstElement();
			final ContainerPortDialog dialog = new ContainerPortDialog(
					getShell(), selectedContainerPort);
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				final ExposedPortModel configuredPort = dialog.getPort();
				selectedContainerPort
						.setContainerPort(configuredPort.getContainerPort());
				selectedContainerPort
						.setHostAddress(configuredPort.getHostAddress());
				selectedContainerPort.setHostPort(configuredPort.getHostPort());
				exposedPortsTableViewer.refresh();
			}
		});
	}

	private SelectionListener onRemovePorts(
			final TableViewer portsTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = portsTableViewer
					.getStructuredSelection();
			for (@SuppressWarnings("unchecked")
			Iterator<ExposedPortModel> iterator = selection.iterator(); iterator
					.hasNext();) {
				final ExposedPortModel port = iterator.next();
				model.removeAvailablePort(port);
				model.getSelectedPorts().remove(port);
			}
		});
	}

	/**
	 * Sets the default values from the optional given {@link IDockerImage} and
	 * {@link ILaunchConfiguration} elements
	 */
	private void setDefaultValues() {
		final IDockerImage selectedImage = model.getSelectedImage();
		if (selectedImage == null) {
			return;
		}
		final IDockerImageInfo selectedImageInfo = getImageInfo(selectedImage);

		// skip if a previous launch configuration was provided
		if (this.lastLaunchConfiguration != null) {
			try {
				this.model.setContainerName(lastLaunchConfiguration
						.getAttribute(CONTAINER_NAME, ""));
				this.model.setEntrypoint(
						lastLaunchConfiguration.getAttribute(ENTRYPOINT, ""));
				this.model.setCommand(
						lastLaunchConfiguration.getAttribute(COMMAND, ""));
				this.model.setPublishAllPorts(lastLaunchConfiguration
						.getAttribute(PUBLISH_ALL_PORTS, false));
				final List<String> exposedPortInfos = lastLaunchConfiguration.getAttribute(PUBLISHED_PORTS,
						Collections.emptyList());
				final List<String> unusedPortInfos = lastLaunchConfiguration.getAttribute(UNUSED_PORTS,
						Collections.emptyList());
				// FIXME: handle the case where ports where added (and selected)
				// by the user.
				if (selectedImageInfo != null) {
					if (exposedPortInfos.isEmpty()) {
						final List<ExposedPortModel> exposedPorts = ExposedPortModel
								.fromStrings(selectedImageInfo.config().exposedPorts());
						model.setExposedPorts(exposedPorts);
						this.model.setSelectedPorts(new HashSet<>(exposedPorts));
					} else {
						final List<ExposedPortModel> exposedPorts = ExposedPortModel.fromStrings(exposedPortInfos);
						model.setExposedPorts(exposedPorts);
						this.model.setSelectedPorts(new HashSet<>(exposedPorts));
					}
					for (String port : unusedPortInfos) {
						ExposedPortModel portModel = ExposedPortModel.fromString(port);
						portModel.setSelected(false);
						model.addExposedPort(portModel);
					}
				}

				// links
				this.model.setLinks(lastLaunchConfiguration.getAttribute(LINKS, Collections.emptyList()));
				// other options
				this.model.setRemoveWhenExits(lastLaunchConfiguration
						.getAttribute(AUTO_REMOVE, false));
				this.model.setInteractiveMode(lastLaunchConfiguration
						.getAttribute(INTERACTIVE, false));
				this.model.setAllocatePseudoTTY(lastLaunchConfiguration
						.getAttribute(ALLOCATE_PSEUDO_CONSOLE, false));
				this.model.setPrivileged(lastLaunchConfiguration
						.getAttribute(PRIVILEGED, false));
				this.model.setBasicSecurity(
						lastLaunchConfiguration.getAttribute(READONLY, false));
			} catch (CoreException e) {
				Activator.log(e);
			}
		} else {
			applyImageInfo(selectedImageInfo);
		}
	}

	/**
	 * @param selectedImage
	 * @return the corresponding {@link IDockerImageInfo} or <code>null</code>
	 *         if something went wrong.
	 */
	private IDockerImageInfo getImageInfo(final IDockerImage selectedImage) {
		try {
			final FindImageInfoRunnable findImageInfoRunnable = new FindImageInfoRunnable(
					selectedImage);
			getContainer().run(true, true, findImageInfoRunnable);
			final IDockerImageInfo selectedImageInfo = findImageInfoRunnable
					.getResult();
			return selectedImageInfo;
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}
		return null;
	}

	private void applyImageInfo(final IDockerImageInfo selectedImageInfo) {
		if (selectedImageInfo != null && selectedImageInfo.config() != null) {
			final List<ExposedPortModel> exposedPorts = ExposedPortModel
					.fromStrings(selectedImageInfo.config().exposedPorts());
			model.setExposedPorts(exposedPorts);
			model.setEntrypoint(selectedImageInfo.config().entrypoint());
			model.setCommand(selectedImageInfo.config().cmd());
		}
	}

	private void togglePortMappingControls(final Control... controls) {
		for (Control control : controls) {
			if (model.isPublishAllPorts()) {
				control.setEnabled(false);
			} else {
				control.setEnabled(true);
			}
		}
	}

	private void pullSelectedImage() {
		try {
			getContainer().run(true, true, monitor -> {
				final IDockerConnection connection = model
						.getSelectedConnection();
				final String imageName = model.getSelectedImageName();
				monitor.beginTask(
						WizardMessages.getFormattedString(
								"ImageRunSelectionPage.pullingTask", imageName), //$NON-NLS-1$
						1);
				try {
					connection.pullImage(imageName,
							new DefaultImagePullProgressHandler(connection,
									imageName, monitor));
				} catch (final DockerOperationCancelledException e) {
					// Cancelled by user. Do nothing
				} catch (final DockerException e) {
					Display.getDefault().syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							DVMessages.getFormattedString(ERROR_PULLING_IMAGE,
									imageName),
							e.getMessage()));
				} finally {
					monitor.done();
					// refresh the widgets
					model.refreshImageNames();
					if (model.getImageNames().contains(imageName)) {
						model.setSelectedImageName(imageName);
						model.setSelectedImageNeedsPulling(false);
						// Force revalidation by changing writeValue which
						// is made to be a dependency of the ImageCombo
						// MultiValidator.
						Display.getDefault()
								.syncExec(() -> writeValue.setValue(Long.toString(System.currentTimeMillis())));
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}
	}

	private static void setControlsEnabled(final Control[] controls,
			final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

	private class ImageSelectionValidator extends MultiValidator {

		private final IObservableValue<String> imageSelectionObservable;

		ImageSelectionValidator(
				final IObservableValue<String> imageSelectionObservable) {
			this.imageSelectionObservable = imageSelectionObservable;
		}

		@Override
		protected IStatus validate() {
			ObservableTracker.getterCalled(writeValue);

			final String selectedImageName = imageSelectionObservable
					.getValue();
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

		@Override
		public IObservableList<IObservable> getTargets() {
			WritableList<IObservable> targets = new WritableList<>();
			targets.add(imageSelectionObservable);
			return targets;
		}

	}

	private class ContainerNameValidator extends MultiValidator {

		private final IDockerConnection connection;

		private final IObservableValue<String> containerNameObservable;

		ContainerNameValidator(final IDockerConnection connection,
				final IObservableValue<String> containerNameObservable) {
			this.connection = connection;
			this.containerNameObservable = containerNameObservable;
		}

		@Override
		protected IStatus validate() {
			final String containerName = containerNameObservable.getValue();

			for (IDockerContainer container : connection.getContainers()) {
				if (container.name().equals(containerName)) {
					return ValidationStatus.error(WizardMessages.getString(
							"ImageRunSelectionPage.containerWithSameName")); //$NON-NLS-1$
				}
			}
			return ValidationStatus.ok();
		}

		@Override
		public IObservableList<IObservable> getTargets() {
			WritableList<IObservable> targets = new WritableList<>();
			targets.add(containerNameObservable);
			return targets;
		}
	}

}
