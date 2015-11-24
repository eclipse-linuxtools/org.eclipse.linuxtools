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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;
import org.eclipse.linuxtools.internal.docker.ui.validators.ImageNameValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * {@link Dialog} to specify Docker Image build options at launch time.
 */
public class ImageBuildDialog extends Dialog {

	private final ImageBuildDialogModel model = new ImageBuildDialogModel();
	private final DataBindingContext dbc = new DataBindingContext();

	public ImageBuildDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		shell.setText(WizardMessages.getString("ImageBuildDialog.title")); //$NON-NLS-1$
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
		explanationLabel.setText(
				WizardMessages.getString("ImageBuildDialog.explanationLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(false, false).applyTo(explanationLabel);
		final Label containerLabel = new Label(container, SWT.NONE);
		containerLabel.setText(
				WizardMessages.getString("ImageBuildDialog.connectionLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(containerLabel);
		final Combo containerSelectionCombo = new Combo(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(containerSelectionCombo);
		final ComboViewer connectionSelectionComboViewer = new ComboViewer(
				containerSelectionCombo);
		connectionSelectionComboViewer
				.setContentProvider(new ArrayContentProvider());
		final List<String> connectionNames = model.getConnectionNames();
		connectionSelectionComboViewer.setInput(connectionNames);
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
				}, getConnectionNameContentProposalProvider(
						containerSelectionCombo),
				null, null);
		final Label repoNameLabel = new Label(container, SWT.NONE);
		repoNameLabel.setText(
				WizardMessages.getString("ImageBuildDialog.repoNameLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(repoNameLabel);
		final Text repoNameText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(repoNameText);
		final ISWTObservableValue connnectionNameObservable = WidgetProperties
				.selection().observe(connectionSelectionComboViewer.getCombo());
		// pre-select with first connection
		if (!connectionNames.isEmpty()) {
			model.setConnectionName(connectionNames.get(0));
		}
		// error message
		final Composite errorContainer = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, true).applyTo(errorContainer);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(2)
				.applyTo(errorContainer);

		final Label errorMessageIcon = new Label(errorContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.hint(20, SWT.DEFAULT).applyTo(errorMessageIcon);
		final Label errorMessageLabel = new Label(errorContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(errorMessageLabel);
		dbc.bindValue(connnectionNameObservable,
				BeanProperties
						.value(ImageBuildDialogModel.class,
								ImageBuildDialogModel.CONNECTION_NAME)
						.observe(model));
		final ISWTObservableValue repoNameObservable = WidgetProperties
				.text(SWT.Modify).observe(repoNameText);

		dbc.bindValue(repoNameObservable,
				BeanProperties
						.value(ImageBuildDialogModel.class,
								ImageBuildDialogModel.REPO_NAME)
						.observe(model));
		// must be called after bindings were set
		setupValidationSupport(errorMessageIcon, errorMessageLabel);
		return container;
	}

	private void setupValidationSupport(final Label errorMessageIcon,
			final Label errorMessageLabel) {
		for (@SuppressWarnings("unchecked")
		Iterator<Binding> iterator = dbc.getBindings().iterator(); iterator
				.hasNext();) {
			final Binding binding = iterator.next();
			binding.getModel().addChangeListener(onBuildSettingsChanged(
					errorMessageIcon, errorMessageLabel));
		}
	}

	private IChangeListener onBuildSettingsChanged(final Label errorMessageIcon,
			final Label errorMessageLabel) {

		return new IChangeListener() {

			@Override
			public void handleChange(ChangeEvent event) {
				final IStatus status = validateInput();
				if (Display.getCurrent() == null) {
					return;
				}
				Display.getCurrent().syncExec(new Runnable() {

					@Override
					public void run() {
						if (status.isOK()) {
							errorMessageIcon.setVisible(false);
							errorMessageLabel.setVisible(false);
							setOkButtonEnabled(true);
						} else if (status.matches(IStatus.WARNING)) {
							errorMessageIcon.setVisible(true);
							errorMessageIcon
									.setImage(SWTImagesFactory.DESC_WARNING
											.createImage());
							errorMessageLabel.setVisible(true);
							errorMessageLabel.setText(status.getMessage());
							setOkButtonEnabled(true);
						} else if (status.matches(IStatus.ERROR)) {
							if (status.getMessage() != null
									&& !status.getMessage().isEmpty()) {
								errorMessageIcon.setVisible(true);
								errorMessageIcon
										.setImage(SWTImagesFactory.DESC_ERROR
												.createImage());
								errorMessageLabel.setVisible(true);
								errorMessageLabel.setText(status.getMessage());
							}
							setOkButtonEnabled(false);
						}
					}
				});
			}
		};
	}

	/**
	 * Validates that the selected {@link IDockerConnection} exists and that the
	 * optional image name is valid.
	 * 
	 * @return a validation status
	 */
	private IStatus validateInput() {
		final String selectedConnectionName = model.getConnectionName();
		final String repoName = model.getRepoName();
		if (selectedConnectionName == null
				|| selectedConnectionName.isEmpty()) {
			return Status.CANCEL_STATUS;
		} else
			if (!model.getConnectionNames().contains(selectedConnectionName)) {
			return ValidationStatus.error(WizardMessages.getFormattedString(
					"ImageBuildDialog.error.unknownConnection", //$NON-NLS-1$
					selectedConnectionName));
		} else if (repoName == null || repoName.isEmpty()) {
			return ValidationStatus.warning(WizardMessages
					.getString("ImageBuildDialog.warning.missingRepoName")); //$NON-NLS-1$
		} else {
			final ImageNameValidator imageNameValidator = new ImageNameValidator();
			return imageNameValidator.validate(repoName);
		}
	}

	public IDockerConnection getConnection() {
		return DockerConnectionManager.getInstance()
				.findConnection(model.connectionName);
	}

	public String getRepoName() {
		return model.getRepoName();
	}

	/**
	 * Creates an {@link IContentProposalProvider} to propose
	 * {@link IDockerContainer} names based on the current text.
	 * 
	 * @param items
	 * @return
	 */
	private IContentProposalProvider getConnectionNameContentProposalProvider(
			final Combo connectionNamesSelectionCombo) {
		return new IContentProposalProvider() {

			@Override
			public IContentProposal[] getProposals(final String contents,
					final int position) {
				final List<IContentProposal> proposals = new ArrayList<>();
				for (String connectionName : connectionNamesSelectionCombo
						.getItems()) {
					if (connectionName.contains(contents)) {
						proposals.add(new ContentProposal(connectionName,
								connectionName, connectionName, position));
					}
				}
				return proposals.toArray(new IContentProposal[0]);
			}
		};
	}

	private void setOkButtonEnabled(final boolean enabled) {
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	class ImageBuildDialogModel extends BaseDatabindingModel {

		public static final String CONNECTION_NAME = "connectionName"; //$NON-NLS-1$

		public static final String REPO_NAME = "repoName"; //$NON-NLS-1$

		private String connectionName;

		private String repoName;

		private final List<String> connectionNames = DockerConnectionManager
				.getInstance().getConnectionNames();

		public String getRepoName() {
			return repoName;
		}

		public List<String> getConnectionNames() {
			return connectionNames;
		}

		public void setRepoName(final String repoName) {
			firePropertyChange(REPO_NAME, this.repoName,
					this.repoName = repoName);
		}

		public String getConnectionName() {
			return connectionName;
		}

		public void setConnectionName(final String connectionName) {
			firePropertyChange(CONNECTION_NAME, this.connectionName,
					this.connectionName = connectionName);
		}

	}

}
