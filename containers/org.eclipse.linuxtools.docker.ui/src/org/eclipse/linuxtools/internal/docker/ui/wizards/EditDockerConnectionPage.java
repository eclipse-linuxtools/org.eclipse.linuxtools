/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.linuxtools.internal.docker.core.UnixSocketConnectionSettings;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.spotify.docker.client.DockerCertificateException;

/**
 * {@link WizardPage} to edit an {@link IDockerConnection}
 *
 */
public class EditDockerConnectionPage extends WizardPage {

	private final DataBindingContext dbc;

	private final EditDockerConnectionPageModel model;

	/**
	 * Constructor.
	 * 
	 * @param currentConnection
	 *            the {@link IDockerConnection} to edit
	 */
	public EditDockerConnectionPage(final IDockerConnection currentConnection) {
		super("DockerConnectionPage", //$NON-NLS-1$
				WizardMessages.getString("DockerConnectionPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("DockerConnectionPage.msg")); //$NON-NLS-1$
		this.model = new EditDockerConnectionPageModel(currentConnection);
		this.dbc = new DataBindingContext();
	}

	@Override
	public void createControl(final Composite parent) {
		final ScrolledComposite scrollTop = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);

		final Composite container = new Composite(scrollTop, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.applyTo(container);
		createConnectionSettingsContainer(container);
		// attach the Databinding context status to this wizard page.
		WizardPageSupport.create(this, this.dbc);

		scrollTop.setContent(container);
		Point point = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollTop.setSize(point);
		scrollTop.setMinSize(point);
		setControl(container);
	}

	@Override
	public void dispose() {
		if (dbc != null) {
			dbc.dispose();
		}
		super.dispose();
	}

	/**
	 * Creates the connection settings container, where the user can choose how
	 * to connect to the docker daemon (using sockets or TCP with SSL - or not)
	 * 
	 * @param parent
	 *            the parent container (ie, the main container in the preference
	 *            page)
	 */
	@SuppressWarnings("unchecked")
	private void createConnectionSettingsContainer(final Composite parent) {
		final int COLUMNS = 3;
		final int INDENT = 20;
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(container);

		// Connection name
		final Label connectionNameLabel = new Label(container, SWT.NONE);
		connectionNameLabel.setText(
				WizardMessages.getString("DockerConnectionPage.nameLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(connectionNameLabel);
		final Text connectionNameText = new Text(container, SWT.BORDER);
		connectionNameText.setToolTipText(
				WizardMessages.getString("DockerConnectionPage.nameTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(connectionNameText);

		// Unix socket
		final Button unixSocketBindingModeButton = new Button(container,
				SWT.RADIO);
		unixSocketBindingModeButton.setText(
				WizardMessages.getString("DockerConnectionPage.unixSocket")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(0, 10)
				.span(COLUMNS, 1).applyTo(unixSocketBindingModeButton);

		final Label unixSocketPathLabel = new Label(container, SWT.NONE);
		unixSocketPathLabel.setText(
				WizardMessages.getString("DockerConnectionPage.location")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).applyTo(unixSocketPathLabel);
		final Text unixSocketPathText = new Text(container, SWT.BORDER);
		unixSocketPathText.setToolTipText(WizardMessages
				.getString("DockerConnectionPage.unixPathTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(unixSocketPathText);

		final Button unixSocketPathBrowseButton = new Button(container,
				SWT.BUTTON1);
		unixSocketPathBrowseButton.setText(
				WizardMessages.getString("DockerConnectionPage.browseButton")); //$NON-NLS-1$
		unixSocketPathBrowseButton
				.addSelectionListener(onBrowseUnixSocketPath());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(unixSocketPathBrowseButton);

		// TCP connection
		final Button tcpConnectionBindingModeButton = new Button(container,
				SWT.RADIO);
		tcpConnectionBindingModeButton.setText(
				WizardMessages.getString("DockerConnectionPage.tcpConnection")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).applyTo(tcpConnectionBindingModeButton);

		final Label tcpHostLabel = new Label(container, SWT.NONE);
		tcpHostLabel.setText(
				WizardMessages.getString("DockerConnectionPage.hostLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).applyTo(tcpHostLabel);

		final Text tcpHostText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false).applyTo(tcpHostText);

		final Button tcpAuthButton = new Button(container, SWT.CHECK);
		tcpAuthButton.setText(
				WizardMessages.getString("DockerConnectionPage.tcpAuthButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).span(3, 1).applyTo(tcpAuthButton);

		final Label tcpCertPathLabel = new Label(container, SWT.NONE);
		tcpCertPathLabel.setText(
				WizardMessages.getString("DockerConnectionPage.tcpPathLabel")); //$NON-NLS-1$
		tcpCertPathLabel.setToolTipText(WizardMessages
				.getString("DockerConnectionPage.tcpPathTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT * 2, 0).applyTo(tcpCertPathLabel);
		final Text tcpCertPathText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(tcpCertPathText);
		final Button tcpCertPathBrowseButton = new Button(container,
				SWT.BUTTON1);
		tcpCertPathBrowseButton.setText(
				WizardMessages.getString("DockerConnectionPage.browseButton")); //$NON-NLS-1$
		tcpCertPathBrowseButton.addSelectionListener(onBrowseTcpCertPath());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(tcpCertPathBrowseButton);

		// the 'test connection' button
		final Button testConnectionButton = new Button(container, SWT.NONE);
		testConnectionButton.setText(WizardMessages
				.getString("DockerConnectionPage.testConnection")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(3, 1)
				.indent(0, 10).align(SWT.END, SWT.CENTER)
				.applyTo(testConnectionButton);
		testConnectionButton
				.addSelectionListener(onTestConnectionButtonSelection());

		// observe
		final IObservableValue<String> connectionNameModelObservable = BeanProperties
				.value(EditDockerConnectionPageModel.class,
						EditDockerConnectionPageModel.CONNECTION_NAME)
				.observe(model);
		final IObservableValue<Boolean> unixSocketBindingModeModelObservable = BeanProperties
				.value(EditDockerConnectionPageModel.class,
						EditDockerConnectionPageModel.UNIX_SOCKET_BINDING_MODE)
				.observe(model);
		final IObservableValue<String> unixSocketPathModelObservable = BeanProperties
				.value(EditDockerConnectionPageModel.class,
						EditDockerConnectionPageModel.UNIX_SOCKET_PATH)
				.observe(model);

		final IObservableValue<Boolean> tcpConnectionBindingModeModelObservable = BeanProperties
				.value(EditDockerConnectionPageModel.class,
						EditDockerConnectionPageModel.TCP_CONNECTION_BINDING_MODE)
				.observe(model);
		final IObservableValue<String> tcpCertPathModelObservable = BeanProperties
				.value(EditDockerConnectionPageModel.class,
						EditDockerConnectionPageModel.TCP_CERT_PATH)
				.observe(model);
		final IObservableValue<Boolean> tcpTlsVerifyModelObservable = BeanProperties
				.value(EditDockerConnectionPageModel.class,
						EditDockerConnectionPageModel.TCP_TLS_VERIFY)
				.observe(model);
		final IObservableValue<String> tcpHostModelObservable = BeanProperties
				.value(EditDockerConnectionPageModel.class,
						EditDockerConnectionPageModel.TCP_HOST)
				.observe(model);

		// group controls to easily enable/disable them
		final Control[] unixSocketControls = new Control[] { unixSocketPathText,
				unixSocketPathLabel, unixSocketPathBrowseButton };
		final Control[] tcpConnectionControls = new Control[] { tcpHostText,
				tcpHostLabel, tcpAuthButton };
		final Control[] tcpAuthControls = new Control[] { tcpCertPathText,
				tcpCertPathLabel, tcpCertPathBrowseButton };
		unixSocketBindingModeModelObservable.addChangeListener(
				onUnixSocketBindingSelection(unixSocketControls));
		tcpConnectionBindingModeModelObservable.addChangeListener(
				onTcpConnectionBindingSelection(tcpConnectionControls,
						tcpAuthControls));
		tcpTlsVerifyModelObservable
				.addValueChangeListener(onTcpAuthSelection(tcpAuthControls));

		// bind controls to model
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(connectionNameText),
				connectionNameModelObservable);
		dbc.bindValue(
				WidgetProperties.selection()
						.observe(unixSocketBindingModeButton),
				unixSocketBindingModeModelObservable);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(unixSocketPathText),
				unixSocketPathModelObservable);
		dbc.bindValue(
				WidgetProperties.selection()
						.observe(tcpConnectionBindingModeButton),
				tcpConnectionBindingModeModelObservable);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(tcpHostText),
				tcpHostModelObservable);
		dbc.bindValue(WidgetProperties.selection().observe(tcpAuthButton),
				tcpTlsVerifyModelObservable);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(tcpCertPathText),
				tcpCertPathModelObservable);
		// validations will be performed when the user changes the value
		// only, not at the dialog opening
		dbc.addValidationStatusProvider(
				new ConnectionNameValidator(connectionNameModelObservable,
						this.model.getInitialConnectionName()));
		dbc.addValidationStatusProvider(
				new UnixSocketValidator(unixSocketBindingModeModelObservable,
						unixSocketPathModelObservable));
		dbc.addValidationStatusProvider(
				new TcpHostValidator(tcpConnectionBindingModeModelObservable,
						tcpHostModelObservable));
		dbc.addValidationStatusProvider(new TcpCertificatesValidator(
				tcpConnectionBindingModeModelObservable,
				tcpTlsVerifyModelObservable, tcpCertPathModelObservable));

		// give focus to connectionName text at first
		connectionNameText.setFocus();
		// set widgets initial state
		updateWidgetsState(unixSocketControls, tcpConnectionControls,
				tcpAuthControls);
	}

	private SelectionListener onBrowseUnixSocketPath() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final FileDialog fileDialog = new FileDialog(getShell());
				final String selectedPath = fileDialog.open();
				if (selectedPath != null) {
					model.setUnixSocketPath("unix://" + selectedPath); //$NON-NLS-1$
				}
			}
		};
	}

	private SelectionListener onBrowseTcpCertPath() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final DirectoryDialog directoryDialog = new DirectoryDialog(
						getShell());
				directoryDialog.setFilterPath(model.getTcpCertPath());
				final String selectedPath = directoryDialog.open();
				if (selectedPath != null) {
					model.setTcpCertPath(selectedPath);
				}
			}
		};
	}

	private void updateWidgetsState(final Control[] unixSocketControls,
			final Control[] tcpConnectionControls,
			final Control[] tcpAuthControls) {
		setWidgetsEnabled(
				model.isTcpConnectionBindingMode() && model.isTcpTLSVerify(),
				tcpAuthControls);
		setWidgetsEnabled(model.isTcpConnectionBindingMode(),
				tcpConnectionControls);
		setWidgetsEnabled(model.isUnixSocketBindingMode(), unixSocketControls);
	}

	private IChangeListener onUnixSocketBindingSelection(
			final Control[] unixSocketControls) {
		return event -> setWidgetsEnabled(model.isUnixSocketBindingMode(),
				unixSocketControls);
	}

	private IChangeListener onTcpConnectionBindingSelection(
			final Control[] tcpConnectionControls,
			final Control[] tcpAuthControls) {
		return event -> {
			setWidgetsEnabled(model.isTcpConnectionBindingMode()
					&& model.isTcpTLSVerify(), tcpAuthControls);
			// and give focus to the first given control (if applicable)
			setWidgetsEnabled(model.isTcpConnectionBindingMode(),
					tcpConnectionControls);
		};
	}

	private IValueChangeListener<Boolean> onTcpAuthSelection(
			final Control[] tcpAuthControls) {
		return event -> setWidgetsEnabled(
				model.isTcpConnectionBindingMode() && model.isTcpTLSVerify(),
				tcpAuthControls);
	}

	private void setWidgetsEnabled(final boolean enabled,
			final Control... controls) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
		// set the focus on the fist element of the group.
		if (enabled) {
			for (Control control : controls) {
				if (control instanceof Text) {
					control.setFocus();
					break;
				}
			}
		}
	}

	/**
	 * Verifies that the given connection settings work by trying to connect to
	 * the target Docker daemon
	 * 
	 * @return
	 */
	private SelectionListener onTestConnectionButtonSelection() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getWizard().getContainer().run(true, false, monitor -> {
						monitor.beginTask(WizardMessages.getString(
										"DockerConnectionPage.pingTask"), //$NON-NLS-1$
								IProgressMonitor.UNKNOWN);
						try {
							final DockerConnection dockerConnection = getDockerConnection();
							dockerConnection.open(false);
							dockerConnection.close();
							// ping succeeded
							displaySuccessDialog();
						} catch (DockerException e1) {
							// only log if there's an underlying cause.
							if (e1.getCause() != null) {
								Activator.log(e1);
							}
							displayErrorDialog();
						}
					});
				} catch (InvocationTargetException | InterruptedException o_O) {
					Activator.log(o_O);
				}

			}

			private void displaySuccessDialog() {
				displayDialog(
						WizardMessages
								.getString("DockerConnectionPage.success"), //$NON-NLS-1$
						WizardMessages
								.getString("DockerConnectionPage.pingSuccess"), //$NON-NLS-1$
						SWT.ICON_INFORMATION, new String[] { WizardMessages
								.getString("DockerConnectionPage.ok") } //$NON-NLS-1$
				);
			}

			private void displayErrorDialog() {
				displayDialog(
						WizardMessages
								.getString("DockerConnectionPage.failure"), //$NON-NLS-1$
						WizardMessages
								.getString("DockerConnectionPage.pingFailure"), //$NON-NLS-1$
						SWT.ICON_ERROR, new String[] { WizardMessages
								.getString("DockerConnectionPage.ok") } //$NON-NLS-1$
				);
			}

			private void displayDialog(final String dialogTitle,
					final String dialogMessage, final int icon,
					final String[] buttonLabels) {
				Display.getDefault()
						.syncExec(() -> new MessageDialog(
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getShell(),
								dialogTitle, null, dialogMessage, icon,
								buttonLabels, 0).open());
			}

		};
	}

	/**
	 * Opens a new {@link DockerConnection} using the settings of this
	 * {@link EditDockerConnectionPage}.
	 * 
	 * @return
	 * @throws DockerCertificateException
	 */
	protected DockerConnection getDockerConnection() {
		if (model.getBindingMode() == UNIX_SOCKET) {
			return new DockerConnection.Builder()
					.name(model.getConnectionName())
					.unixSocketConnection(new UnixSocketConnectionSettings(
							model.getUnixSocketPath()));
		} else {
			return new DockerConnection.Builder()
					.name(model.getConnectionName())
					.tcpConnection(new TCPConnectionSettings(model.getTcpHost(),
							model.getTcpCertPath()));
		}
	}

	private static class ConnectionNameValidator extends MultiValidator {

		private final IObservableValue<String> connectionNameModelObservable;

		private final String initialConnectionName;

		public ConnectionNameValidator(
				final IObservableValue<String> connectionNameModelObservable,
				final String initialConnectionName) {
			this.connectionNameModelObservable = connectionNameModelObservable;
			this.initialConnectionName = initialConnectionName;
		}

		@Override
		public IObservableList<IObservableValue<String>> getTargets() {
			WritableList<IObservableValue<String>> targets = new WritableList<>();
			targets.add(connectionNameModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final String connectionName = this.connectionNameModelObservable
					.getValue();
			if (connectionName == null || connectionName.isEmpty()) {
				return ValidationStatus.error(WizardMessages.getString(
						"DockerConnectionPage.validation.missingConnectionName.msg")); //$NON-NLS-1$
			} else if (!connectionName.equals(this.initialConnectionName)
					&& DockerConnectionManager.getInstance()
							.findConnection(connectionName) != null) {
				return ValidationStatus.error(WizardMessages.getString(
						"DockerConnectionPage.validation.duplicateConnectionName.msg")); //$NON-NLS-1$
			}
			return ValidationStatus.ok();
		}
	}

	private static class UnixSocketValidator extends MultiValidator {

		private final IObservableValue<Boolean> unixSocketBindingModeModelObservable;
		private final IObservableValue<String> unixSocketPathModelObservable;

		public UnixSocketValidator(
				final IObservableValue<Boolean> unixSocketBindingModeModelObservable,
				final IObservableValue<String> unixSocketPathModelObservable) {
			this.unixSocketBindingModeModelObservable = unixSocketBindingModeModelObservable;
			this.unixSocketPathModelObservable = unixSocketPathModelObservable;
		}

		@Override
		public IObservableList<IObservableValue<String>> getTargets() {
			WritableList<IObservableValue<String>> targets = new WritableList<>();
			targets.add(unixSocketPathModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final Boolean unixSocketBindingMode = this.unixSocketBindingModeModelObservable
					.getValue();
			final String unixSocketPath = this.unixSocketPathModelObservable
					.getValue();
			if (unixSocketBindingMode) {
				if (unixSocketPath == null || unixSocketPath.isEmpty()) {
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.missingUnixSocket.msg")); //$NON-NLS-1$
				}
				try {
					final URI unixSocketURI = new URI(unixSocketPath);
					if (unixSocketURI.getScheme() != null
							&& !unixSocketURI.getScheme().equals("unix")) { //$NON-NLS-1$
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.invalidUnixSocketScheme.msg")); //$NON-NLS-1$
					}
					if (unixSocketURI.getPath() != null) {
						final File unixSocket = new File(
								unixSocketURI.getPath());
						if (!unixSocket.exists()) {
							return ValidationStatus
									.error(WizardMessages.getString(
											"DockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$
						} else if (!unixSocket.canRead()
								|| !unixSocket.canWrite()) {
							return ValidationStatus
									.error(WizardMessages.getString(
											"DockerConnectionPage.validation.unreadableUnixSocket.msg")); //$NON-NLS-1$
						}
					} else {
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$

					}
				} catch (URISyntaxException e) {
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$

				}
			}
			return ValidationStatus.ok();
		}

	}

	private static class TcpHostValidator extends MultiValidator {

		private final IObservableValue<Boolean> tcpConnectionBindingModeModelObservable;
		private final IObservableValue<String> tcpHostModelObservable;

		public TcpHostValidator(
				final IObservableValue<Boolean> tcpConnectionBindingModeModelObservable,
				final IObservableValue<String> tcpHostModelObservable) {
			this.tcpConnectionBindingModeModelObservable = tcpConnectionBindingModeModelObservable;
			this.tcpHostModelObservable = tcpHostModelObservable;
		}

		@Override
		public IObservableList<IObservableValue<String>> getTargets() {
			WritableList<IObservableValue<String>> targets = new WritableList<>();
			targets.add(tcpHostModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final Boolean tcpConnectionBindingMode = this.tcpConnectionBindingModeModelObservable
					.getValue();
			final String tcpHost = this.tcpHostModelObservable.getValue();
			if (tcpConnectionBindingMode) {
				if (tcpHost == null || tcpHost.isEmpty()) {
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.missingTcpConnectionURI.msg")); //$NON-NLS-1$
				}
				try {
					final URI uri = new URI(tcpHost);
					final String scheme = uri.getScheme() != null
							? uri.getScheme().toLowerCase() : null;
					final String host = uri.getHost();
					final int port = uri.getPort();
					if (scheme != null
							&& !(scheme.equals("tcp") || scheme.equals("http") //$NON-NLS-1$ //$NON-NLS-2$
									|| scheme.equals("https"))) { //$NON-NLS-1$
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.invalidTcpConnectionScheme.msg")); //$NON-NLS-1$
					} else if (host == null) {
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.invalidTcpConnectionHost.msg")); //$NON-NLS-1$

					} else if (port == -1) {
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.invalidTcpConnectionPort.msg")); //$NON-NLS-1$

					}
				} catch (URISyntaxException e) {
					// URI is not valid
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.invalidTcpConnectionURI.msg")); //$NON-NLS-1$
				}
			}
			return ValidationStatus.ok();
		}

	}

	private static class TcpCertificatesValidator extends MultiValidator {

		private final IObservableValue<Boolean> tcpConnectionBindingModeModelObservable;
		private final IObservableValue<Boolean> tcpTlsVerifyModelObservable;
		private final IObservableValue<String> tcpCertPathModelObservable;

		public TcpCertificatesValidator(
				final IObservableValue<Boolean> tcpConnectionBindingModeModelObservable,
				final IObservableValue<Boolean> tcpTlsVerifyModelObservable,
				final IObservableValue<String> tcpCertPathModelObservable) {
			this.tcpConnectionBindingModeModelObservable = tcpConnectionBindingModeModelObservable;
			this.tcpTlsVerifyModelObservable = tcpTlsVerifyModelObservable;
			this.tcpCertPathModelObservable = tcpCertPathModelObservable;
		}

		@Override
		public IObservableList<IObservableValue<String>> getTargets() {
			WritableList<IObservableValue<String>> targets = new WritableList<>();
			targets.add(tcpCertPathModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final Boolean tcpConnectionBindingMode = this.tcpConnectionBindingModeModelObservable
					.getValue();
			final Boolean tcpTlsVerify = this.tcpTlsVerifyModelObservable
					.getValue();
			final String tcpCertPath = this.tcpCertPathModelObservable
					.getValue();
			if (tcpConnectionBindingMode && tcpTlsVerify) {
				if (tcpCertPath == null || tcpCertPath.isEmpty()) {
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.missingTcpCertPath.msg")); //$NON-NLS-1$
				}
				final File tcpCert = new File(tcpCertPath);
				if (!tcpCert.exists()) {
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.invalidTcpCertPath.msg")); //$NON-NLS-1$
				} else if (!tcpCert.canRead() || !tcpCert.canRead()) {
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.unreadableTcpCertPath.msg")); //$NON-NLS-1$
				}
			}
			return ValidationStatus.ok();
		}

	}

}
