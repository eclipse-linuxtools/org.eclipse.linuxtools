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

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel.MountType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author xcoulon
 *
 */
public class ContainerDataVolumeDialog extends Dialog {

	private final DataVolumeModel model;

	private final DataBindingContext dbc = new DataBindingContext();

	private final List<String> containerNames;

	private final IDockerConnection connection;

	protected ContainerDataVolumeDialog(final Shell parentShell,
			final IDockerConnection connection,
			final DataVolumeModel selectedDataVolume) {
		super(parentShell);
		this.connection = connection;
		this.model = new DataVolumeModel(selectedDataVolume);
		this.containerNames = WizardUtils.getContainerNames(connection);
	}

	protected ContainerDataVolumeDialog(final Shell parentShell,
			final IDockerConnection connection) {
		super(parentShell);
		this.connection = connection;
		this.model = new DataVolumeModel();
		this.containerNames = WizardUtils.getContainerNames(connection);
	}

	public DataVolumeModel getDataVolume() {
		return model;
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		shell.setText("Data Volume");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, super.getInitialSize().y);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final int COLUMNS = 3;
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(COLUMNS)
				.applyTo(container);
		final Label explanationLabel = new Label(container, SWT.NONE);
		explanationLabel.setText("Specify the Data Volume to mount:"); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(explanationLabel);

		// No mount
		final Button noMountButton = new Button(container, SWT.RADIO);
		noMountButton.setText("No external mount");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(noMountButton);
		bindButton(noMountButton, MountType.NONE);
		// File System mount
		final Button fileSystemMountButton = new Button(container, SWT.RADIO);
		fileSystemMountButton.setText("Mount a host directory or host file");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(fileSystemMountButton);
		final Label hostPathLabel = new Label(container, SWT.NONE);
		hostPathLabel.setText("Path:"); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(20, SWT.DEFAULT).grab(false, false)
				.applyTo(hostPathLabel);
		final Text hostPathText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(hostPathText);
		final IObservableValue hostPathObservable = BeanProperties
				.value(DataVolumeModel.class, DataVolumeModel.HOST_PATH_MOUNT)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(hostPathText),
				hostPathObservable);

		final Button hostPathBrowseButton = new Button(container, SWT.NONE);
		hostPathBrowseButton.setText("Browse...");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(hostPathBrowseButton);
		hostPathBrowseButton.addSelectionListener(onHostPathBrowse());
		// optional read-only access
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(new Label(container, SWT.NONE));
		final Button readOnlyButton = new Button(container, SWT.CHECK);
		readOnlyButton.setText("Read-only access"); //$NON-NLS-1$
		readOnlyButton.setToolTipText(
				"Specify if the mounted host directory or path is read-only"); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS - 1, 1).grab(true, false).applyTo(readOnlyButton);
		final ISWTObservableValue readOnlyButtonObservable = WidgetProperties
				.selection().observe(readOnlyButton);
		dbc.bindValue(readOnlyButtonObservable,
				BeanProperties
						.value(DataVolumeModel.class,
								DataVolumeModel.READ_ONLY_VOLUME)
						.observe(model));
		bindButton(fileSystemMountButton, MountType.HOST_FILE_SYSTEM,
				hostPathText, hostPathBrowseButton, readOnlyButton);

		// Container mount
		final Button containerMountButton = new Button(container, SWT.RADIO);
		containerMountButton.setText("Mount a data volume container");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(containerMountButton);
		final Label containerSelectionLabel = new Label(container, SWT.NONE);
		containerSelectionLabel.setText("Container:"); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(20, SWT.DEFAULT).applyTo(containerSelectionLabel);
		final Combo containerSelectionCombo = new Combo(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(containerSelectionCombo);
		new ControlDecoration(containerSelectionCombo, SWT.TOP | SWT.LEFT);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(new Label(container, SWT.NONE));
		bindButton(containerMountButton, MountType.CONTAINER,
				containerSelectionCombo);
		final ComboViewer containerSelectionComboViewer = new ComboViewer(
				containerSelectionCombo);
		containerSelectionComboViewer
				.setContentProvider(new ArrayContentProvider());
		containerSelectionComboViewer.setInput(this.containerNames);
		final IObservableValue selectedContainerObservable = BeanProperties
				.value(DataVolumeModel.class, DataVolumeModel.CONTAINER_MOUNT)
				.observe(model);
		dbc.bindValue(
				WidgetProperties.selection().observe(containerSelectionCombo),
				selectedContainerObservable);
		new ContentProposalAdapter(containerSelectionCombo,
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
				}, getContainerNameContentProposalProvider(
						containerSelectionCombo),
				null, null);

		// error message
		final Composite errorContainer = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, true).applyTo(errorContainer);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(2)
				.applyTo(errorContainer);

		final Label errorMessageIcon = new Label(errorContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.hint(20, SWT.DEFAULT)
				.applyTo(errorMessageIcon);
		final Label errorMessageLabel = new Label(errorContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false)
				.applyTo(errorMessageLabel);
		setupValidationSupport(errorMessageIcon, errorMessageLabel);
		return container;
	}

	private void setupValidationSupport(final Label errorMessageIcon,
			final Label errorMessageLabel) {
		for (@SuppressWarnings("unchecked")
		Iterator<Binding> iterator = dbc.getBindings().iterator(); iterator
				.hasNext();) {
			final Binding binding = iterator.next();
			binding.getModel().addChangeListener(onDataVolumeSettingsChanged(
					errorMessageIcon, errorMessageLabel));

		}
	}

	/**
	 * Binds the given {@link MountType} to the given {@link Button} when it is
	 * selected, and set the enablement of the associated {@link Control} at the
	 * same time (ie: the {@link Control} are only enabled when the given
	 * {@link Button} is selected.
	 * 
	 * @param button
	 *            the {@link Button} to bind
	 * @param mountType
	 *            the {@link MountType} to bind to the {@link Button}
	 * @param controls
	 *            the {@link Control}s to enable or disable when the Button is
	 *            selected/unselected.
	 * @return
	 */
	private Binding bindButton(final Button button, final MountType mountType,
			final Control... controls) {
		return dbc.bindValue(WidgetProperties.selection().observe(button),
				BeanProperties.value(DataVolumeModel.class,
						DataVolumeModel.MOUNT_TYPE).observe(model),
				new UpdateValueStrategy() {
					@Override
					public Object convert(Object value) {
						if (value.equals(Boolean.TRUE)) {
							setEnabled(controls, true);
							return mountType;
						}
						setEnabled(controls, false);
						return null;
					}

					private void setEnabled(final Control[] controls,
							final boolean enabled) {
						for (Control control : controls) {
							control.setEnabled(enabled);
						}
					}
				}, new UpdateValueStrategy() {
					@Override
					public Object convert(final Object value) {
						if (mountType.equals(value)) {
							button.setEnabled(true);
						}
						return mountType.equals(value);
					}
				});
	}

	private SelectionListener onHostPathBrowse() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final FileDialog fileDialog = new FileDialog(getShell());
				final String selectedPath = fileDialog.open();
				if (selectedPath != null) {
					model.setHostPathMount(selectedPath);
				}
			}
		};
	}

	/**
	 * Creates an {@link IContentProposalProvider} to propose
	 * {@link IDockerContainer} names based on the current text.
	 * 
	 * @param items
	 * @return
	 */
	private IContentProposalProvider getContainerNameContentProposalProvider(
			final Combo containerSelectionCombo) {
		return new IContentProposalProvider() {

			@Override
			public IContentProposal[] getProposals(final String contents,
					final int position) {
				final List<IContentProposal> proposals = new ArrayList<IContentProposal>();
				for (String containerName : containerSelectionCombo
						.getItems()) {
					if (containerName.contains(contents)) {
						proposals.add(new ContentProposal(containerName,
								containerName, containerName, position));
					}
				}
				return proposals.toArray(new IContentProposal[0]);
			}
		};
	}

	private IChangeListener onDataVolumeSettingsChanged(
			final Label errorMessageIcon, final Label errorMessageLabel) {

		return new IChangeListener() {

			@Override
			public void handleChange(ChangeEvent event) {
				final IStatus status = validateInput(errorMessageLabel);
				if (status.isOK()) {
					errorMessageIcon.setVisible(false);
					errorMessageLabel.setVisible(false);
					setOkButtonEnabled(true);
				} else if (status.matches(IStatus.WARNING)) {
					errorMessageIcon.setVisible(true);
					errorMessageIcon.setImage(
							SWTImagesFactory.DESC_WARNING.createImage());
					errorMessageLabel.setVisible(true);
					errorMessageLabel.setText(status.getMessage());
					setOkButtonEnabled(true);
				} else if (status.matches(IStatus.ERROR)) {
					errorMessageIcon.setVisible(true);
					errorMessageIcon.setImage(
							SWTImagesFactory.DESC_ERROR.createImage());
					errorMessageLabel.setVisible(true);
					errorMessageLabel.setText(status.getMessage());
					setOkButtonEnabled(true);
				}
			}
		};
	}

	private IStatus validateInput(final Label errorMessageLabel) {
		final MountType mountType = model.getMountType();
		final String hostPath = model.getHostPathMount();
		if (mountType == MountType.HOST_FILE_SYSTEM
				&& (hostPath == null || hostPath.isEmpty())) {
			return ValidationStatus.ok();
		} else if (mountType == MountType.HOST_FILE_SYSTEM
				&& !new File(hostPath).exists()) {
			return ValidationStatus
					.warning("The specified path does not exist on the host."); //$NON-NLS-1$
		} else if (mountType == MountType.CONTAINER) {
			final IDockerContainer container = WizardUtils
					.getContainer(connection, model.getContainerMount());
			if (container == null) {
				return ValidationStatus.error("");
			}
			final IDockerContainerInfo selectedContainerInfo = container.info();
			if (!selectedContainerInfo.volumes()
					.containsKey(model.getContainerPath())) {
				return ValidationStatus
						.warning("The selected container does not define a "
								+ model.getContainerPath() + " volume."); //$NON-NLS-1$
			}
		}
		return ValidationStatus.ok();
	}

	private void setOkButtonEnabled(final boolean enabled) {
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

}
