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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings.BindingType;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerMachine;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.linuxtools.internal.docker.core.UnixSocketConnectionSettings;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.spotify.docker.client.DockerCertificateException;

/**
 * {@link WizardPage} to input the settings to connect to a Docker
 * engine/daemon.
 *
 */
public class NewDockerConnectionPage extends WizardPage {

	private static final String DOCKER_MACHINE_PREFERENCE_PAGE_ID = "org.eclipse.linuxtools.docker.ui.preferences.DockerMachinePreferencePage"; //$NON-NLS-1$

	private final DataBindingContext dbc;
	
	private final NewDockerConnectionPageModel model;

	/**
	 * Constructor.
	 */
	public NewDockerConnectionPage() {
		super("DockerConnectionPage", //$NON-NLS-1$
				WizardMessages.getString("DockerConnectionPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("DockerConnectionPage.msg")); //$NON-NLS-1$
		this.model = new NewDockerConnectionPageModel();
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

		Clipboard clip = new Clipboard(Display.getCurrent());
		String content = (String) clip.getContents(TextTransfer.getInstance(), DND.SELECTION_CLIPBOARD);
		// DOCKER_HOST is the minimal property needed
		if (content != null && content.contains(DefaultDockerConnectionSettingsFinder.DOCKER_HOST)) {
			retrieveConnectionSettings(content);
		} else {
			content = (String) clip.getContents(TextTransfer.getInstance(), DND.CLIPBOARD);
			if (content != null && content.contains(DefaultDockerConnectionSettingsFinder.DOCKER_HOST)) {
				retrieveConnectionSettings(content);
			} else {
				retrieveDefaultConnectionSettings();
			}
		}

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
		connectionNameText.setToolTipText(WizardMessages
				.getString("DockerConnectionPage.nameTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(connectionNameText);

		// the 'Search' button
		final Button searchButton = new Button(container, SWT.NONE);
		searchButton.setText(WizardMessages
				.getString("DockerConnectionPage.searchButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.align(SWT.BEGINNING, SWT.CENTER).grab(false, false)
				.applyTo(searchButton);
		searchButton.addSelectionListener(onSearchButtonSelection());


		// custom settings checkbox
		final Button customConnectionSettingsButton = new Button(container,
				SWT.CHECK);
		customConnectionSettingsButton.setText(WizardMessages
				.getString("DockerConnectionPage.customLabel")); //$NON-NLS-1$
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
				WizardMessages.getString("DockerConnectionPage.unixSocket")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).applyTo(unixSocketBindingModeButton);

		final Label unixSocketPathLabel = new Label(customSettingsGroup,
				SWT.NONE);
		unixSocketPathLabel.setText(
				WizardMessages.getString("DockerConnectionPage.location")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).applyTo(unixSocketPathLabel);
		final Text unixSocketPathText = new Text(customSettingsGroup,
				SWT.BORDER);
		unixSocketPathText.setToolTipText(WizardMessages
				.getString("DockerConnectionPage.unixPathTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(unixSocketPathText);

		final Button unixSocketPathBrowseButton = new Button(
				customSettingsGroup, SWT.BUTTON1);
		unixSocketPathBrowseButton.setText(WizardMessages
				.getString("DockerConnectionPage.browseButton")); //$NON-NLS-1$
		unixSocketPathBrowseButton
				.addSelectionListener(onBrowseUnixSocketPath());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(unixSocketPathBrowseButton);

		// TCP connection
		final Button tcpConnectionBindingModeButton = new Button(
				customSettingsGroup, SWT.RADIO);
		tcpConnectionBindingModeButton.setText(WizardMessages
				.getString("DockerConnectionPage.tcpConnection")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).applyTo(tcpConnectionBindingModeButton);

		final Label tcpHostLabel = new Label(customSettingsGroup, SWT.NONE);
		tcpHostLabel.setText(
				WizardMessages.getString("DockerConnectionPage.hostLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).applyTo(tcpHostLabel);

		final Text tcpHostText = new Text(customSettingsGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false).applyTo(tcpHostText);

		final Button tcpAuthButton = new Button(customSettingsGroup, SWT.CHECK);
		tcpAuthButton.setText(WizardMessages
				.getString("DockerConnectionPage.tcpAuthButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).span(3, 1).applyTo(tcpAuthButton);

		final Label tcpCertPathLabel = new Label(customSettingsGroup, SWT.NONE);
		tcpCertPathLabel.setText(WizardMessages
				.getString("DockerConnectionPage.tcpPathLabel")); //$NON-NLS-1$
		tcpCertPathLabel.setToolTipText(WizardMessages
				.getString("DockerConnectionPage.tcpPathTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT * 2, 0).applyTo(tcpCertPathLabel);
		final Text tcpCertPathText = new Text(customSettingsGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(tcpCertPathText);
		final Button tcpCertPathBrowseButton = new Button(customSettingsGroup,
				SWT.BUTTON1);
		tcpCertPathBrowseButton.setText(WizardMessages
				.getString("DockerConnectionPage.browseButton")); //$NON-NLS-1$
		tcpCertPathBrowseButton.addSelectionListener(onBrowseTcpCertPath());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(tcpCertPathBrowseButton);

		// the 'test connection' button
		final Button testConnectionButton = new Button(container, SWT.NONE);
		testConnectionButton.setText(WizardMessages
				.getString("DockerConnectionPage.testConnection")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3, 1)
				.align(SWT.END, SWT.CENTER).applyTo(testConnectionButton);
		testConnectionButton
				.addSelectionListener(onTestConnectionButtonSelection());

		// observe
		final IObservableValue<String> connectionNameModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.CONNECTION_NAME)
				.observe(model);
		final IObservableValue<Boolean> unixSocketBindingModeModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.UNIX_SOCKET_BINDING_MODE)
				.observe(model);
		final IObservableValue<String> unixSocketPathModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.UNIX_SOCKET_PATH)
				.observe(model);

		final IObservableValue customConnectionSettingsModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.CUSTOM_SETTINGS)
				.observe(model);
		final IObservableValue<Boolean> tcpConnectionBindingModeModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_CONNECTION_BINDING_MODE)
				.observe(model);
		final IObservableValue<String> tcpCertPathModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_CERT_PATH)
				.observe(model);
		final IObservableValue<Boolean> tcpTlsVerifyModelObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_TLS_VERIFY)
				.observe(model);
		final IObservableValue<String> tcpHostModelObservable = BeanProperties
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
				new UnixSocketValidator(connectionNameModelObservable,
						unixSocketBindingModeModelObservable,
						unixSocketPathModelObservable));
		dbc.addValidationStatusProvider(
				new TcpHostValidator(connectionNameModelObservable,
						tcpConnectionBindingModeModelObservable,
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
				directoryDialog.setFilterPath(model.getTcpCertPath());
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
					monitor -> {
						monitor.beginTask(
								WizardMessages.getString(
										"DockerConnectionPage.retrieveTask"), //$NON-NLS-1$
								1);
						final DockerConnectionManager dockerConnectionManager = DockerConnectionManager
								.getInstance();
						final IDockerConnectionSettings defaultConnectionSettings = dockerConnectionManager
								.findDefaultConnectionSettings();
						if (defaultConnectionSettings != null) {

							model.setCustomSettings(!defaultConnectionSettings
									.isSettingsResolved());
							model.setConnectionName(dockerConnectionManager
									.resolveConnectionName(
											defaultConnectionSettings));
							switch (defaultConnectionSettings.getType()) {
							case TCP_CONNECTION:
								final TCPConnectionSettings tcpConnectionSettings = (TCPConnectionSettings) defaultConnectionSettings;
								model.setTcpConnectionBindingMode(true);
								model.setTcpCertPath(tcpConnectionSettings
										.getPathToCertificates());
								model.setTcpTLSVerify(
										tcpConnectionSettings.isTlsVerify());
								model.setTcpHost(
										tcpConnectionSettings.getHost());
								break;
							case UNIX_SOCKET_CONNECTION:
								model.setUnixSocketBindingMode(true);
								final UnixSocketConnectionSettings unixSocketConnectionSettings = (UnixSocketConnectionSettings) defaultConnectionSettings;
								model.setUnixSocketPath(
										unixSocketConnectionSettings.getPath());
								break;
							}
						} else {
							// fall-back to custom settings, suggesting a
							// Unix Socket connection to the user.
							model.setCustomSettings(true);
							model.setUnixSocketBindingMode(true);
						}

						monitor.done();
					});
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}

	}

	private void retrieveConnectionSettings(String content) {
		final String EQUAL = "="; //$NON-NLS-1$
		StringTokenizer tok = new StringTokenizer(content);
		while (tok.hasMoreTokens()) {
			String line = tok.nextToken();
			String[] tokens;
			if (line.startsWith(DefaultDockerConnectionSettingsFinder.DOCKER_HOST)) {
				tokens = line.split(EQUAL);
				if (tokens.length == 2) {
					String host = tokens[1];
					if (host.startsWith("unix")) { //$NON-NLS-1$
						model.setUnixSocketBindingMode(true);
						model.setUnixSocketPath(host);
					} else {
						model.setTcpConnectionBindingMode(true);
						model.setTcpHost(host);
					}
					model.setCustomSettings(true);
				}
			} else if (line.startsWith(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH)) {
				tokens = line.split(EQUAL);
				if (tokens.length == 2) {
					model.setTcpCertPath(tokens[1]);
				}
			} else if (line.startsWith(DefaultDockerConnectionSettingsFinder.DOCKER_TLS_VERIFY)) {
				tokens = line.split(EQUAL);
				if (tokens.length == 2) {
						model.setTcpTLSVerify(
								DefaultDockerConnectionSettingsFinder.DOCKER_TLS_VERIFY_TRUE
										.equals(tokens[1]));
				}
			}
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

		return event -> updateWidgetsState(bindingModeSelectionControls,
				unixSocketControls, tcpConnectionControls, tcpAuthControls);
	}

	private IChangeListener onUnixSocketBindingSelection(
			final Control[] unixSocketControls) {
		return event -> setWidgetsEnabled(
				model.isCustomSettings() && model.isUnixSocketBindingMode(),
				unixSocketControls);
	}

	private IChangeListener onTcpConnectionBindingSelection(
			final Control[] tcpConnectionControls,
			final Control[] tcpAuthControls) {
		return event -> {
			setWidgetsEnabled(model.isCustomSettings()
					&& model.isTcpConnectionBindingMode()
					&& model.isTcpTLSVerify(), tcpAuthControls);
			// and give focus to the first given control (if applicable)
			setWidgetsEnabled(
					model.isCustomSettings()
							&& model.isTcpConnectionBindingMode(),
					tcpConnectionControls);
		};
	}

	private IValueChangeListener onTcpAuthSelection(
			final Control[] tcpAuthControls) {
		return event -> setWidgetsEnabled(model.isCustomSettings()
				&& model.isTcpConnectionBindingMode() && model.isTcpTLSVerify(),
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
					getWizard().getContainer().run(true, false,
							monitor -> {
								monitor.beginTask(WizardMessages.getString(
										"DockerConnectionPage.pingTask"), //$NON-NLS-1$
										IProgressMonitor.UNKNOWN);
								try {
									final DockerConnection dockerConnection = getDockerConnection();
									dockerConnection.open(false);
									dockerConnection.ping();
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
						WizardMessages.getString(
								"DockerConnectionPage.pingSuccess"), //$NON-NLS-1$
						SWT.ICON_INFORMATION,
						new String[] { WizardMessages
								.getString("DockerConnectionPage.ok") } //$NON-NLS-1$
						);
			}

			private void displayErrorDialog() {
				displayDialog(
						WizardMessages
								.getString("DockerConnectionPage.failure"), //$NON-NLS-1$
						WizardMessages.getString(
								"DockerConnectionPage.pingFailure"), //$NON-NLS-1$
						SWT.ICON_ERROR,
						new String[] { WizardMessages
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

	private SelectionListener onSearchButtonSelection() {
		return new SelectionAdapter() {
			
			private String getVMDriverInstallDir() {
				return Activator.getDefault()
						.getPreferenceStore().getString(
								PreferenceConstants.VM_DRIVER_INSTALLATION_DIRECTORY);
			}

			private String getDockerMachineInstallDir() {
				return Activator.getDefault()
						.getPreferenceStore()
						.getString(PreferenceConstants.DOCKER_MACHINE_INSTALLATION_DIRECTORY);
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!DockerMachine
						.checkPathToDockerMachine(getDockerMachineInstallDir())
						|| getVMDriverInstallDir().equals("")) { //$NON-NLS-1$
					final boolean confirm = MessageDialog.openConfirm(
							getShell(),
									WizardMessages.getString(
											"DockerMachineSupport.title"), //$NON-NLS-1$
							WizardMessages
									.getString("DockerMachineSupport.message")); //$NON-NLS-1$
					if (confirm) {
						PreferencesUtil.createPreferenceDialogOn(getShell(),
								DOCKER_MACHINE_PREFERENCE_PAGE_ID,
								new String[] {
										DOCKER_MACHINE_PREFERENCE_PAGE_ID },
								null).open();
						// after user input data, check again
						if (getDockerMachineInstallDir().equals("") //$NON-NLS-1$
								|| getVMDriverInstallDir().equals("")) { //$NON-NLS-1$
							return;
						}
					} else {
						return;
					}
				}
				final String dockerMachineInstallDir = getDockerMachineInstallDir();
				final String vmDriverInstallDir = getVMDriverInstallDir();
				final Map<String, Boolean> allMachineStates = retrieveDockerMachineNames(
						dockerMachineInstallDir,
							vmDriverInstallDir);
				final List<String> activeMachineNames = allMachineStates
						.entrySet().stream()
						.filter((machineEntry) -> machineEntry.getValue()
								.booleanValue())
						.map((machineEntry) -> machineEntry.getKey())
						.collect(Collectors.toList());
				if (activeMachineNames.size() > 0) {
					ListDialog connPrompt = new ListDialog(getShell());
					connPrompt.setContentProvider(new ConnectionSelectionContentProvider());
					connPrompt.setLabelProvider(new ConnectionSelectionLabelProvider());
					connPrompt.setTitle(WizardMessages.getString(
							"DockerConnectionPage.searchDialog.title")); //$NON-NLS-1$
					connPrompt.setMessage(WizardMessages.getString(
							"DockerConnectionPage.searchDialog.message")); //$NON-NLS-1$
					connPrompt.setInput(
							activeMachineNames.toArray(new String[0]));
					if (connPrompt.open() == 0 && connPrompt.getResult().length > 0) {
						final String name = ((String) connPrompt
								.getResult()[0]);
						model.setBindingMode(
								EnumDockerConnectionSettings.TCP_CONNECTION);
						model.setConnectionName(name);
						model.setUnixSocketPath(null);
						try {
							final String host = DockerMachine.getHost(name,
									dockerMachineInstallDir,
									vmDriverInstallDir);
							model.setTcpHost(host);
						} catch (DockerException e1) {
							Activator.log(e1);
						}
						try {
							final String certPath = DockerMachine.getCertPath(
									name, dockerMachineInstallDir,
									vmDriverInstallDir);
							if (certPath != null) {
								model.setTcpTLSVerify(true);
								model.setTcpCertPath(certPath);
							} else {
								model.setTcpTLSVerify(false);
								model.setTcpCertPath(null);
							}
						} catch (DockerException e1) {
							Activator.log(e1);
						}
					}
				} else {
					if (allMachineStates.size() == 1) {
						MessageDialog.openInformation(getShell(),
								WizardMessages.getString(
										"DockerConnectionPage.searchDialog.discovery.title"), //$NON-NLS-1$
								WizardMessages.getFormattedString(
										"DockerConnectionPage.searchDialog.discovery.innactive.single", //$NON-NLS-1$
										allMachineStates.entrySet().iterator()
												.next().getKey()));
					} else if (allMachineStates.size() > 1) {
						final String allMachineNames = allMachineStates
								.entrySet().stream()
								.map((machineEntry) -> machineEntry.getKey())
								.collect(Collectors.joining(", "));
						MessageDialog.openInformation(getShell(),
								WizardMessages.getString(
										"DockerConnectionPage.searchDialog.discovery.title"), //$NON-NLS-1$
								WizardMessages.getFormattedString(
										"DockerConnectionPage.searchDialog.discovery.innactive.multiple", //$NON-NLS-1$
										allMachineNames));
					} else {
						MessageDialog.openInformation(getShell(),
								WizardMessages.getString(
										"DockerConnectionPage.searchDialog.discovery.title"), //$NON-NLS-1$
								WizardMessages.getString(
										"DockerConnectionPage.searchDialog.discovery.empty")); //$NON-NLS-1$
					}
				}
			}

			/**
			 * Retrieves the docker machine names along with a boolean flag to
			 * indicate if it is running or not.
			 * 
			 * @param dockerMachineInstallDir
			 * @param vmDriverInstallDir
			 * @return
			 */
			private Map<String, Boolean> retrieveDockerMachineNames(
					final String dockerMachineInstallDir,
					final String vmDriverInstallDir) {
				try {
					final String[] dmNames = DockerMachine
							.getNames(dockerMachineInstallDir);
					return java.util.stream.Stream.of(dmNames)
							.collect(Collectors.toMap(name -> name, name -> {
								try {
									return DockerMachine.getHost(name,
											dockerMachineInstallDir,
											vmDriverInstallDir) != null;
								} catch (DockerException e) {
									Activator.log(e);
									return false;
								}
							}));
				} catch (DockerException e) {
					Activator.log(e);
					return Collections.emptyMap();
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
					.unixSocketConnection(new UnixSocketConnectionSettings(
							model.getUnixSocketPath()));
		} else {
			return new DockerConnection.Builder()
					.name(model.getConnectionName())
					.tcpConnection(new TCPConnectionSettings(model.getTcpHost(),
							model.getTcpCertPath()));
		}
	}

	private static abstract class ConnectionNameValidator
			extends MultiValidator {

		private final IObservableValue<String> connectionNameModelObservable;

		ConnectionNameValidator(
				final IObservableValue<String> connectionNameModelObservable) {
			this.connectionNameModelObservable = connectionNameModelObservable;
		}

		public IObservableValue<String> getConnectionNameModelObservable() {
			return this.connectionNameModelObservable;
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
			} else if (DockerConnectionManager.getInstance()
					.findConnection(connectionName) != null) {
				return ValidationStatus.error(WizardMessages.getString(
						"DockerConnectionPage.validation.duplicateConnectionName.msg")); //$NON-NLS-1$
			}
			return ValidationStatus.ok();
		}
	}

	private static class UnixSocketValidator extends ConnectionNameValidator {

		private final IObservableValue<Boolean> unixSocketBindingModeModelObservable;
		private final IObservableValue<String> unixSocketPathModelObservable;

		public UnixSocketValidator(
				final IObservableValue<String> connectionNameModelObservable,
				final IObservableValue<Boolean> unixSocketBindingModeModelObservable,
				final IObservableValue<String> unixSocketPathModelObservable) {
			super(connectionNameModelObservable);
			this.unixSocketBindingModeModelObservable = unixSocketBindingModeModelObservable;
			this.unixSocketPathModelObservable = unixSocketPathModelObservable;
		}

		@Override
		public IObservableList<IObservableValue<String>> getTargets() {
			WritableList<IObservableValue<String>> targets = new WritableList<>();
			targets.add(getConnectionNameModelObservable());
			targets.add(unixSocketPathModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final IStatus connectionNameStatus = super.validate();
			final Boolean unixSocketBindingMode = this.unixSocketBindingModeModelObservable
					.getValue();
			final String unixSocketPath = this.unixSocketPathModelObservable
					.getValue();
			if (!connectionNameStatus.isOK()) {
				return connectionNameStatus;
			}
			if (unixSocketBindingMode) {
				if (unixSocketPath == null || unixSocketPath.isEmpty()) {
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.missingUnixSocket.msg")); //$NON-NLS-1$
				}
				try {
					// validate the Unix socket format
					final URI unixSocketURI = new URI(unixSocketPath);
					if (unixSocketURI.getScheme() != null
							&& !unixSocketURI.getScheme().equals("unix")) { //$NON-NLS-1$
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.invalidUnixSocketScheme.msg")); //$NON-NLS-1$
					}
					if (unixSocketURI.getPath() == null) {
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$
					}
					final File unixSocket = new File(unixSocketURI.getPath());
					if (!unixSocket.exists()) {
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$
					} else if (!unixSocket.canRead()
							|| !unixSocket.canWrite()) {
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.unreadableUnixSocket.msg")); //$NON-NLS-1$
					}
					// check for other, existing connections with the same
					// settings
					final boolean duplicateConnection = DockerConnectionManager
							.getInstance().getAllConnections().stream()
							.filter(c -> c.getSettings()
									.getType() == BindingType.UNIX_SOCKET_CONNECTION)
							.map(c -> (UnixSocketConnectionSettings) c
									.getSettings())
							.anyMatch(s -> s.getPath().equals(unixSocketPath));
					if (duplicateConnection) {
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.duplicateUnixSocketPath.msg")); //$NON-NLS-1$
					}
				} catch (URISyntaxException e) {
					return ValidationStatus.error(WizardMessages.getString(
							"DockerConnectionPage.validation.invalidUnixSocketPath.msg")); //$NON-NLS-1$

				}
			}
			return ValidationStatus.ok();
		}

	}

	private static class TcpHostValidator extends ConnectionNameValidator {

		private final IObservableValue<Boolean> tcpConnectionBindingModeModelObservable;
		private final IObservableValue<String> tcpHostModelObservable;

		public TcpHostValidator(
				final IObservableValue<String> connectionNameModelObservable,
				final IObservableValue<Boolean> tcpConnectionBindingModeModelObservable,
				final IObservableValue<String> tcpHostModelObservable) {
			super(connectionNameModelObservable);
			this.tcpConnectionBindingModeModelObservable = tcpConnectionBindingModeModelObservable;
			this.tcpHostModelObservable = tcpHostModelObservable;
		}

		@Override
		public IObservableList<IObservableValue<String>> getTargets() {
			WritableList<IObservableValue<String>> targets = new WritableList<>();
			targets.add(getConnectionNameModelObservable());
			targets.add(tcpHostModelObservable);
			return targets;
		}

		@Override
		protected IStatus validate() {
			final Boolean tcpConnectionBindingMode = this.tcpConnectionBindingModeModelObservable
					.getValue();
			final String tcpHost = this.tcpHostModelObservable.getValue();
			final IStatus connectionNameStatus = super.validate();
			if (!connectionNameStatus.isOK()) {
				return connectionNameStatus;
			}
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
					// check for other, existing connections with the same
					// settings
					final boolean duplicateConnection = DockerConnectionManager
							.getInstance().getAllConnections().stream()
							.filter(c -> c.getSettings()
									.getType() == BindingType.TCP_CONNECTION)
							.map(c -> (TCPConnectionSettings) c.getSettings())
							.anyMatch(s -> s.getHost().equals(tcpHost));
					if (duplicateConnection) {
						return ValidationStatus.error(WizardMessages.getString(
								"DockerConnectionPage.validation.duplicateUnixSocketPath.msg")); //$NON-NLS-1$
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

	private class ConnectionSelectionContentProvider
			implements IStructuredContentProvider {
		@Override
		public void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return (String[]) (inputElement);
		}
	}

	private class ConnectionSelectionLabelProvider implements ILabelProvider {

		private Image CONNECTION_IMAGE = SWTImagesFactory.DESC_REPOSITORY_MIDDLE
				.createImage();

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void dispose() {
			CONNECTION_IMAGE.dispose();
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public String getText(Object element) {
			return element.toString();
		}

		@Override
		public Image getImage(Object element) {
			return CONNECTION_IMAGE;
		}
	}

}
