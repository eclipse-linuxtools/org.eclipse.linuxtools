/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection.Builder;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link WizardPage} to input the settings to connect to a Docker
 * engine/daemon.
 *
 */
public class NewDockerConnectionPage extends WizardPage {

	private final DataBindingContext dbc;
	private final NewDockerConnectionPageModel model;

	public NewDockerConnectionPage() {
		super("NewDockerConnectionPage", //$NON-NLS-1$
				WizardMessages.getString("NewDockerConnectionPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("NewDockerConnectionPage.msg")); //$NON-NLS-1$
		this.model = new NewDockerConnectionPageModel();
		this.dbc = new DataBindingContext();
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.applyTo(container);
		createConnectionSettingsContainer(container);
		// attach the Databinding context status to this wizard page.
		WizardPageSupport.create(this, this.dbc);
		retrieveDefaultConnectionSettings();
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
				WizardMessages.getString("NewDockerConnectionPage.nameLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(connectionNameLabel);
		final Text connectionNameText = new Text(container, SWT.BORDER);
		connectionNameText.setToolTipText(WizardMessages
				.getString("NewDockerConnectionPage.nameTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(connectionNameText);

		// custom settings checkbox
		final Button customConnectionSettingsButton = new Button(container,
				SWT.CHECK);
		customConnectionSettingsButton
				.setText("Use custom connection settings:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).indent(0, 10).span(COLUMNS, 1)
				.applyTo(customConnectionSettingsButton);
		final Group customSettingsGroup = new Group(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(customSettingsGroup);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(customSettingsGroup);

		// Unix socket
		final Button unixSocketBindingModeButton = new Button(
				customSettingsGroup, SWT.RADIO);
		unixSocketBindingModeButton.setText(
				WizardMessages.getString("NewDockerConnectionPage.unixSocket")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).applyTo(unixSocketBindingModeButton);

		final Label unixSocketPathLabel = new Label(customSettingsGroup,
				SWT.NONE);
		unixSocketPathLabel.setText(
				WizardMessages.getString("NewDockerConnectionPage.location")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).applyTo(unixSocketPathLabel);
		final Text unixSocketPathText = new Text(customSettingsGroup,
				SWT.BORDER);
		unixSocketPathText.setToolTipText(WizardMessages
				.getString("NewDockerConnectionPage.unixPathTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(unixSocketPathText);

		final Button unixSocketPathBrowseButton = new Button(
				customSettingsGroup, SWT.BUTTON1);
		unixSocketPathBrowseButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.browseButton")); //$NON-NLS-1$
		unixSocketPathBrowseButton
				.addSelectionListener(onBrowseUnixSocketPath());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(unixSocketPathBrowseButton);

		// TCP connection
		final Button tcpConnectionBindingModeButton = new Button(
				customSettingsGroup, SWT.RADIO);
		tcpConnectionBindingModeButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.tcpConnection")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).applyTo(tcpConnectionBindingModeButton);

		final Label tcpHostLabel = new Label(customSettingsGroup, SWT.NONE);
		tcpHostLabel.setText(
				WizardMessages.getString("NewDockerConnectionPage.hostLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).applyTo(tcpHostLabel);

		final Text tcpHostText = new Text(customSettingsGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false).applyTo(tcpHostText);

		final Button tcpAuthButton = new Button(customSettingsGroup, SWT.CHECK);
		tcpAuthButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.tcpAuthButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).span(3, 1).applyTo(tcpAuthButton);

		final Label tcpCertPathLabel = new Label(customSettingsGroup, SWT.NONE);
		tcpCertPathLabel.setText(WizardMessages
				.getString("NewDockerConnectionPage.tcpPathLabel")); //$NON-NLS-1$
		tcpCertPathLabel.setToolTipText(WizardMessages
				.getString("NewDockerConnectionPage.tcpPathTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT * 2, 0).applyTo(tcpCertPathLabel);
		final Text tcpCertPathText = new Text(customSettingsGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(tcpCertPathText);
		final Button tcpCertPathBrowseButton = new Button(customSettingsGroup,
				SWT.BUTTON1);
		tcpCertPathBrowseButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.browseButton")); //$NON-NLS-1$
		tcpCertPathBrowseButton.addSelectionListener(onBrowseTcpCertPath());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(tcpCertPathBrowseButton);

		// the 'test connection' button
		final Button testConnectionButton = new Button(container, SWT.NONE);
		testConnectionButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.testConnection")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).align(SWT.END, SWT.CENTER)
				.applyTo(testConnectionButton);
		testConnectionButton
				.addSelectionListener(onTestConnectionButtonSelection());

		// observe
		final IObservableValue connectionNameModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.CONNECTION_NAME)
				.observe(model);
		final IObservableValue unixSocketBindingModeModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.UNIX_SOCKET_BINDING_MODE)
				.observe(model);
		final IObservableValue unixSocketPathModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.UNIX_SOCKET_PATH)
				.observe(model);

		final IObservableValue customConnectionSettingsModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.CUSTOM_SETTINGS)
				.observe(model);
		final IObservableValue tcpConnectionBindingModeModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_CONNECTION_BINDING_MODE)
				.observe(model);
		final IObservableValue tcpCertPathModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_CERT_PATH)
				.observe(model);
		final IObservableValue tcpTlsVerifyModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_TLS_VERIFY)
				.observe(model);
		final IObservableValue tcpHostModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_HOST)
				.observe(model);

		// group controls to easily enable/disable them
		final Control[] bindingModeSelectionControls = new Control[] {
				unixSocketBindingModeButton, tcpConnectionBindingModeButton };
		final Control[] unixSocketControls = new Control[] { unixSocketPathText,
				unixSocketPathLabel, unixSocketPathBrowseButton };
		final Control[] tcpConnectionControls = new Control[] { tcpHostText,
				tcpHostLabel, tcpAuthButton };
		final Control[] tcpAuthControls = new Control[] { tcpCertPathText,
				tcpCertPathLabel, tcpCertPathBrowseButton };
		customConnectionSettingsModelObservable
				.addValueChangeListener(onCustomConnectionSettingsSelection(
						bindingModeSelectionControls, unixSocketControls,
						tcpAuthControls, tcpConnectionControls));
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
						.observe(customConnectionSettingsButton),
				customConnectionSettingsModelObservable);
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
				new ConnectionNameValidator(connectionNameModelObservable));
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
		updateWidgetsState(bindingModeSelectionControls, unixSocketControls,
				tcpConnectionControls, tcpAuthControls);
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
				final String selectedPath = directoryDialog.open();
				if (selectedPath != null) {
					model.setTcpCertPath(selectedPath);
				}
			}
		};
	}

	/**
	 * Sets the default settings by looking for the:
	 * <ul>
	 * <li>a Unix socket at /var/run/docker.sock</li>
	 * <li>the following environment variables:
	 * <ul>
	 * <li>DOCKER_HOST</li>
	 * <li>DOCKER_CERT_PATH</li>
	 * <li>DOCKER_TLS_VERIFY</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * and sets the default connection settings accordingly.
	 */
	private void retrieveDefaultConnectionSettings() {
		// let's run this in a job and show the progress in the wizard
		// progressbar
		try {
			getWizard().getContainer().run(true, true,
					new IRunnableWithProgress() {
						@Override
						public void run(final IProgressMonitor monitor) {
							monitor.beginTask(WizardMessages.getString(
									"NewDockerConnectionPage.retrieveTask"), //$NON-NLS-1$
									1);
							final DockerConnection.Defaults defaults = new DockerConnection.Defaults();
							model.setTcpCertPath(defaults.getTcpCertPath());
							model.setTcpTLSVerify(defaults.getTcpTlsVerify());
							model.setTcpHost(defaults.getTcpHost());
							model.setUnixSocketPath(
									defaults.getUnixSocketPath());
							model.setBindingMode(defaults.getBindingMode());
							model.setCustomSettings(
									!defaults.isSettingsResolved());
							model.setConnectionName(defaults.getName());
							monitor.done();
						}
					});
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}

	}

	private void updateWidgetsState(
			final Control[] bindingModeSelectionControls,
			final Control[] unixSocketControls,
			final Control[] tcpConnectionControls,
			final Control[] tcpAuthControls) {
		setWidgetsEnabled(model.isCustomSettings()
				&& model.isTcpConnectionBindingMode() && model.isTcpTLSVerify(),
				tcpAuthControls);
		setWidgetsEnabled(
				model.isCustomSettings() && model.isTcpConnectionBindingMode(),
				tcpConnectionControls);
		setWidgetsEnabled(
				model.isCustomSettings() && model.isUnixSocketBindingMode(),
				unixSocketControls);
		setWidgetsEnabled(model.isCustomSettings(),
				bindingModeSelectionControls);
	}

	private IValueChangeListener onCustomConnectionSettingsSelection(
			final Control[] bindingModeSelectionControls,
			final Control[] unixSocketControls, final Control[] tcpAuthControls,
			final Control[] tcpConnectionControls) {

		return new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				updateWidgetsState(bindingModeSelectionControls,
						unixSocketControls, tcpConnectionControls,
						tcpAuthControls);
			}
		};
	}

	private IChangeListener onUnixSocketBindingSelection(
			final Control[] unixSocketControls) {
		return new IChangeListener() {
			@Override
			public void handleChange(final ChangeEvent event) {
				setWidgetsEnabled(
						model.isCustomSettings()
								&& model.isUnixSocketBindingMode(),
						unixSocketControls);
			}
		};
	}

	private IChangeListener onTcpConnectionBindingSelection(
			final Control[] tcpConnectionControls,
			final Control[] tcpAuthControls) {
		return new IChangeListener() {
			@Override
			public void handleChange(final ChangeEvent event) {
				setWidgetsEnabled(model.isCustomSettings()
						&& model.isTcpConnectionBindingMode()
						&& model.isTcpTLSVerify(), tcpAuthControls);
				// and give focus to the first given control (if applicable)
				setWidgetsEnabled(
						model.isCustomSettings()
								&& model.isTcpConnectionBindingMode(),
						tcpConnectionControls);
			}
		};
	}

	private IValueChangeListener onTcpAuthSelection(
			final Control[] tcpAuthControls) {
		return new IValueChangeListener() {

			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				setWidgetsEnabled(model.isCustomSettings()
						&& model.isTcpConnectionBindingMode()
						&& model.isTcpTLSVerify(), tcpAuthControls);
			}
		};
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
				final ArrayBlockingQueue<Boolean> resultQueue = new ArrayBlockingQueue<>(
						1);
				try {
					getWizard().getContainer().run(true, false,
							new IRunnableWithProgress() {
						@Override
						public void run(final IProgressMonitor monitor) {
							monitor.beginTask(
									WizardMessages.getString(
											"NewDockerConnectionPage.pingTask"), //$NON-NLS-1$
									IProgressMonitor.UNKNOWN);
							try {
								final DockerConnection dockerConnection = getDockerConnection();
								dockerConnection.open(false);
								dockerConnection.ping();
								dockerConnection.close();
								resultQueue.add(true);
							} catch (DockerException e) {
								Activator.log(e);
								resultQueue.add(false);
							}
						}
					});
				} catch (InvocationTargetException | InterruptedException o_O) {
					Activator.log(o_O);
				}
				try {
					final Boolean result = resultQueue.poll(5000,
							TimeUnit.MILLISECONDS);
					if (result != null && result) {
						new MessageDialog(Display.getDefault().getActiveShell(),
								WizardMessages.getString(
										"NewDockerConnectionPage.success"), //$NON-NLS-1$
								null,
								WizardMessages.getString(
										"NewDockerConnectionPage.pingSuccess"), //$NON-NLS-1$
								SWT.ICON_INFORMATION,
								new String[] { WizardMessages.getString(
										"NewDockerConnectionPage.ok") }, //$NON-NLS-1$
								0).open();

					} else {
						new MessageDialog(Display.getDefault().getActiveShell(),
								WizardMessages.getString(
										"NewDockerConnectionPage.failure"), //$NON-NLS-1$
								null,
								WizardMessages.getString(
										"NewDockerConnectionPage.pingFailure"), //$NON-NLS-1$
								SWT.ICON_ERROR,
								new String[] { WizardMessages.getString(
										"NewDockerConnectionPage.ok") }, //$NON-NLS-1$
								0).open();
					}
				} catch (InterruptedException o_O) {
					new MessageDialog(Display.getDefault().getActiveShell(),
							WizardMessages.getString(
									"NewDockerConnectionPage.failure"), //$NON-NLS-1$
							null,
							WizardMessages.getString(
									"NewDockerConnectionPage.pingFailure"), //$NON-NLS-1$
							SWT.ICON_ERROR,
							new String[] { WizardMessages
									.getString("NewDockerConnectionPage.ok") }, //$NON-NLS-1$
							0).open();
				}
			}

		};
	}

	/**
	 * Opens a new {@link DockerConnection} using the settings of this
	 * {@link NewDockerConnectionPage}.
	 * 
	 * @return
	 * @throws DockerCertificateException
	 */
	protected DockerConnection getDockerConnection() {
		if (model.getBindingMode() == UNIX_SOCKET) {
			return new DockerConnection.Builder()
					.name(model.getConnectionName())
					.unixSocket(model.getUnixSocketPath()).build();
		} else {
			final Builder tcpConnectionBuilder = new DockerConnection.Builder()
					.name(model.getConnectionName())
					.tcpHost(model.getTcpHost());
			if (model.isTcpTLSVerify()) {
				tcpConnectionBuilder.tcpCertPath(model.getTcpCertPath());
			}
			return tcpConnectionBuilder.build();
		}
	}

	private static class ConnectionNameValidator extends MultiValidator {

		private final IObservableValue connectionNameModelObservable;

		public ConnectionNameValidator(
				final IObservableValue connectionNameModelObservable) {
			this.connectionNameModelObservable = connectionNameModelObservable;
		}

		@Override
		public IObservableList getTargets() {
			WritableList targets = new WritableList();
			targets.add(connectionNameModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final String connectionName = (String) this.connectionNameModelObservable
					.getValue();
			if (connectionName == null || connectionName.isEmpty()) {
				return ValidationStatus.cancel(WizardMessages.getString(
						"NewDockerConnectionPage.validation.missingConnectionName.msg")); //$NON-NLS-1$
			} else if (DockerConnectionManager.getInstance()
					.findConnection(connectionName) != null) {
				return ValidationStatus.error(WizardMessages.getString(
						"NewDockerConnectionPage.validation.duplicateConnectionName.msg")); //$NON-NLS-1$
			}
			return ValidationStatus.ok();
		}
	}

	private static class UnixSocketValidator extends MultiValidator {

		private final IObservableValue unixSocketBindingModeModelObservable;
		private final IObservableValue unixSocketPathModelObservable;

		public UnixSocketValidator(
				final IObservableValue unixSocketBindingModeModelObservable,
				final IObservableValue unixSocketPathModelObservable) {
			this.unixSocketBindingModeModelObservable = unixSocketBindingModeModelObservable;
			this.unixSocketPathModelObservable = unixSocketPathModelObservable;
		}

		@Override
		public IObservableList getTargets() {
			WritableList targets = new WritableList();
			targets.add(unixSocketPathModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final Boolean unixSocketBindingMode = (Boolean) this.unixSocketBindingModeModelObservable
					.getValue();
			final String unixSocketPath = (String) this.unixSocketPathModelObservable
					.getValue();
			if (unixSocketBindingMode) {
				if (unixSocketPath == null || unixSocketPath.isEmpty()) {
					return ValidationStatus.error(WizardMessages.getString(
							"NewDockerConnectionPage.validation.missingUnixSocket.msg")); //$NON-NLS-1$
				}
				try {
					final URI unixSocketURI = new URI(unixSocketPath);
					if (unixSocketURI.getScheme() != null
							&& !unixSocketURI.getScheme().equals("unix")) {
						return ValidationStatus.error(WizardMessages.getString(
								"NewDockerConnectionPage.validation.invalidUnixSocketScheme.msg")); //$NON-NLS-1$
					}
					if (unixSocketURI.getPath() != null) {
						final File unixSocket = new File(
								unixSocketURI.getPath());
						if (!unixSocket.exists()) {
							return ValidationStatus
									.error(WizardMessages.getString(
											"NewDockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$
						} else if (!unixSocket.canRead()
								|| !unixSocket.canWrite()) {
							return ValidationStatus
									.error(WizardMessages.getString(
											"NewDockerConnectionPage.validation.unreadableUnixSocket.msg")); //$NON-NLS-1$
						}
					} else {
						return ValidationStatus.error(WizardMessages.getString(
								"NewDockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$

					}
				} catch (URISyntaxException e) {
					return ValidationStatus.error(WizardMessages.getString(
							"NewDockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$

				}
			}
			return ValidationStatus.ok();
		}

	}

	private static class TcpHostValidator extends MultiValidator {

		private final IObservableValue tcpConnectionBindingModeModelObservable;
		private final IObservableValue tcpHostModelObservable;

		public TcpHostValidator(
				final IObservableValue tcpConnectionBindingModeModelObservable,
				final IObservableValue tcpHostModelObservable) {
			this.tcpConnectionBindingModeModelObservable = tcpConnectionBindingModeModelObservable;
			this.tcpHostModelObservable = tcpHostModelObservable;
		}

		@Override
		public IObservableList getTargets() {
			WritableList targets = new WritableList();
			targets.add(tcpHostModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final Boolean tcpConnectionBindingMode = (Boolean) this.tcpConnectionBindingModeModelObservable
					.getValue();
			final String tcpHost = (String) this.tcpHostModelObservable
					.getValue();
			if (tcpConnectionBindingMode) {
				if (tcpHost == null || tcpHost.isEmpty()) {
					return ValidationStatus.error(WizardMessages.getString(
							"NewDockerConnectionPage.validation.missingTcpConnectionURI.msg")); //$NON-NLS-1$
				}
				try {
					final URI uri = new URI(tcpHost);
					final String scheme = uri.getScheme() != null
							? uri.getScheme().toLowerCase() : null;
					final String host = uri.getHost();
					final int port = uri.getPort();
					if (scheme != null
							&& !(scheme.equals("tcp") || scheme.equals("http")
									|| scheme.equals("https"))) {
						return ValidationStatus.error(WizardMessages.getString(
								"NewDockerConnectionPage.validation.invalidTcpConnectionScheme.msg")); //$NON-NLS-1$
					} else if (host == null) {
						return ValidationStatus.error(WizardMessages.getString(
								"NewDockerConnectionPage.validation.invalidTcpConnectionHost.msg")); //$NON-NLS-1$

					} else if (port == -1) {
						return ValidationStatus.error(WizardMessages.getString(
								"NewDockerConnectionPage.validation.invalidTcpConnectionPort.msg")); //$NON-NLS-1$

					}
				} catch (URISyntaxException e) {
					// URI is not valid
					return ValidationStatus.error(WizardMessages.getString(
							"NewDockerConnectionPage.validation.invalidTcpConnectionURI.msg")); //$NON-NLS-1$
				}
			}
			return ValidationStatus.ok();
		}

	}

	private static class TcpCertificatesValidator extends MultiValidator {

		private final IObservableValue tcpConnectionBindingModeModelObservable;
		private final IObservableValue tcpTlsVerifyModelObservable;
		private final IObservableValue tcpCertPathModelObservable;

		public TcpCertificatesValidator(
				final IObservableValue tcpConnectionBindingModeModelObservable,
				final IObservableValue tcpTlsVerifyModelObservable,
				final IObservableValue tcpCertPathModelObservable) {
			this.tcpConnectionBindingModeModelObservable = tcpConnectionBindingModeModelObservable;
			this.tcpTlsVerifyModelObservable = tcpTlsVerifyModelObservable;
			this.tcpCertPathModelObservable = tcpCertPathModelObservable;
		}

		@Override
		public IObservableList getTargets() {
			WritableList targets = new WritableList();
			targets.add(tcpCertPathModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final Boolean tcpConnectionBindingMode = (Boolean) this.tcpConnectionBindingModeModelObservable
					.getValue();
			final Boolean tcpTlsVerify = (Boolean) this.tcpTlsVerifyModelObservable
					.getValue();
			final String tcpCertPath = (String) this.tcpCertPathModelObservable
					.getValue();
			if (tcpConnectionBindingMode && tcpTlsVerify) {
				if (tcpCertPath == null || tcpCertPath.isEmpty()) {
					return ValidationStatus.error(WizardMessages.getString(
							"NewDockerConnectionPage.validation.missingTcpCertPath.msg")); //$NON-NLS-1$
				}
				final File tcpCert = new File(tcpCertPath);
				if (!tcpCert.exists()) {
					return ValidationStatus.error(WizardMessages.getString(
							"NewDockerConnectionPage.validation.invalidTcpCertPath.msg")); //$NON-NLS-1$
				} else if (!tcpCert.canRead() || !tcpCert.canRead()) {
					return ValidationStatus.error(WizardMessages.getString(
							"NewDockerConnectionPage.validation.unreadableTcpCertPath.msg")); //$NON-NLS-1$
				}
			}
			return ValidationStatus.ok();
		}

	}

}
