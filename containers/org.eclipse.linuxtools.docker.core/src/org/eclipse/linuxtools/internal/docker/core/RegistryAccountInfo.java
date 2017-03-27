/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.net.Authenticator;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;

public class RegistryAccountInfo extends RegistryInfo implements IRegistryAccount {

	private final String username;
	private final String email;
	private final char[] password;

	public RegistryAccountInfo(final String serverAddress,
			final String username, final String email, final char[] password,
			final boolean dockerHubRegistry) {
		super(serverAddress, dockerHubRegistry);
		this.username = username;
		this.email = email;
		this.password = password;
	}

	@Override
	public String getRegistryId() {
		return "[username=" + username + ", email=" + email //$NON-NLS-1$ //$NON-NLS-2$
				+ ", getServerAddress()=" + getServerAddress() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean isAuthProvided() {
		return this.username != null && getPassword() != null;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public char [] getPassword() {
		if (password != null) {
			return password;
		}
		char[] password = null;
		final ISecurePreferences preferences = SecurePreferencesFactory
				.getDefault();
		final ISecurePreferences dockerNode = preferences
				.node("org.eclipse.linuxtools.docker.ui.accounts"); //$NON-NLS-1$
		final String key = getServerAddress() + "," + getUsername() + ","
				+ getEmail();
		try {
			password = dockerNode.get(key, null) != null
					? dockerNode.get(key, null).toCharArray() : null;
		} catch (StorageException e) {
		}
		return password;
	}

	@Override
	protected void enableDockerAuthenticator() {
		if (getUsername() != null && getPassword() != null) {
			Authenticator.setDefault(new DockerAuthenticator(getUsername(), getPassword()));
		}
	}

	@Override
	protected void restoreAuthenticator() {
		IExtension[] extensions = RegistryFactory.getRegistry()
				.getExtensionPoint("org.eclipse.core.net", "authenticator") //$NON-NLS-1$ //$NON-NLS-2$
				.getExtensions();
		if (extensions.length == 0) {
			return;
		}
		IExtension extension = extensions[0];
		IConfigurationElement[] configs = extension.getConfigurationElements();
		if (configs.length == 0) {
			return;
		}
		try {
			IConfigurationElement config = configs[0];
			Authenticator original = (Authenticator) config
					.createExecutableExtension("class"); //$NON-NLS-1$
			Authenticator.setDefault(original);
		} catch (CoreException ex) {
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + Arrays.hashCode(password);
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RegistryAccountInfo other = (RegistryAccountInfo) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		if (!Arrays.equals(password, other.password)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RegistryAccountInfo [username=" + username + ", email=" + email
				+ ", getServerAddress()=" + getServerAddress()
				+ ", isDockerHubRegistry()=" + isDockerHubRegistry()
				+ ", isVersion2()=" + isVersion2() + "]";
	}

}
