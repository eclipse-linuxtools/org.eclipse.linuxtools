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

import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_CONNECTION;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
 * @author xcoulon
 *
 */
public class NewDockerConnectionPage extends WizardPage {

	private EnumDockerConnectionSettings bindingMode;
	private String connectionName = null;
	private String unixSocketPath = null;
	private String tcpHost = null;
	private boolean tcpTLSVerify = false;
	private String tcpCertPath = null;
	private Control[] bindingModeSelectionControls;
	private Control[] unixSocketControls;
	private Control[] tcpConnectionControls;
	private Control[] tcpAuthControls;
	private Text connectionNameText;
	private Button customConnectionSettingsButton;
	private Button unixSocketSelectionButton;
	private Text unixSocketPathText;
	private Button tcpConnectionSelectionButton;
	private Text tcpHostText;
	private Button tcpAuthButton;
	private Text tcpCertPathText;

	public NewDockerConnectionPage() {
		super("NewDockerConnectionPage", //$NON-NLS-1$
				WizardMessages.getString("NewDockerConnectionPage.title"),
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("NewDockerConnectionPage.msg")); //$NON-NLS-1$
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(container);
		createConnectionSettingsContainer(container);
		setControl(container);
		retrieveDefaultConnectionSettings();
		setDefaultControlValues();
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
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6).spacing(10, 2).applyTo(container);

		// Connection name
		final Label connectionNameLabel = new Label(container, SWT.NONE);
		connectionNameLabel.setText(
				WizardMessages.getString("NewDockerConnectionPage.nameLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(connectionNameLabel);
		connectionNameText = new Text(container, SWT.BORDER);
		connectionNameText.setToolTipText(WizardMessages
				.getString("NewDockerConnectionPage.nameTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1)
				.applyTo(connectionNameText);

		customConnectionSettingsButton = new Button(container, SWT.CHECK);
		customConnectionSettingsButton.setText("Use custom connection settings:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).indent(0, 10).span(COLUMNS, 1)
				.applyTo(customConnectionSettingsButton);
		customConnectionSettingsButton.setSelection(false);

		final Group customSettingsGroup = new Group(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(COLUMNS, 1).grab(true, false)
				.applyTo(customSettingsGroup);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6).spacing(10, 2).applyTo(customSettingsGroup);

		unixSocketSelectionButton = new Button(customSettingsGroup, SWT.RADIO);
		unixSocketSelectionButton.setText(
				WizardMessages.getString("NewDockerConnectionPage.unixSocket")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(COLUMNS, 1).applyTo(unixSocketSelectionButton);
		final Label unixSocketPathLabel = new Label(customSettingsGroup, SWT.NONE);
		unixSocketPathLabel.setText(
				WizardMessages.getString("NewDockerConnectionPage.location")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).applyTo(unixSocketPathLabel);
		unixSocketPathText = new Text(customSettingsGroup, SWT.BORDER);
		unixSocketPathText.setToolTipText(WizardMessages
				.getString("NewDockerConnectionPage.unixPathTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(unixSocketPathText);
		final Button unixSocketPathBrowseButton = new Button(customSettingsGroup, SWT.BUTTON1);
		unixSocketPathBrowseButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.browseButton")); //$NON-NLS-1$
		unixSocketPathBrowseButton
				.addSelectionListener(onBrowseUnixSocketPath());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(unixSocketPathBrowseButton);

		tcpConnectionSelectionButton = new Button(customSettingsGroup, SWT.RADIO);
		tcpConnectionSelectionButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.tcpConnection")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(COLUMNS, 1)
				.applyTo(tcpConnectionSelectionButton);
		final Label tcpHostLabel = new Label(customSettingsGroup, SWT.NONE);
		tcpHostLabel.setText(
				WizardMessages.getString("NewDockerConnectionPage.hostLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).applyTo(tcpHostLabel);
		tcpHostText = new Text(customSettingsGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false).applyTo(tcpHostText);
		tcpAuthButton = new Button(customSettingsGroup, SWT.CHECK);
		tcpAuthButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.tcpAuthButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).span(3, 1).applyTo(tcpAuthButton);
		final Label tcpCertPathLabel = new Label(customSettingsGroup, SWT.NONE);
		tcpCertPathLabel.setText(
				WizardMessages.getString("NewDockerConnectionPage.tcpPathLabel")); //$NON-NLS-1$
		tcpCertPathLabel.setToolTipText(WizardMessages
				.getString("NewDockerConnectionPage.tcpPathTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT * 2, 0).applyTo(tcpCertPathLabel);
		tcpCertPathText = new Text(customSettingsGroup, SWT.BORDER);
		tcpCertPathText.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(tcpCertPathText);
		final Button tcpCertPathBrowseButton = new Button(customSettingsGroup, SWT.BUTTON1);
		tcpCertPathBrowseButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.browseButton")); //$NON-NLS-1$
		tcpCertPathBrowseButton.addSelectionListener(onBrowseTcpCertPathFile());
		tcpCertPathText.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(tcpCertPathBrowseButton);

		// the 'test connection' button
		final Button testConnectionButton = new Button(container, SWT.NONE);
		testConnectionButton.setText(WizardMessages
				.getString("NewDockerConnectionPage.testConnection")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(COLUMNS, 1).align(SWT.END, SWT.CENTER).applyTo(testConnectionButton);
		testConnectionButton.addSelectionListener(onTestConnectionButtonSelection());
		
		// group controls to easily enable/disable them
		bindingModeSelectionControls = new Control[] { unixSocketSelectionButton, tcpConnectionSelectionButton };
		unixSocketControls = new Control[] { unixSocketPathText, unixSocketPathLabel, unixSocketPathBrowseButton };
		tcpConnectionControls = new Control[] { tcpHostText, tcpHostLabel, tcpAuthButton };
		tcpAuthControls = new Control[] { tcpCertPathText, tcpCertPathLabel, tcpCertPathBrowseButton };

		// now use the control groups to bind events
		customConnectionSettingsButton.addSelectionListener(onCustomConnectionSettingsSelection());
		connectionNameText.addModifyListener(onConnectionNameModification());
		unixSocketSelectionButton.addSelectionListener(onUnixSocketSelection());
		unixSocketPathText.addModifyListener(onUnixSocketModification());
		tcpConnectionSelectionButton.addSelectionListener(onTcpConnectionSelection());
		tcpAuthButton.addSelectionListener(onTcpAuthSelection());
		tcpHostText.addModifyListener(onTcpHostModification());
		tcpCertPathText.addModifyListener(onTcpCertPathModification());

		// by default, custom settings are disabled:
		setWidgetsEnabled(false, bindingModeSelectionControls, unixSocketControls, tcpConnectionControls,
				tcpAuthControls);
	}
	
	private SelectionListener onBrowseUnixSocketPath() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final FileDialog fileDialog = new FileDialog(getShell());
				final String selectedPath = fileDialog.open();
				if (selectedPath != null) {
					unixSocketPathText.setText(selectedPath);
				}

			}
		};
	}

	private SelectionListener onBrowseTcpCertPathFile() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final DirectoryDialog directoryDialog = new DirectoryDialog(
						getShell());
				final String selectedPath = directoryDialog.open();
				if (selectedPath != null) {
					tcpCertPathText.setText(selectedPath);
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
		// let's run this in a job and show the progress in the wizard progressbar
		try {
			getWizard().getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
						public void run(final IProgressMonitor monitor) {
							monitor.beginTask(
									WizardMessages.getString(
											"NewDockerConnectionPage.retrieveTask"), //$NON-NLS-1$
									1);
					try {
						final DockerConnection.Defaults defaults = new DockerConnection.Defaults();
						NewDockerConnectionPage.this.bindingMode = defaults.getBindingMode();
						NewDockerConnectionPage.this.connectionName = defaults.getName();
						NewDockerConnectionPage.this.unixSocketPath = defaults.getUnixSocketPath();
						NewDockerConnectionPage.this.tcpHost = defaults.getTcpHost();
						NewDockerConnectionPage.this.tcpTLSVerify = defaults.getTcpTlsVerify();
						NewDockerConnectionPage.this.tcpCertPath = defaults.getTcpCertPath();
						NewDockerConnectionPage.this.connectionName = defaults.getName();
					} catch (DockerException e) {
						Activator.log(e);
					}
					
					monitor.done();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}
		
		
	}

	private void setDefaultControlValues() {
		// now that all widgets and their selectionAdapters are instantiated,
		// let's set the values
		if(this.connectionName != null) {
			this.connectionNameText.setText(this.connectionName);
		}
		if (this.bindingMode == UNIX_SOCKET) {
			unixSocketSelectionButton.setSelection(true);
			if (this.unixSocketPath != null) {
				unixSocketPathText.setText(this.unixSocketPath);
			}
		} else {
			tcpConnectionSelectionButton.setSelection(true);
			if (this.tcpHost != null) {
				tcpHostText.setText(this.tcpHost);
			}
			if (this.tcpTLSVerify) {
				setWidgetsEnabled(true, tcpAuthControls);
				tcpAuthButton.setSelection(true);
				tcpCertPathText.setEnabled(true);
				if (this.tcpCertPath != null) {
					tcpCertPathText.setText(this.tcpCertPath);
				}
			}
			// disable other widgets
			unixSocketSelectionButton.setSelection(false);
		}
		// disable widgets
		setWidgetsEnabled(false, tcpConnectionControls, tcpAuthControls);
		setWidgetsEnabled(false, unixSocketControls);
		this.connectionNameText.setFocus();
	}

	private SelectionAdapter onCustomConnectionSettingsSelection() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button targetButton = ((Button) e.widget);
				if (targetButton.getSelection()) {
					setWidgetsEnabled(true, bindingModeSelectionControls);
					if (bindingMode == UNIX_SOCKET) {
						setWidgetsEnabled(true, unixSocketControls);
					} else {
						if (tcpTLSVerify) {
							setWidgetsEnabled(true, tcpAuthControls);
						}
						setWidgetsEnabled(true, tcpConnectionControls);
					}
				} else {
					setWidgetsEnabled(false, unixSocketControls, tcpConnectionControls, tcpAuthControls);
				}
			}
		};
	}

	private SelectionAdapter onUnixSocketSelection() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button targetButton = ((Button) e.widget);
				if (targetButton.getSelection()) {
					bindingMode = UNIX_SOCKET;
					setWidgetsEnabled(true, unixSocketControls);
				} else {
					setWidgetsEnabled(false, unixSocketControls);
				}
			}
		};
	}

	private SelectionAdapter onTcpConnectionSelection() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button targetButton = ((Button) e.widget);
				if (targetButton.getSelection()) {
					bindingMode = TCP_CONNECTION;
					if (tcpTLSVerify) {
						setWidgetsEnabled(true, tcpAuthControls);
					}
					setWidgetsEnabled(true, tcpConnectionControls);
				} else {
					setWidgetsEnabled(false, tcpConnectionControls);
					setWidgetsEnabled(false, tcpAuthControls);
				}
			}
		};
	}

	private SelectionAdapter onTcpAuthSelection() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button targetButton = ((Button) e.widget);
				if (targetButton.getSelection()) {
					tcpTLSVerify = true;
					setWidgetsEnabled(true, tcpAuthControls);
				} else {
					tcpTLSVerify = false;
					setWidgetsEnabled(false, tcpAuthControls);
				}
			}
		};
	}

	private void setWidgetsEnabled(final boolean enabled, final Control... controls) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
		// set the focus on the fist element of the group.
		if (controls.length > 0 && enabled) {
			controls[0].setFocus();
		}
	}

	private void setWidgetsEnabled(final boolean enabled, final Control[]... controlGroups) {
		for (Control[] controlGroup : controlGroups) {
			for (Control control : controlGroup) {
				control.setEnabled(enabled);
			}
		}
	}

	private ModifyListener onConnectionNameModification() {
		return new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				NewDockerConnectionPage.this.connectionName = ((Text) evt.widget).getText();
			}
		};
	}

	private ModifyListener onUnixSocketModification() {
		return new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				NewDockerConnectionPage.this.unixSocketPath = ((Text) evt.widget).getText();
			}
		};
	}

	private ModifyListener onTcpHostModification() {
		return new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				NewDockerConnectionPage.this.tcpHost = ((Text) evt.widget).getText();
			}
		};
	}

	private ModifyListener onTcpCertPathModification() {
		return new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				NewDockerConnectionPage.this.tcpCertPath = ((Text) evt.widget).getText();
			}
		};
	}
	
	/**
	 * Verifies that the given connection settings work by trying to connect to the 
	 * target Docker daemon
	 * @return
	 */
	private SelectionListener onTestConnectionButtonSelection() {
		return new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ArrayBlockingQueue<Boolean> resultQueue = new ArrayBlockingQueue<>(1);
				try {
					getWizard().getContainer().run(true, false, new IRunnableWithProgress() {
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
							} catch(DockerException e) {
								Activator.log(e);
								resultQueue.add(false);
							}
						}
					});
				} catch (InvocationTargetException | InterruptedException o_O) {
					Activator.log(o_O);
				}
				try {
					final Boolean result = resultQueue.poll(5000, TimeUnit.MILLISECONDS);
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
	 * Opens a new {@link DockerConnection} using the settings of this {@link NewDockerConnectionPage}.
	 * @return
	 * @throws DockerCertificateException
	 */
	protected DockerConnection getDockerConnection() {
		if(bindingMode == UNIX_SOCKET) {
			return new DockerConnection.Builder().name(connectionName).unixSocket(unixSocketPath).build();
		} else {
			if(tcpTLSVerify) {
				return new DockerConnection.Builder().name(connectionName).tcpHost(tcpHost).tcpCertPath(tcpCertPath).build();
			} else {
				return new DockerConnection.Builder().name(connectionName).tcpHost(tcpHost).build();
			}
		}
	}

}
