/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerLabelVariableDialog;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.LabelVariableModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class RunImageLabelsTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "RunLabelsTab.name"; //$NON-NLS-1$

	private static final int COLUMNS = 3;

	private ImageRunResourceVolumesVariablesModel model = null;

	public RunImageLabelsTab(ImageRunResourceVolumesVariablesModel model) {
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
			createLabelVariablesContainer(container);
		}
		setControl(container);
	}

	private void createLabelVariablesContainer(
			final Composite container) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1)
				.applyTo(new Label(container, SWT.NONE));
		final Label envVarLabel = new Label(container, SWT.NONE);
		envVarLabel.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.labelVarLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(COLUMNS, 1).applyTo(envVarLabel);
		final TableViewer labelVariablesTableViewer = createLabelVariablesTable(
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
		addButton.addSelectionListener(
				onAddLabelVariable(labelVariablesTableViewer));
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
				onRemoveLabelVariable(labelVariablesTableViewer));
		removeButton.setEnabled(false);
		// update table content when selected image changes
		ViewerSupport.bind(labelVariablesTableViewer, model.getLabelVariables(),
				BeanProperties.values(LabelVariableModel.class,
						LabelVariableModel.NAME, LabelVariableModel.VALUE));

		// disable the edit and removeButton if the table is empty
		labelVariablesTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));
	}

	private ISelectionChangedListener onSelectionChanged(
			final Button... targetButtons) {
		return e -> {
			if (e.getSelection().isEmpty()) {
				setControlsEnabled(targetButtons, false);
			} else {
				setControlsEnabled(targetButtons, true);
			}
			updateLaunchConfigurationDialog();
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

	private TableViewer createLabelVariablesTable(Composite container) {
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

	private SelectionListener onAddLabelVariable(
			final TableViewer labelVariablesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ContainerLabelVariableDialog dialog = new ContainerLabelVariableDialog(
					getShell());
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				model.getLabelVariables().add(dialog.getLabelVariable());
				labelVariablesTableViewer.add(dialog.getLabelVariable());
				labelVariablesTableViewer.refresh();
				updateLaunchConfigurationDialog();
			}
		});
	}

	private SelectionListener onEditLabelVariable(
			final TableViewer labelVariablesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final LabelVariableModel selectedVariable = (LabelVariableModel) labelVariablesTableViewer
					.getStructuredSelection().getFirstElement();
			final ContainerLabelVariableDialog dialog = new ContainerLabelVariableDialog(
					getShell(), selectedVariable);
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				selectedVariable.setName(dialog.getLabelVariable().getName());
				selectedVariable.setValue(dialog.getLabelVariable().getValue());
				labelVariablesTableViewer.refresh();
			}
			updateLaunchConfigurationDialog();
		});
	}

	private SelectionListener onRemoveLabelVariable(
			final TableViewer labelVariablesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = labelVariablesTableViewer
					.getStructuredSelection();
			for (@SuppressWarnings("unchecked")
			Iterator<LabelVariableModel> iterator = selection
					.iterator(); iterator.hasNext();) {
				LabelVariableModel m = iterator.next();
				model.removeLabelVariable(m);
				labelVariablesTableViewer.remove(m);
				labelVariablesTableViewer.refresh();
			}
			updateLaunchConfigurationDialog();
		});
	}

	private static void setControlsEnabled(final Control[] controls,
			final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_LABELS_TAB);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		if (model == null)
			return;
		try {
			model.removeLabelVariables();
			final Map<String, String> labelVariables = configuration
					.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.LABELS,
					(Map<String, String>) null);
			if (labelVariables != null) {
				for (Map.Entry<String, String> entry : labelVariables
						.entrySet()) {
					model.addLabelVariable(
							LabelVariableModel
							.createLabelVariableModel(entry.getKey(),
									entry.getValue()));
				}
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
		if (model == null)
			return;
		Map<String, String> labelMap = new HashMap<>();
		for (Object o : model.getLabelVariables()) {
			LabelVariableModel m = (LabelVariableModel) o;
			labelMap.put(m.getName(), m.getValue());
		}
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.LABELS,
				labelMap);
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

}
