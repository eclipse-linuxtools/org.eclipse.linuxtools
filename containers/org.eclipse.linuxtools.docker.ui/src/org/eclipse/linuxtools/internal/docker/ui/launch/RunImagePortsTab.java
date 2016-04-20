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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerPortDialog;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ExposedPortModel;
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

public class RunImagePortsTab extends AbstractLaunchConfigurationTab
		implements PropertyChangeListener {

	private static final String TAB_NAME = "RunPortsTab.name"; //$NON-NLS-1$

	private static final int INDENT = 30;

	private static final int COLUMNS = 3;

	private final DataBindingContext dbc = new DataBindingContext();

	private final ImageRunSelectionModel model;

	public RunImagePortsTab(ImageRunSelectionModel model) {
		this.model = model;
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
		} else {
			setErrorMessage(null);
			createPortSettingsSection(container);
		}
		setControl(container);
	}

	private void createPortSettingsSection(final Composite container) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1)
				.applyTo(new Label(container, SWT.NONE));
		final Button publishAllPortsButton = new Button(container, SWT.CHECK);
		publishAllPortsButton.setText(WizardMessages
				.getString("ImageRunSelectionPage.publishAllPorts")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(publishAllPortsButton);
		dbc.bindValue(
				WidgetProperties.selection().observe(publishAllPortsButton),
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
				.setText(WizardMessages.getString("ImageRunSelectionPage.add")); //$NON-NLS-1$
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
						new String[] { ExposedPortModel.CONTAINER_PORT,
								ExposedPortModel.PORT_TYPE,
								ExposedPortModel.HOST_ADDRESS,
								ExposedPortModel.HOST_PORT }));
		dbc.bindSet(
				ViewersObservables.observeCheckedElements(
						exposedPortsTableViewer, ExposedPortModel.class),
				BeanProperties.set(ImageRunSelectionModel.SELECTED_PORTS)
						.observe(model));

		// disable the edit and removeButton if the table is empty
		exposedPortsTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));
		exposedPortsTableViewer.addCheckStateListener(onCheckStateChanged());

		togglePortMappingControls(exposedPortsTableViewer.getTable(), addButton,
				removeButton);
	}

	private static void setControlsEnabled(final Control[] controls,
			final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
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

	private ICheckStateListener onCheckStateChanged() {
		return e -> {
			ImageRunSelectionModel.ExposedPortModel element = (ImageRunSelectionModel.ExposedPortModel) e
					.getElement();
			if (e.getChecked()) {
				model.getSelectedPorts().add(element);
				element.setSelected(true);
			} else {
				model.getSelectedPorts().remove(element);
				element.setSelected(false);
			}
			updateLaunchConfigurationDialog();
		};
	}

	private CheckboxTableViewer createPortSettingsTable(
			final Composite container) {
		final Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		final CheckboxTableViewer tableViewer = new CheckboxTableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createTableViewerColumn(tableViewer,
				WizardMessages
						.getString("ImageRunSelectionPage.containerPortColumn"), //$NON-NLS-1$
				100);
		createTableViewerColumn(tableViewer,
				WizardMessages.getString("ImageRunSelectionPage.typeColumn"), //$NON-NLS-1$
				50);
		createTableViewerColumn(tableViewer,
				WizardMessages
						.getString("ImageRunSelectionPage.hostAddressColumn"), //$NON-NLS-1$
				100);
		createTableViewerColumn(tableViewer,
				WizardMessages
						.getString("ImageRunSelectionPage.hostPortColumn"), //$NON-NLS-1$
				100);
		tableViewer.setContentProvider(new ObservableListContentProvider());
		return tableViewer;
	}

	private TableViewerColumn createTableViewerColumn(
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

	private IValueChangeListener onPublishAllPortsChange(
			final Control... controls) {
		return event -> togglePortMappingControls(controls);
	}

	private SelectionListener onAddPort(
			final CheckboxTableViewer exposedPortsTableViewer) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ContainerPortDialog dialog = new ContainerPortDialog(
						getShell());
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					final ExposedPortModel port = dialog.getPort();
					model.addAvailablePort(port);
					model.getSelectedPorts().add(port);
					port.setSelected(true);
					exposedPortsTableViewer.setChecked(port, true);
					updateLaunchConfigurationDialog();
				}
			}
		};
	}

	private SelectionListener onEditPort(
			final CheckboxTableViewer exposedPortsTableViewer) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = exposedPortsTableViewer
						.getStructuredSelection();
				final ExposedPortModel selectedContainerPort = (ExposedPortModel) selection
						.getFirstElement();
				final ContainerPortDialog dialog = new ContainerPortDialog(
						getShell(), selectedContainerPort);
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					final ExposedPortModel configuredPort = dialog.getPort();
					selectedContainerPort.setContainerPort(
							configuredPort.getContainerPort());
					selectedContainerPort
							.setHostAddress(configuredPort.getHostAddress());
					selectedContainerPort
							.setHostPort(configuredPort.getHostPort());
				}
				updateLaunchConfigurationDialog();
			}
		};
	}

	private SelectionListener onRemovePorts(
			final TableViewer portsTableViewer) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = portsTableViewer
						.getStructuredSelection();
				for (@SuppressWarnings("unchecked")
				Iterator<ExposedPortModel> iterator = selection
						.iterator(); iterator.hasNext();) {
					final ExposedPortModel port = iterator.next();
					model.removeAvailablePort(port);
					model.getSelectedPorts().remove(port);
				}
				updateLaunchConfigurationDialog();
			}
		};
	}

	private void togglePortMappingControls(final Control... controls) {
		for (Control control : controls) {
			if (model.isPublishAllPorts()) {
				control.setEnabled(false);
			} else {
				control.setEnabled(true);
			}
			updateLaunchConfigurationDialog();
		}
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_CONTAINER_PORT);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			// recycle the model
			model.removeExposedPorts();
			final boolean publishAllPorts = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.PUBLISH_ALL_PORTS,
					true);
			model.setPublishAllPorts(publishAllPorts);
			final List<String> publishedPorts = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.PUBLISHED_PORTS,
					new ArrayList<String>());
			final Set<ExposedPortModel> selectedPorts = new HashSet<>();
			for (String port : publishedPorts) {
				final ImageRunSelectionModel.ExposedPortModel exposedPort = ImageRunSelectionModel.ExposedPortModel
						.createPortModel(port);
				model.addExposedPort(exposedPort);
				if (exposedPort.getSelected()) {
					selectedPorts.add(exposedPort);
				}
			}
			// select ports
			model.setSelectedPorts(selectedPorts);

			// update the underlying launch config working copy on model
			// changes.
			model.addPropertyChangeListener(
					new LaunchConfigurationChangeListener());
		} catch (CoreException e) {
			Activator.logErrorMessage(
					LaunchMessages.getString(
							"RunDockerImageLaunchConfiguration.load.failure"), //$NON-NLS-1$
					e);
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.PUBLISH_ALL_PORTS,
				model.isPublishAllPorts());
		List<String> portsList = new ArrayList<>();
		for (Object o : model.getExposedPorts().toArray()) {
			ImageRunSelectionModel.ExposedPortModel m = (ImageRunSelectionModel.ExposedPortModel) o;
			portsList.add(m.toString());
		}
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.PUBLISHED_PORTS,
				portsList);
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub

	}

	private class LaunchConfigurationChangeListener
			implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			updateLaunchConfigurationDialog();
		}
	}

}
