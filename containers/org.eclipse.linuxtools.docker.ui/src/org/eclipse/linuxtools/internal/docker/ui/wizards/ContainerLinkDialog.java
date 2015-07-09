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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ContainerLinkModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ContainerLinkDialog extends Dialog {

	private final IDockerConnection connection;

	private final ContainerLinkDialogModel model;

	private final List<String> containerNames;

	private final DataBindingContext dbc = new DataBindingContext();

	protected ContainerLinkDialog(final Shell shell,
			final IDockerConnection connection) {
		super(shell);
		this.connection = connection;
		this.model = new ContainerLinkDialogModel();
		this.containerNames = WizardUtils.getContainerNames(connection);
	}

	public ContainerLinkDialog(final Shell shell,
			final IDockerConnection connection,
			final ContainerLinkModel selectedContainerLink) {
		super(shell);
		this.connection = connection;
		this.model = new ContainerLinkDialogModel(selectedContainerLink);
		this.containerNames = WizardUtils.getContainerNames(connection);
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		shell.setText(WizardMessages.getString("ContainerLinkDialog.title")); //$NON-NLS-1$
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, super.getInitialSize().y);
	}

	/**
	 * Disable the 'OK' button by default
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		final Button button = super.createButton(parent, id, label,
				defaultButton);
		if (id == IDialogConstants.OK_ID) {
			button.setEnabled(false);
		}
		return button;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final int COLUMNS = 2;
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(10, 10)
				.applyTo(container);
		final Label explanationLabel = new Label(container, SWT.NONE);
		explanationLabel.setText(WizardMessages
				.getString("ContainerLinkDialog.explanationLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(false, false).applyTo(explanationLabel);
		final Label containerLabel = new Label(container, SWT.NONE);
		containerLabel.setText(
				WizardMessages.getString("ContainerLinkDialog.containerLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(containerLabel);
		final Combo containerSelectionCombo = new Combo(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(containerSelectionCombo);
		final ComboViewer containerSelectionComboViewer = new ComboViewer(
				containerSelectionCombo);
		containerSelectionComboViewer
				.setContentProvider(new ArrayContentProvider());
		containerSelectionComboViewer.setInput(model.getContainerNames());
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
		final Label aliasLabel = new Label(container, SWT.NONE);
		aliasLabel.setText(
				WizardMessages.getString("ContainerLinkDialog.aliasLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(aliasLabel);
		final Text containerAliasText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(containerAliasText);
		// error message
		final Label errorMessageLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(errorMessageLabel);

		final ISWTObservableValue containerNameObservable = WidgetProperties
				.selection().observe(containerSelectionComboViewer.getCombo());

		dbc.bindValue(containerNameObservable,
				BeanProperties
						.value(ContainerLinkDialogModel.class,
								ContainerLinkDialogModel.CONTAINER_NAME)
						.observe(model));
		final ISWTObservableValue containerAliasObservable = WidgetProperties
				.text(SWT.Modify).observe(containerAliasText);

		dbc.bindValue(containerAliasObservable,
				BeanProperties
						.value(ContainerLinkDialogModel.class,
								ContainerLinkDialogModel.CONTAINER_ALIAS)
						.observe(model));
		containerNameObservable.addValueChangeListener(
onContainerLinkSettingsChanged());
		containerAliasObservable.addValueChangeListener(
onContainerLinkSettingsChanged());
		return container;
	}

	private IValueChangeListener onContainerLinkSettingsChanged() {
		return new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				validateInput();
			}
		};
	}

	public String getContainerName() {
		return model.getContainerName();
	}

	public String getContainerAlias() {
		return model.getContainerAlias();
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
				final List<IContentProposal> proposals = new ArrayList<>();
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

	private void validateInput() {
		final String selectedContainerName = model.getContainerName();
		final Object[] containerNames = model.getContainerNames().toArray();
		final String containerAlias = model.getContainerAlias();
		if (selectedContainerName == null || selectedContainerName.isEmpty()) {
			setOkButtonEnabled(false);
		} else if (Arrays.binarySearch(containerNames, 0, containerNames.length,
				selectedContainerName) < 0) {
			setOkButtonEnabled(false);
		} else if (containerAlias == null || containerAlias.isEmpty()) {
			setOkButtonEnabled(false);
			return;
		} else {
			setOkButtonEnabled(true);
		}
	}

	private void setOkButtonEnabled(final boolean enabled) {
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	class ContainerLinkDialogModel extends BaseDatabindingModel {

		public static final String CONTAINER_NAME = "containerName"; //$NON-NLS-1$

		public static final String CONTAINER_ALIAS = "containerAlias"; //$NON-NLS-1$

		private String containerName;

		private String containerAlias;

		public ContainerLinkDialogModel() {
		}

		public ContainerLinkDialogModel(
				final ContainerLinkModel selectedContainerLink) {
			this();
			this.containerName = selectedContainerLink.getContainerName();
			this.containerAlias = selectedContainerLink.getContainerAlias();
		}

		public IDockerConnection getConnection() {
			return connection;
		}

		private List<String> getContainerNames() {
			return containerNames;
		}

		public List<IDockerContainer> getContainers() {
			return getConnection().getContainers();
		}

		public String getContainerName() {
			return containerName;
		}

		public void setContainerName(final String containerName) {
			firePropertyChange(CONTAINER_NAME, this.containerName,
					this.containerName = containerName);
		}

		public String getContainerAlias() {
			return containerAlias;
		}

		public void setContainerAlias(final String containerAlias) {
			firePropertyChange(CONTAINER_ALIAS, this.containerAlias,
					this.containerAlias = containerAlias);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((containerAlias == null) ? 0
					: containerAlias.hashCode());
			result = prime * result
					+ ((containerName == null) ? 0 : containerName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ContainerLinkDialogModel other = (ContainerLinkDialogModel) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (containerAlias == null) {
				if (other.containerAlias != null)
					return false;
			} else if (!containerAlias.equals(other.containerAlias))
				return false;
			if (containerName == null) {
				if (other.containerName != null)
					return false;
			} else if (!containerName.equals(other.containerName))
				return false;
			return true;
		}

		private ContainerLinkDialog getOuterType() {
			return ContainerLinkDialog.this;
		}

	}

}
