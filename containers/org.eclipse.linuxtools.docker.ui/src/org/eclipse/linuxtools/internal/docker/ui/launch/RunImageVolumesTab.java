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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerDataVolumeDialog;
import org.eclipse.linuxtools.internal.docker.ui.wizards.DataVolumeModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel.MountType;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class RunImageVolumesTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "RunVolumesTab.name"; //$NON-NLS-1$

	private static final int COLUMNS = 3;

	private final DataBindingContext dbc = new DataBindingContext();
	private ImageRunResourceVolumesVariablesModel model = null;

	public RunImageVolumesTab(ImageRunResourceVolumesVariablesModel model) {
		this.model = model;
	}

	public ImageRunResourceVolumesVariablesModel getModel() {
		return model;
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.applyTo(container);
		if (model == null) {
			setErrorMessage(LaunchMessages.getString("NoConnectionError.msg"));
		} else {
			setErrorMessage(null);
			createVolumeSettingsContainer(container);
		}
		setControl(container);
	}

	private void createVolumeSettingsContainer(final Composite container) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1)
				.applyTo(new Label(container, SWT.NONE));
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
		// update table content when selected image changes
		bind(dataVolumesTableViewer, model.getDataVolumes(),
				BeanProperties.values(DataVolumeModel.class,
						new String[] { DataVolumeModel.CONTAINER_PATH,
								DataVolumeModel.MOUNT,
								DataVolumeModel.READ_ONLY_VOLUME }));
		final IObservableSet selectedVolumesObservable = BeanProperties
				.set(ImageRunResourceVolumesVariablesModel.SELECTED_DATA_VOLUMES)
				.observe(model);
		dbc.bindSet(
				ViewersObservables.observeCheckedElements(
						dataVolumesTableViewer, DataVolumeModel.class),
				selectedVolumesObservable);
		dataVolumesTableViewer.addCheckStateListener(onCheckStateChanged());

		// initializes the checkboxes selection upon loading the model.
				// remove ?
				// selectedVolumesObservable.addChangeListener(new
				// IChangeListener() {
				//
				// @Override
				// public void handleChange(ChangeEvent event) {
				// final IObservableSet observable = (IObservableSet) event
				// .getObservable();
				// for (Iterator<?> iterator = observable.iterator(); iterator
				// .hasNext();) {
				// final DataVolumeModel volume = (DataVolumeModel) iterator
				// .next();
				// dataVolumesTableViewer.setChecked(volume, true);
				// }
				// updateLaunchConfigurationDialog();
				// }
				// });

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
		// disable the edit and removeButton if the table is empty
		dataVolumesTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));

	}

	private ICheckStateListener onCheckStateChanged() {
		return new ICheckStateListener() {

			@Override
			public void checkStateChanged(final CheckStateChangedEvent e) {
				DataVolumeModel element = (DataVolumeModel) e.getElement();
				if (e.getChecked()) {
					model.getSelectedDataVolumes().add(element);
					element.setSelected(true);
				} else {
					model.getSelectedDataVolumes().remove(element);
					element.setSelected(false);
				}
				updateLaunchConfigurationDialog();
			}
		};
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
		return new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent e) {
				if (e.getSelection().isEmpty()) {
					setControlsEnabled(targetButtons, false);
					updateLaunchConfigurationDialog();
				} else {
					setControlsEnabled(targetButtons, true);
					updateLaunchConfigurationDialog();
				}
			}

		};
	}

	private SelectionListener onAddDataVolume(
			final CheckboxTableViewer dataVolumesTableViewer) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ContainerDataVolumeDialog dialog = new ContainerDataVolumeDialog(
						getShell(), model.getConnection());
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					model.getDataVolumes().add(dialog.getDataVolume());
					model.getSelectedDataVolumes().add(dialog.getDataVolume());
					dialog.getDataVolume().setSelected(true);
					dataVolumesTableViewer.setChecked(dialog.getDataVolume(),
							true);
					updateLaunchConfigurationDialog();
				}
			}
		};
	}

	private SelectionListener onEditDataVolume(
			final CheckboxTableViewer dataVolumesTableViewer) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = (IStructuredSelection) dataVolumesTableViewer
						.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				final DataVolumeModel selectedDataVolume = (DataVolumeModel) selection
						.getFirstElement();
				final ContainerDataVolumeDialog dialog = new ContainerDataVolumeDialog(
						getShell(), model.getConnection(), selectedDataVolume);
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					final DataVolumeModel dialogDataVolume = dialog
							.getDataVolume();
					selectedDataVolume.setContainerMount(
							dialogDataVolume.getContainerMount());
					selectedDataVolume
							.setMountType(dialogDataVolume.getMountType());
					selectedDataVolume.setHostPathMount(
							dialogDataVolume.getHostPathMount());
					selectedDataVolume.setContainerMount(
							dialogDataVolume.getContainerMount());
					selectedDataVolume
							.setReadOnly(dialogDataVolume.isReadOnly());
					model.getSelectedDataVolumes().add(selectedDataVolume);
					dataVolumesTableViewer.setChecked(selectedDataVolume, true);
					updateLaunchConfigurationDialog();
				}
			}
		};
	}

	private SelectionListener onRemoveDataVolumes(
			final TableViewer dataVolumesTableViewer) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = dataVolumesTableViewer
						.getStructuredSelection();
				for (@SuppressWarnings("unchecked")
				Iterator<DataVolumeModel> iterator = selection
						.iterator(); iterator.hasNext();) {
					final DataVolumeModel volume = iterator.next();
					model.removeDataVolume(volume);
					model.getSelectedDataVolumes().remove(volume);
				}
				updateLaunchConfigurationDialog();
			}
		};
	}

	private CheckboxTableViewer createVolumesTable(final Composite container) {
		final Table table = new Table(container, SWT.CHECK | SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		final CheckboxTableViewer tableViewer = new CheckboxTableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		addTableViewerColumn(tableViewer,
				WizardMessages.getString(
						"ImageRunResourceVolVarPage.containerPathColumn"), //$NON-NLS-1$
				150);
		addTableViewerColumn(tableViewer,
				WizardMessages
						.getString("ImageRunResourceVolVarPage.mountColumn"), //$NON-NLS-1$
				150);
		addTableViewerColumn(tableViewer,
				WizardMessages
						.getString("ImageRunResourceVolVarPage.readonlyColumn"), //$NON-NLS-1$
				60);
		return tableViewer;
	}

	private TableViewerColumn addTableViewerColumn(
			final TableViewer tableViewer,
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

	private static void setControlsEnabled(final Control[] controls,
			final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

	private static final class DataVolumesLabelProvider
			extends ObservableMapLabelProvider {

		public DataVolumesLabelProvider(final IObservableMap[] attributeMaps) {
			super(attributeMaps);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			final DataVolumeModel dataVolume = ((DataVolumeModel) element);
			if (dataVolume.getMountType() != null && columnIndex == 1) {
				switch (dataVolume.getMountType()) {
				case CONTAINER:
					return SWTImagesFactory.DESC_CONTAINER.createImage();
				case HOST_FILE_SYSTEM:
					final File hostFile = new File(dataVolume.getMount());
					if (!hostFile.exists() || hostFile.isDirectory()) {
						return SWTImagesFactory.DESC_FOLDER_CLOSED
								.createImage();
					} else {
						return SWTImagesFactory.DESC_FILE.createImage();
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

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_CONTAINER_VOLUME);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		final List<DataVolumeModel> volumes = new ArrayList<>();
		try {
			final List<String> volumesList = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.DATA_VOLUMES,
					new ArrayList<String>());
			final Set<DataVolumeModel> selectedVolumes = new HashSet<>();
			for (String volume : volumesList) {
				DataVolumeModel volumeModel = DataVolumeModel
						.parseString(volume);
				volumes.add(volumeModel);
				if (volumeModel.getSelected()) {
					selectedVolumes.add(volumeModel);
				}
			}
			model.setDataVolumes(volumes);
			model.setSelectedDataVolumes(selectedVolumes);

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
		WritableList volumes = model.getDataVolumes();
		Set<DataVolumeModel> selectedVolumes = model.getSelectedDataVolumes();

		ArrayList<String> binds = new ArrayList<>();
		ArrayList<String> volumesFrom = new ArrayList<>();
		ArrayList<String> volumesList = new ArrayList<>();
		Set<String> selectedVolumesSet = new TreeSet<>();

		for (@SuppressWarnings("unchecked")
		Iterator<DataVolumeModel> iterator = volumes.iterator(); iterator
				.hasNext();) {
			DataVolumeModel volume = iterator.next();
			StringBuffer buffer = new StringBuffer();
			volumesList.add(volume.toString());
			switch (volume.getMountType()) {
			case HOST_FILE_SYSTEM:
				buffer.append(volume.getHostPathMount() + ","
						+ volume.getHostPathMount() + ","
						+ volume.isReadOnly());
				if (selectedVolumes.contains(volume)) {
					selectedVolumesSet.add(volume.toString());
					String bind = convertToUnixPath(volume.getHostPathMount())
							+ ':' + volume.getContainerPath() + ':' + 'Z';
					if (volume.isReadOnly()) {
						bind += ",ro"; //$NON-NLS-1$
					}
					binds.add(bind);
				}
				break;
			case CONTAINER:
				if (selectedVolumes.contains(volume)) {
					selectedVolumesSet.add(volume.toString());
					volumesFrom.add(volume.getContainerMount());
				}
				break;
			default:
				break;
			}
		}
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.BINDS, binds);
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.VOLUMES_FROM,
				volumesFrom);
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.DATA_VOLUMES,
				volumesList);
	}

	private String convertToUnixPath(String path) {
		String unixPath = path;

		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			// replace backslashes with slashes
			unixPath = unixPath.replaceAll("\\\\", "/");

			// replace "C:/" with "/c/"
			Matcher m = Pattern.compile("([a-zA-Z]):/").matcher(unixPath);
			if (m.find()) {
				StringBuffer b = new StringBuffer();
				b.append('/');
				m.appendReplacement(b, m.group(1).toLowerCase());
				b.append('/');
				m.appendTail(b);
				unixPath = b.toString();
			}
		}

		return unixPath;
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

	private class LaunchConfigurationChangeListener
			implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			updateLaunchConfigurationDialog();
		}
	}

}
