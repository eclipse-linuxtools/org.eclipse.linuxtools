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

import org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * 
 */
public class NewDockerConnectionPageModel extends BaseDatabindingModel {

	public static final String CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	public static final String CUSTOM_SETTINGS = "customSettings"; //$NON-NLS-1$
	public static final String UNIX_SOCKET_BINDING_MODE = "unixSocketBindingMode"; //$NON-NLS-1$
	public static final String UNIX_SOCKET_PATH = "unixSocketPath"; //$NON-NLS-1$
	public static final String TCP_CONNECTION_BINDING_MODE = "tcpConnectionBindingMode"; //$NON-NLS-1$
	public static final String TCP_HOST = "tcpHost"; //$NON-NLS-1$
	public static final String TCP_TLS_VERIFY = "tcpTLSVerify"; //$NON-NLS-1$
	public static final String TCP_CERT_PATH = "tcpCertPath"; //$NON-NLS-1$

	/** the name of the connection. */
	private String connectionName;
	/** flag to indicate if custom settings are used. */
	private boolean customSettings = false;
	/** flag to indicate if the binding uses Unix socket. */
	private boolean unixSocketBindingMode = false;
	/** the path to the Unix socket (if used). */
	private String unixSocketPath = null;
	/** flag to indicate if the binding uses a TCP connection. */
	private boolean tcpConnectionBindingMode = false;
	/** the host/port (if REST API connection is used). */
	private String tcpHost = null;
	/** flag to indicate if auth with certificates is enabled. */
	private boolean tcpTLSVerify = false;
	/** path to auth certificates (if enabled). */
	private String tcpCertPath = null;

	/**
	 * @return the binding mode (Unix socket or tcp/REST API).
	 */
	public EnumDockerConnectionSettings getBindingMode() {
		if (this.unixSocketBindingMode) {
			return EnumDockerConnectionSettings.UNIX_SOCKET;
		}
		return EnumDockerConnectionSettings.TCP_CONNECTION;
	}

	/**
	 * @param bindingMode
	 *            the binding mode (Unix socket or tcp/REST API) to set
	 */
	public void setBindingMode(final EnumDockerConnectionSettings bindingMode) {
		setUnixSocketBindingMode(
				bindingMode == EnumDockerConnectionSettings.UNIX_SOCKET);
		setTcpConnectionBindingMode(
				bindingMode == EnumDockerConnectionSettings.TCP_CONNECTION);
	}

	/**
	 * @return the name of the connection
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/**
	 * @param connectionName
	 *            the name of the connection to set
	 */
	public void setConnectionName(final String connectionName) {
		firePropertyChange(CONNECTION_NAME, this.connectionName,
				this.connectionName = connectionName);
	}

	public boolean isCustomSettings() {
		return customSettings;
	}

	public void setCustomSettings(final boolean customSettings) {
		firePropertyChange(CUSTOM_SETTINGS, this.customSettings,
				this.customSettings = customSettings);
	}

	public boolean isUnixSocketBindingMode() {
		return unixSocketBindingMode;
	}

	public void setUnixSocketBindingMode(boolean unixSocketBindingMode) {
		firePropertyChange(UNIX_SOCKET_BINDING_MODE, this.unixSocketBindingMode,
				this.unixSocketBindingMode = unixSocketBindingMode);
	}

	/**
	 * @return the path to the Unix socket (if used).
	 */
	public String getUnixSocketPath() {
		return unixSocketPath;
	}

	/**
	 * @param unixSocketPath
	 *            the path to the Unix socket (if used) to set
	 */
	public void setUnixSocketPath(final String unixSocketPath) {
		firePropertyChange(UNIX_SOCKET_PATH, this.unixSocketPath,
				this.unixSocketPath = unixSocketPath);
	}

	public boolean isTcpConnectionBindingMode() {
		return tcpConnectionBindingMode;
	}

	public void setTcpConnectionBindingMode(boolean tcpBindingMode) {
		firePropertyChange(TCP_CONNECTION_BINDING_MODE, this.tcpConnectionBindingMode,
				this.tcpConnectionBindingMode = tcpBindingMode);
	}

	/**
	 * @return the host/port (if REST API connection is used)
	 */
	public String getTcpHost() {
		return tcpHost;
	}

	/**
	 * @param tcpHost
	 *            the host/port (if REST API connection is used) to set
	 */
	public void setTcpHost(final String tcpHost) {
		firePropertyChange(TCP_HOST, this.tcpHost, this.tcpHost = tcpHost);
	}

	/**
	 * @return flag to indicate if auth with certificates is enabled
	 */
	public boolean isTcpTLSVerify() {
		return tcpTLSVerify;
	}

	/**
	 * @param tcpTLSVerify
	 *            flag to indicate if auth with certificates is enabled
	 */
	public void setTcpTLSVerify(final boolean tcpTLSVerify) {
		firePropertyChange(TCP_TLS_VERIFY, this.tcpTLSVerify,
				this.tcpTLSVerify = tcpTLSVerify);
	}

	/**
	 * @return path to auth certificates (if enabled)
	 */
	public String getTcpCertPath() {
		return tcpCertPath;
	}

	/**
	 * @param tcpCertPath
	 *            path to auth certificates (if enabled) to set
	 */
	public void setTcpCertPath(final String tcpCertPath) {
		firePropertyChange(TCP_CERT_PATH, this.tcpCertPath,
				this.tcpCertPath = tcpCertPath);
	}

}
