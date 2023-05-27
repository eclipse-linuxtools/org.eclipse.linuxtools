/*******************************************************************************
 * Copyright (c) 2015, 2019 Red Hat Inc. and others.
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

import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CPU_PRIORITY;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.DATA_VOLUMES;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENABLE_LIMITS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENV_VARIABLES;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.LABELS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.MEMORY_LIMIT;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.jobs.FindImageInfoRunnable;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel.MountType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * A {@link WizardPage} to let the user select the CPU and memory allocation for
 * the container, as well as the volumes to mount.
 *
 * @author xcoulon
 *
 */
public class ImageRunResourceVolumesVariablesPage extends WizardPage {

	private final int COLUMNS = 2;

	private final DataBindingContext dbc = new DataBindingContext();
	private final ImageRunResourceVolumesVariablesModel model;
	private final ILaunchConfiguration lastLaunchConfiguration;

	/**
	 * Default constructor.
	 *
	 * @param selectedImage
	 *            the {@link IDockerImage} to run
	 * @throws DockerException
	 *             if obtaining info from the given {@link IDockerConnection}
	 *             failed
	 *
	 */
	public ImageRunResourceVolumesVariablesPage(
			final IDockerConnection connection) throws DockerException {
		super("ImageSelectionPage2", //$NON-NLS-1$
				WizardMessages.getString("ImageRunResourceVolVarPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setPageComplete(true);
		this.model = new ImageRunResourceVolumesVariablesModel(connection);
		this.lastLaunchConfiguration = null;
	}

	/**
	 * Default constructor.
	 *
	 * @param selectedImage
	 *            the {@link IDockerImage} to run
	 * @param lastLaunchConfiguration
	 *            the last {@link ILaunchConfiguration} used to run this
	 *            {@link IDockerImage} or <code>null</code> if none exists.
	 * @throws DockerException
	 *             if obtaining info from the given {@link IDockerConnection}
	 *             failed
	 * @throws CoreException
	 *             if reading attributes from {@link ILaunchConfiguration}
	 *             failed
	 *
	 */
	public ImageRunResourceVolumesVariablesPage(final IDockerImage image,
			final ILaunchConfiguration lastLaunchConfiguration)
					throws DockerException {
		super("ImageSelectionPage2", //$NON-NLS-1$
				WizardMessages.getString("ImageRunResourceVolVarPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setPageComplete(true);
		this.model = new ImageRunResourceVolumesVariablesModel(
image);
		this.lastLaunchConfiguration = lastLaunchConfiguration;
	}

	public ImageRunResourceVolumesVariablesModel getModel() {
		return model;
	}

	@Override
	public void createControl(Composite parent) {
		final ScrolledComposite scrollTop = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);
		final Composite container = new Composite(scrollTop, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.applyTo(container);
		createVolumeSettingsContainer(container);
		// createSectionSeparator(container, true);
		createEnvironmentVariablesContainer(container);
		createLabelVariablesContainer(container);
		createSectionSeparator(container, true);
		createResourceSettingsContainer(container);
		setDefaultValues();

		scrollTop.setContent(container);
		Point point = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollTop.setSize(point);
		scrollTop.setMinSize(point);
		// TODO: Workaround https://bugs.eclipse.org/bugs/show_bug.cgi?id=487160
		setControl(scrollTop);
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

	private void createResourceSettingsContainer(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().spacing(10, 2).applyTo(container);
		final Button enableResourceLimitationButton = new Button(container,
				SWT.CHECK);
		enableResourceLimitationButton.setText(WizardMessages.getString(
				"ImageRunResourceVolVarPage.enableLimitationButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(enableResourceLimitationButton);
		final int COLUMNS = 5;
		final int INDENT = 20;
		final Composite subContainer = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.indent(INDENT, 0).span(COLUMNS, 1).grab(true, false)
				.applyTo(subContainer);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(subContainer);

		// specify CPU limitation
		final Label cpuPriorityLabel = new Label(subContainer, SWT.NONE);
		cpuPriorityLabel.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.cpuPriorityLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(cpuPriorityLabel);
		final Button lowCPULimitationButton = new Button(subContainer,
				SWT.RADIO);
		bindButton(lowCPULimitationButton,
				ImageRunResourceVolumesVariablesModel.CPU_LOW);
		lowCPULimitationButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.lowButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(lowCPULimitationButton);
		final Button mediumCPULimitationButton = new Button(subContainer,
				SWT.RADIO);
		mediumCPULimitationButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.mediumButton")); //$NON-NLS-1$
		bindButton(mediumCPULimitationButton,
				ImageRunResourceVolumesVariablesModel.CPU_MEDIUM);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(mediumCPULimitationButton);
		final Button highCPULimitationButton = new Button(subContainer,
				SWT.RADIO);
		highCPULimitationButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.highButton")); //$NON-NLS-1$
		bindButton(highCPULimitationButton,
				ImageRunResourceVolumesVariablesModel.CPU_HIGH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.applyTo(highCPULimitationButton);

		// Memory limitation
		final Label memoryLimitLabel = new Label(subContainer, SWT.NONE);
		memoryLimitLabel.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.memoryLimit")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(memoryLimitLabel);
		final Scale memoryLimitSpinner = new Scale(subContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(2, 1).applyTo(memoryLimitSpinner);
		memoryLimitSpinner.setBackground(
				Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
		memoryLimitSpinner.setMinimum(0);
		memoryLimitSpinner.setMaximum(this.model.getTotalMemory());
		memoryLimitSpinner.setPageIncrement(64);
		dbc.bindValue(WidgetProperties.widgetSelection().observe(memoryLimitSpinner),
				BeanProperties
						.value(ImageRunResourceVolumesVariablesModel.class,
								ImageRunResourceVolumesVariablesModel.MEMORY_LIMIT)
						.observe(model));

		final Text memoryLimitValueText = new Text(subContainer, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).hint(50, SWT.DEFAULT)
				.applyTo(memoryLimitValueText);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(memoryLimitValueText),
				BeanProperties
						.value(ImageRunResourceVolumesVariablesModel.class,
								ImageRunResourceVolumesVariablesModel.MEMORY_LIMIT)
						.observe(model));
		final Label memoryLimitValueLabel = new Label(subContainer, SWT.NONE);
		memoryLimitValueLabel.setText("MB"); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(memoryLimitValueLabel);

		// enable/disable controls
		final IObservableValue<?> enableResourceLimitationsObservable = BeanProperties
				.value(ImageRunResourceVolumesVariablesModel.class,
						ImageRunResourceVolumesVariablesModel.ENABLE_RESOURCE_LIMITATIONS)
				.observe(model);
		dbc.bindValue(
				WidgetProperties.widgetSelection()
						.observe(enableResourceLimitationButton),
				enableResourceLimitationsObservable);
		enableResourceLimitationsObservable
				.addChangeListener(onEnableResourceLimitation(subContainer));
		toggleResourceLimitationControls(subContainer);

	}

	/**
	 * Binds the given <code>cpuShares</code> value to the given {@link Button}
	 * when it is selected.
	 *
	 * @param button
	 *            the {@link Button} to bind
	 * @param cpuShares
	 *            the <code>cpuShares</code> to bind to the {@link Button}
	 * @return
	 */
	private Binding bindButton(final Button button, final long cpuShares) {
		return dbc.bindValue(WidgetProperties.widgetSelection().observe(button),
				BeanProperties
						.value(ImageRunResourceVolumesVariablesModel.class,
								ImageRunResourceVolumesVariablesModel.CPU_SHARE_WEIGHT)
						.observe(model),
				new UpdateValueStrategy() {
					@Override
					public Object convert(Object value) {
						if (value.equals(Boolean.TRUE)) {
							return cpuShares;
						}
						return 0l;
					}

				}, new UpdateValueStrategy() {
					@Override
					public Object convert(final Object value) {
						return value.equals(cpuShares);
					}
				});
	}

	private IChangeListener onEnableResourceLimitation(
			final Composite container) {
		return event -> toggleResourceLimitationControls(container);
	}

	private void toggleResourceLimitationControls(Composite container) {
		for (Control childControl : container.getChildren()) {
			if (model.isEnableResourceLimitations()) {
				childControl.setEnabled(true);
			} else {
				childControl.setEnabled(false);
			}
		}
	}

	private void createVolumeSettingsContainer(final Composite container) {
		final Label volumesLabel = new Label(container, SWT.NONE);
		volumesLabel.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.dataVolumesLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(COLUMNS, 1).applyTo(volumesLabel);
		final CheckboxTableViewer dataVolumesTableViewer = createVolumesTable(
				container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).hint(200, 100)
				.applyTo(dataVolumesTableViewer.getTable());
		// buttons
		final Composite buttonsContainers = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(false, false).applyTo(buttonsContainers);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.spacing(SWT.DEFAULT, 0).applyTo(buttonsContainers);

		final Button addButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(addButton);
		addButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.addButton")); //$NON-NLS-1$
		addButton.addSelectionListener(onAddDataVolume(dataVolumesTableViewer));
		final Button editButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(editButton);
		editButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.editButton")); //$NON-NLS-1$
		editButton
				.addSelectionListener(onEditDataVolume(dataVolumesTableViewer));
		editButton.setEnabled(false);
		final Button removeButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(removeButton);
		removeButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.removeButton")); //$NON-NLS-1$
		removeButton.addSelectionListener(
				onRemoveDataVolumes(dataVolumesTableViewer));
		removeButton.setEnabled(false);
		// update table content when selected image changes
		bind(dataVolumesTableViewer, model.getDataVolumes(),
				BeanProperties.values(DataVolumeModel.class,
						DataVolumeModel.CONTAINER_PATH, DataVolumeModel.MOUNT,
						DataVolumeModel.READ_ONLY_VOLUME));
		dbc.bindSet(ViewerProperties.checkedElements(DataVolumeModel.class).observe((Viewer) dataVolumesTableViewer),
				BeanProperties.set(ImageRunResourceVolumesVariablesModel.SELECTED_DATA_VOLUMES).observe(model));
		// disable the edit and removeButton if the table is empty
		dataVolumesTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));

	}

	private void setDefaultValues() {
		try {
			// skip if a previous launch configuration was provided
			final IDockerImage selectedImage = model.getSelectedImage();
			if (selectedImage == null) {
				return;
			}
			final IDockerImageInfo selectedImageInfo = findImageInfo(
					selectedImage);
			// using a map filled with entries (key==value) from default volumes
			// that can be overridden by values from last launch config
			final Map<String, DataVolumeModel> volumes = new HashMap<>();
			final Set<DataVolumeModel> selectedVolumes = new HashSet<>();
			if (selectedImageInfo != null
					&& selectedImageInfo.config() != null) {
				for (String volume : selectedImageInfo.config().volumes().keySet()) {
					volumes.put(volume, new DataVolumeModel(volume));
				}
			}
			if (lastLaunchConfiguration != null) {
				// volumes:
				final List<String> launchConfigVolumes = lastLaunchConfiguration
						.getAttribute(DATA_VOLUMES,
								Collections.<String> emptyList());
				for (String containerVolume : launchConfigVolumes) {
					final DataVolumeModel volume = DataVolumeModel
							.parseString(containerVolume);
					if (volume != null) {
						volumes.put(volume.getContainerPath(), volume);
						selectedVolumes.add(volume);
					}
				}

				// environment variables
				model.setEnvironmentVariables(
						lastLaunchConfiguration.getAttribute(ENV_VARIABLES,
								Collections.<String> emptyList()));

				// labels
				Map<String, String> labels = lastLaunchConfiguration
						.getAttribute(LABELS, (Map<String, String>) null);
				if (labels != null) {
					model.setLabelVariables(labels);
				}

				// resource limitations
				model.setEnableResourceLimitations(lastLaunchConfiguration
						.getAttribute(ENABLE_LIMITS, false));
				// CPU shares
				model.setCpuShareWeight(Long.parseLong(lastLaunchConfiguration
						.getAttribute(CPU_PRIORITY, Long.toString(
								ImageRunResourceVolumesVariablesModel.CPU_MEDIUM))));
				// retrieve memory limit stored in MB
				final long memoryLimit = Long.parseLong(lastLaunchConfiguration
						.getAttribute(MEMORY_LIMIT, Long.toString(
								ImageRunResourceVolumesVariablesModel.DEFAULT_MEMORY)));
				// make sure memory limit is not higher than maxMemory
				model.setMemoryLimit(
						Math.min(model.getTotalMemory(), memoryLimit));
			}
			model.setDataVolumes(volumes.values());
			model.setSelectedDataVolumes(selectedVolumes);
		} catch (CoreException | InvocationTargetException
				| InterruptedException e) {
			Activator.log(e);
		}
	}

	private IDockerImageInfo findImageInfo(final IDockerImage selectedImage)
			throws InvocationTargetException, InterruptedException {
		final FindImageInfoRunnable findImageInfoRunnable = new FindImageInfoRunnable(
				selectedImage);
		getContainer().run(true, true, findImageInfoRunnable);
		final IDockerImageInfo selectedImageInfo = findImageInfoRunnable
				.getResult();
		return selectedImageInfo;
	}

	/**
	 * Same as
	 * {@link ViewerSupport#bind(StructuredViewer, IObservableList, org.eclipse.core.databinding.property.value.IValueProperty[])
	 * but with a custom LabelProvider, DataVolumesLabelProvider
	 *
	 * @param viewer
	 * @param input
	 * @param labelProperties
	 */
	private void bind(final StructuredViewer viewer,
			final IObservableList input,
			final IBeanValueProperty[] labelProperties) {
		final ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		if (viewer.getInput() != null) {
			viewer.setInput(null);
		}
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(
				new DataVolumesLabelProvider(Properties.observeEach(
						contentProvider.getKnownElements(), labelProperties)));
		if (input != null) {
			viewer.setInput(input);
		}

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

	private SelectionListener onAddDataVolume(
			final CheckboxTableViewer dataVolumesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ContainerDataVolumeDialog dialog = new ContainerDataVolumeDialog(
					getShell(), model.getConnection());
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				final DataVolumeModel dataVolume = dialog.getDataVolume();
				dataVolume.setSelected(true);
				model.getDataVolumes().add(dataVolume);
				model.getSelectedDataVolumes().add(dataVolume);
				dataVolumesTableViewer.setChecked(dataVolume, true);
			}
		});
	}

	private SelectionListener onEditDataVolume(
			final CheckboxTableViewer dataVolumesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = dataVolumesTableViewer.getStructuredSelection();
			if (selection.isEmpty()) {
				return;
			}
			final DataVolumeModel selectedDataVolume = (DataVolumeModel) selection
					.getFirstElement();
			final ContainerDataVolumeDialog dialog = new ContainerDataVolumeDialog(
					getShell(), model.getConnection(), selectedDataVolume);
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				final DataVolumeModel dialogDataVolume = dialog.getDataVolume();
				selectedDataVolume.setContainerMount(
						dialogDataVolume.getContainerMount());
				selectedDataVolume
						.setMountType(dialogDataVolume.getMountType());
				selectedDataVolume
						.setHostPathMount(dialogDataVolume.getHostPathMount());
				selectedDataVolume.setContainerMount(
						dialogDataVolume.getContainerMount());
				selectedDataVolume.setReadOnly(dialogDataVolume.isReadOnly());
				model.getSelectedDataVolumes().add(selectedDataVolume);
				dataVolumesTableViewer.setChecked(selectedDataVolume, true);
			}
		});
	}

	private SelectionListener onRemoveDataVolumes(
			final TableViewer dataVolumesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = dataVolumesTableViewer
					.getStructuredSelection();
			for (@SuppressWarnings("unchecked")
			Iterator<DataVolumeModel> iterator = selection.iterator(); iterator
					.hasNext();) {
				final DataVolumeModel volume = iterator.next();
				model.removeDataVolume(volume);
				model.getSelectedDataVolumes().remove(volume);
			}

		});
	}

	private CheckboxTableViewer createVolumesTable(final Composite container) {
		final Table table = new Table(container, SWT.CHECK | SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		final CheckboxTableViewer tableViewer = new CheckboxTableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		dbc.bindSet(ViewerProperties.checkedElements(DataVolumeModel.class).observe((Viewer) tableViewer),
				BeanProperties.set(ImageRunResourceVolumesVariablesModel.SELECTED_DATA_VOLUMES).observe(model));
		addTableViewerColum(tableViewer,
				WizardMessages.getString(
						"ImageRunResourceVolVarPage.containerPathColumn"), //$NON-NLS-1$
				150);
		addTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageRunResourceVolVarPage.mountColumn"), //$NON-NLS-1$
				150);
		addTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageRunResourceVolVarPage.readonlyColumn"), //$NON-NLS-1$
				60);
		return tableViewer;
	}

	private TableViewerColumn addTableViewerColum(final TableViewer tableViewer,
			final String title, final int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		if (title != null) {
			column.setText(title);
		}
		column.setWidth(width);
		return viewerColumn;
	}

	private void createLabelVariablesContainer(final Composite container) {
		final Label labelVarLabel = new Label(container, SWT.NONE);
		labelVarLabel.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.labelVarLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(COLUMNS, 1).applyTo(labelVarLabel);
		final TableViewer labelVariablesTableViewer = createEnvironmentVariablesTable(
				container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).hint(200, 100)
				.applyTo(labelVariablesTableViewer.getTable());
		// buttons
		final Composite buttonsContainers = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(false, false).applyTo(buttonsContainers);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.spacing(SWT.DEFAULT, 0).applyTo(buttonsContainers);

		final Button addButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(addButton);
		addButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.addButton")); //$NON-NLS-1$
		addButton.setEnabled(true);
		addButton.addSelectionListener(onAddLabelVariable());
		final Button editButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(editButton);
		editButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.editButton")); //$NON-NLS-1$
		editButton.setEnabled(true);
		editButton.addSelectionListener(
				onEditLabelVariable(labelVariablesTableViewer));
		editButton.setEnabled(false);
		final Button removeButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(removeButton);
		removeButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.removeButton")); //$NON-NLS-1$
		removeButton.addSelectionListener(
				onRemoveLabelVariables(labelVariablesTableViewer));
		removeButton.setEnabled(false);
		// update table content when selected image changes
		ViewerSupport.bind(labelVariablesTableViewer, model.getLabelVariables(),
				BeanProperties.values(LabelVariableModel.class,
						LabelVariableModel.NAME, LabelVariableModel.VALUE));
		// disable the edit and removeButton if the table is empty
		labelVariablesTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));
	}

	private SelectionListener onAddLabelVariable() {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ContainerLabelVariableDialog dialog = new ContainerLabelVariableDialog(
					getShell());
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				model.getLabelVariables().add(dialog.getLabelVariable());
			}
		});
	}

	private SelectionListener onEditLabelVariable(
			final TableViewer LabelVariablesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final LabelVariableModel selectedVariable = (LabelVariableModel) LabelVariablesTableViewer
					.getStructuredSelection().getFirstElement();
			final ContainerLabelVariableDialog dialog = new ContainerLabelVariableDialog(
					getShell(), selectedVariable);
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				selectedVariable.setName(dialog.getLabelVariable().getName());
				selectedVariable.setValue(dialog.getLabelVariable().getValue());
				LabelVariablesTableViewer.refresh();
			}
		});
	}

	private SelectionListener onRemoveLabelVariables(
			final TableViewer linksTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = linksTableViewer
					.getStructuredSelection();
			for (@SuppressWarnings("unchecked")
			Iterator<LabelVariableModel> iterator = selection
					.iterator(); iterator.hasNext();) {
				model.removeLabelVariable(iterator.next());
			}
		});
	}

	private void createEnvironmentVariablesContainer(
			final Composite container) {
		final Label envVarLabel = new Label(container, SWT.NONE);
		envVarLabel.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.envVarLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(COLUMNS, 1).applyTo(envVarLabel);
		final TableViewer environmentVariablesTableViewer = createEnvironmentVariablesTable(
				container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).hint(200, 100)
				.applyTo(environmentVariablesTableViewer.getTable());
		// buttons
		final Composite buttonsContainers = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(false, false).applyTo(buttonsContainers);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.spacing(SWT.DEFAULT, 0).applyTo(buttonsContainers);

		final Button addButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(addButton);
		addButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.addButton")); //$NON-NLS-1$
		addButton.setEnabled(true);
		addButton.addSelectionListener(onAddEnvironmentVariable());
		final Button editButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(editButton);
		editButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.editButton")); //$NON-NLS-1$
		editButton.setEnabled(true);
		editButton.addSelectionListener(
				onEditEnvironmentVariable(environmentVariablesTableViewer));
		editButton.setEnabled(false);
		final Button removeButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(removeButton);
		removeButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.removeButton")); //$NON-NLS-1$
		removeButton.addSelectionListener(
				onRemoveEnvironmentVariables(environmentVariablesTableViewer));
		removeButton.setEnabled(false);
		// update table content when selected image changes
		ViewerSupport.bind(environmentVariablesTableViewer,
				model.getEnvironmentVariables(),
				BeanProperties.values(EnvironmentVariableModel.class,
						EnvironmentVariableModel.NAME,
						EnvironmentVariableModel.VALUE));
		// disable the edit and removeButton if the table is empty
		environmentVariablesTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));
	}

	private TableViewer createEnvironmentVariablesTable(Composite container) {
		final Table table = new Table(container,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		final TableViewer tableViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		addTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageRunResourceVolVarPage.nameColumn"), //$NON-NLS-1$
				200);
		addTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageRunResourceVolVarPage.valueColumn"), //$NON-NLS-1$
				200);
		return tableViewer;
	}

	private SelectionListener onAddEnvironmentVariable() {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ContainerEnvironmentVariableDialog dialog = new ContainerEnvironmentVariableDialog(
					getShell());
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				model.getEnvironmentVariables()
						.add(dialog.getEnvironmentVariable());
			}
		});
	}

	private SelectionListener onEditEnvironmentVariable(
			final TableViewer environmentVariablesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final EnvironmentVariableModel selectedVariable = (EnvironmentVariableModel) environmentVariablesTableViewer
					.getStructuredSelection().getFirstElement();
			final ContainerEnvironmentVariableDialog dialog = new ContainerEnvironmentVariableDialog(
					getShell(), selectedVariable);
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				selectedVariable
						.setName(dialog.getEnvironmentVariable().getName());
				selectedVariable
						.setValue(dialog.getEnvironmentVariable().getValue());
				environmentVariablesTableViewer.refresh();
			}
		});
	}

	private SelectionListener onRemoveEnvironmentVariables(
			final TableViewer linksTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = linksTableViewer
					.getStructuredSelection();
			for (@SuppressWarnings("unchecked")
			Iterator<EnvironmentVariableModel> iterator = selection
					.iterator(); iterator.hasNext();) {
				model.removeEnvironmentVariable(iterator.next());
			}
		});
	}

	private static void setControlsEnabled(final Control[] controls,
			final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

	private static final class DataVolumesLabelProvider
			extends ObservableMapLabelProvider {

		private Image CONTAINER_IMAGE = SWTImagesFactory.DESC_CONTAINER
				.createImage();
		private Image FOLDER_CLOSED_IMAGE = SWTImagesFactory.DESC_FOLDER_CLOSED
				.createImage();
		private Image FILE_IMAGE = SWTImagesFactory.DESC_FILE.createImage();

		public DataVolumesLabelProvider(final IObservableMap[] attributeMaps) {
			super(attributeMaps);
		}

		@Override
		public void dispose() {
			CONTAINER_IMAGE.dispose();
			FOLDER_CLOSED_IMAGE.dispose();
			FILE_IMAGE.dispose();
			super.dispose();
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			final DataVolumeModel dataVolume = ((DataVolumeModel) element);
			if (dataVolume.getMountType() != null && columnIndex == 1) {
				switch (dataVolume.getMountType()) {
				case CONTAINER:
					return CONTAINER_IMAGE;
				case HOST_FILE_SYSTEM:
					final File hostFile = new File(dataVolume.getMount());
					if (!hostFile.exists() || hostFile.isDirectory()) {
						return FOLDER_CLOSED_IMAGE;
					} else {
						return FILE_IMAGE;
					}
				default:
					return null;
				}
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			final DataVolumeModel dataVolume = ((DataVolumeModel) element);
			switch (columnIndex) {
			case 0:
				return dataVolume.getContainerPath();
			case 1:
				return dataVolume.getMount();
			case 2:
				if (dataVolume.getMountType() != MountType.HOST_FILE_SYSTEM) {
					return null;
				} else if (dataVolume.isReadOnly()) {
					return WizardMessages
							.getString("ImageRunResourceVolVarPage.true"); //$NON-NLS-1$
				}
				return WizardMessages
						.getString("ImageRunResourceVolVarPage.false"); //$NON-NLS-1$
			default:
				return null;
			}
		}
	}

}
