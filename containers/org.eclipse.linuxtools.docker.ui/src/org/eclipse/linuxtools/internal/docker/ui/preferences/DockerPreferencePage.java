/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.preferences;

import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.BINDING_MODE;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_CERT_PATH;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_CONNECTION;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_HOST;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.TCP_TLS_VERIFY;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET;
import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET_PATH;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DockerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String REFRESH_TIME_MSG = "RefreshTime.label"; //$NON-NLS-1$
	
	private Button unixSocketBtn;
	private Text unixSocketPathText;

	private EnumDockerConnectionSettings bindingMode;
	private String unixSocketPath = null;
	private String tcpHost = null;
	private boolean tcpTLSVerify = false;
	private String tcpCertPath = null;
	
	public DockerPreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	
	/**:
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 * 
	 * Preferences are initialized by the {@link PreferenceInitializer} which attempts to find
	 * DOCKER environment variables at the system level.
	 * 
	 *  @see PreferenceInitializer
	 */
	public void init(final IWorkbench workbench) {
		final IPreferenceStore preferenceStore = getPreferenceStore();
		this.bindingMode = EnumDockerConnectionSettings.valueOf(preferenceStore.getString(BINDING_MODE.toString()));
		this.unixSocketPath = preferenceStore.getString(UNIX_SOCKET_PATH.toString());
		this.tcpHost = preferenceStore.getString(TCP_HOST.toString());
		this.tcpTLSVerify = preferenceStore.getBoolean(TCP_TLS_VERIFY.toString());
		this.tcpCertPath = preferenceStore.getString(TCP_CERT_PATH.toString());
	}

	/**
	 * Saves the current values in the {@link PreferenceStore}.
	 */
	private void savePreferences() {
		final IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(BINDING_MODE.toString(), this.bindingMode.toString());
		preferenceStore.setValue(UNIX_SOCKET_PATH.toString(), this.unixSocketPath);
		preferenceStore.setValue(TCP_HOST.toString(), this.tcpHost);
		preferenceStore.setValue(TCP_TLS_VERIFY.toString(), this.tcpTLSVerify);
		preferenceStore.setValue(TCP_CERT_PATH.toString(), this.tcpCertPath);
	}

	@Override
	public boolean performOk() {
		savePreferences();
		return true;
	}
	

	@Override
	protected void performApply() {
		// TODO Auto-generated method stub
		super.performApply();
	}
	
	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		super.performDefaults();
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(container);
		createConnectionSettingsContainer(container);
		createContainerRefreshContainer(container);
		return container;
	}
	
	/**
	 * Creates the connection settings container, where the user can choose how to 
	 * connect to the docker daemon (using sockets or TCP with SSL - or not)
	 * 
	 * @param parent the parent container (ie, the main container in the preference page)
	 */
	private void createConnectionSettingsContainer(final Composite parent) {
		final int COLUMNS = 3;
		final int INDENT = 20;
		final Group container = new Group(parent, SWT.BORDER);
		container.setText("Docker deamon binding settings");
		container.setToolTipText("Select the binding mode to use to connect to the Docker daemon");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1,  1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(0, 0).spacing(10, 2).applyTo(container);
		
		unixSocketBtn = new Button(container, SWT.RADIO);
		unixSocketBtn.setText("Unix socket");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(COLUMNS,  1).applyTo(unixSocketBtn);
		final Label socketPathLabel = new Label(container, SWT.NONE);
		socketPathLabel.setText("Location:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).applyTo(socketPathLabel);
		unixSocketPathText = new Text(container, SWT.BORDER); 
		unixSocketPathText.setToolTipText("Path to the socket file");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(unixSocketPathText);
		final Button socketPathBrowseButton = new Button(container, SWT.BUTTON1);
		socketPathBrowseButton.setText("Browse...");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(socketPathBrowseButton);
		unixSocketBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button targetButton = ((Button)e.widget);
				if(targetButton.getSelection()) {
					bindingMode = UNIX_SOCKET;
					unixSocketPathText.setEnabled(true);
					socketPathBrowseButton.setEnabled(true);
				} else {
					unixSocketPathText.setEnabled(false);
					socketPathBrowseButton.setEnabled(false);
				}
			}
		});
		unixSocketPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				unixSocketPath = ((Text)evt.widget).getText();
			}
		});
		
		// TCP Connection with optional TLS
		final Button tcpConnectionBtn = new Button(container, SWT.RADIO);
		tcpConnectionBtn.setText("TCP Connection");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(COLUMNS,  1).applyTo(tcpConnectionBtn);
		final Label tcpHostLabel = new Label(container, SWT.NONE);
		tcpHostLabel.setText("Host:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).applyTo(tcpHostLabel);
		final Text tcpHostText = new Text(container, SWT.BORDER); 
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false).applyTo(tcpHostText);
		final Button tcpAuthButton = new Button(container, SWT.CHECK);
		tcpAuthButton.setText("Enable authentication");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).span(3, 1).applyTo(tcpAuthButton);
		final Label tcpCertPathLabel = new Label(container, SWT.NONE);
		tcpCertPathLabel.setText("Path:");
		tcpCertPathLabel.setToolTipText("Path to the certificates folder");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT * 2, 0).applyTo(tcpCertPathLabel);
		final Text tcpCertPathText = new Text(container, SWT.BORDER);
		tcpCertPathText.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(tcpCertPathText);
		final Button tcpCertPathBrowseButton = new Button(container, SWT.BUTTON1);
		tcpCertPathBrowseButton.setText("Browse...");
		tcpCertPathText.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(tcpCertPathBrowseButton);
		
		tcpConnectionBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button targetButton = ((Button)e.widget);
				if(targetButton.getSelection()) {
					bindingMode = TCP_CONNECTION;
					tcpHostText.setEnabled(true);
					tcpAuthButton.setEnabled(true);
				} else {
					tcpHostText.setEnabled(false);
					tcpAuthButton.setEnabled(false);
					tcpCertPathText.setEnabled(false);
					tcpCertPathBrowseButton.setEnabled(false);
				}
			}
		});
		tcpAuthButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button targetButton = ((Button)e.widget);
				if(targetButton.getSelection()) {
					tcpTLSVerify = true;
					tcpCertPathText.setEnabled(true);
					tcpCertPathBrowseButton.setEnabled(true);
				} else {
					tcpTLSVerify = false;
					tcpCertPathText.setEnabled(false);
					tcpCertPathBrowseButton.setEnabled(false);
				}
			}
		});
		tcpHostText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				tcpHost = ((Text)evt.widget).getText();
			}
		});
		tcpCertPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				tcpCertPath = ((Text)evt.widget).getText();
			}
		});
		
		// now that all widgets and their selectionAdapters are instanciated, let's set the values
		if(this.bindingMode == UNIX_SOCKET) {
			unixSocketBtn.setSelection(true);
			if(this.unixSocketPath != null) {
				unixSocketPathText.setText(this.unixSocketPath);
			}
			// disable other widgets
			tcpHostText.setEnabled(false);
			tcpAuthButton.setEnabled(false);
			tcpCertPathText.setEnabled(false);
			tcpCertPathBrowseButton.setEnabled(false);
		} else {
			tcpConnectionBtn.setSelection(true);
			if(this.tcpHost != null) {
				tcpHostText.setText(this.tcpHost);
			}
			if(this.tcpTLSVerify) {
				tcpAuthButton.setSelection(true);
				tcpCertPathText.setEnabled(true);
				if(this.tcpCertPath != null) {
					tcpCertPathText.setText(this.tcpCertPath);
				}
				tcpCertPathBrowseButton.setEnabled(true);
			} 
			// disable other widgets
			unixSocketBtn.setSelection(false);
			unixSocketPathText.setEnabled(false);
		}
	}

	/**
	 * Create a container for the refresh rate property
	 * @param parent the parent container
	 */
	private void createContainerRefreshContainer(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1,  1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, 2).applyTo(container);
		final IntegerFieldEditor refreshTime = new IntegerFieldEditor(PreferenceConstants.REFRESH_TIME,
				Messages.getString(REFRESH_TIME_MSG), container);
		
		refreshTime.setValidRange(5, 200);
		// If the preference changes, alert the Refresh Manager
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(PreferenceConstants.REFRESH_TIME))
					DockerContainerRefreshManager.getInstance().setRefreshTime(refreshTime.getIntValue());
			}
		});
	}



	
}