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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerEnvironmentVariableDialog;
import org.eclipse.linuxtools.internal.docker.ui.wizards.EnvironmentVariableModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel;
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

public class RunImageEnvironmentTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "RunEnvTab.name"; //$NON-NLS-1$

	private static final int COLUMNS = 3;

	private ImageRunResourceVolumesVariablesModel model = null;

	public RunImageEnvironmentTab(ImageRunResourceVolumesVariablesModel model) {
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
			createEnvironmentVariablesContainer(container);
		}
		setControl(container);
	}

	private void createEnvironmentVariablesContainer(
			final Composite container) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1)
				.applyTo(new Label(container, SWT.NONE));
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
		addButton.addSelectionListener(
				onAddEnvironmentVariable(environmentVariablesTableViewer));
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
						new String[] { EnvironmentVariableModel.NAME,
								EnvironmentVariableModel.VALUE }));

		// disable the edit and removeButton if the table is empty
		environmentVariablesTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));
	}

	private ISelectionChangedListener onSelectionChanged(
			final Button... targetButtons) {
		return new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent e) {
				if (e.getSelection().isEmpty()) {
					setControlsEnabled(targetButtons, false);
				} else {
					setControlsEnabled(targetButtons, true);
				}
				updateLaunchConfigurationDialog();
			}

		};
	}

	private TableViewerColumn addTableViewerColumn(
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

	private TableViewer createEnvironmentVariablesTable(Composite container) {
		final Table table = new Table(container,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		final TableViewer tableViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		addTableViewerColumn(tableViewer,
				WizardMessages
						.getString("ImageRunResourceVolVarPage.nameColumn"), //$NON-NLS-1$
				200);
		addTableViewerColumn(tableViewer,
				WizardMessages
						.getString("ImageRunResourceVolVarPage.valueColumn"), //$NON-NLS-1$
				200);
		tableViewer.setContentProvider(new ObservableListContentProvider());
		return tableViewer;
	}

	private SelectionListener onAddEnvironmentVariable(
			final TableViewer environmentVariablesTableViewer) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ContainerEnvironmentVariableDialog dialog = new ContainerEnvironmentVariableDialog(
						getShell());
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					model.getEnvironmentVariables()
							.add(dialog.getEnvironmentVariable());
					environmentVariablesTableViewer
							.add(dialog.getEnvironmentVariable());
					environmentVariablesTableViewer.refresh();
					updateLaunchConfigurationDialog();
				}
			}
		};
	}

	private SelectionListener onEditEnvironmentVariable(
			final TableViewer environmentVariablesTableViewer) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final EnvironmentVariableModel selectedVariable = (EnvironmentVariableModel) environmentVariablesTableViewer
						.getStructuredSelection().getFirstElement();
				final ContainerEnvironmentVariableDialog dialog = new ContainerEnvironmentVariableDialog(
						getShell(), selectedVariable);
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					selectedVariable
							.setName(dialog.getEnvironmentVariable().getName());
					selectedVariable.setValue(
							dialog.getEnvironmentVariable().getValue());
					environmentVariablesTableViewer.refresh();
				}
				updateLaunchConfigurationDialog();
			}
		};
	}

	private SelectionListener onRemoveEnvironmentVariables(
			final TableViewer environmentVariablesTableViewer) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = environmentVariablesTableViewer
						.getStructuredSelection();
				for (@SuppressWarnings("unchecked")
				Iterator<EnvironmentVariableModel> iterator = selection
						.iterator(); iterator.hasNext();) {
					EnvironmentVariableModel m = iterator.next();
					model.removeEnvironmentVariable(m);
					environmentVariablesTableViewer.remove(m);
					environmentVariablesTableViewer.refresh();
				}
				updateLaunchConfigurationDialog();
			}
		};
	}

	private static void setControlsEnabled(final Control[] controls,
			final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_ENV);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			model.removeEnvironmentVariables();
			final List<String> environmentVariables = configuration
					.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.ENV_VARIABLES,
					new ArrayList<String>());
			for (String environmenVariable : environmentVariables) {
				model.addEnvironmentVariable(EnvironmentVariableModel
						.createEnvironmentVariableModel(environmenVariable));
			}
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

	private class LaunchConfigurationChangeListener
			implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			updateLaunchConfigurationDialog();
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		ArrayList<String> envList = new ArrayList<>();
		for (Object o : model.getEnvironmentVariables()) {
			EnvironmentVariableModel m = (EnvironmentVariableModel) o;
			envList.add(m.toString());
		}
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.ENV_VARIABLES,
				envList);
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

}
