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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;

public class RegistryAccountStorageManager {

	public List<IRegistryAccount> getAccounts() {
		final List<IRegistryAccount> accounts = new ArrayList<>();
		final ISecurePreferences preferences = SecurePreferencesFactory
				.getDefault();
		final ISecurePreferences dockerNode = preferences
				.node("org.eclipse.linuxtools.docker.ui.accounts"); //$NON-NLS-1$
		for (String key : dockerNode.keys()) {
			final String[] tokens = key.split(","); //$NON-NLS-1$
			if (tokens.length > 1) {
				final String serverAddress = tokens[0];
				final String username = tokens[1];
				final String email = tokens.length > 2 ? tokens[2] : ""; //$NON-NLS-1$
				final RegistryAccountInfo account = new RegistryAccountInfo(
						serverAddress, username, email, null, false);
				accounts.add(account);
			}
		}
		return accounts;
	}

	public IRegistryAccount getAccount(String serverAddress, String username) {
		return getAccounts().stream()
				.filter(a -> a.getServerAddress().equals(serverAddress)
						&& a.getUsername().equals(username))
				.findFirst().orElse(null);
	}

	public void add(IRegistryAccount info) {
		final ISecurePreferences preferences = getDockerNode();
		final char[] password = info.getPassword();
		final String key = getKeyFor(info);
		try {
			preferences.put(key,
					(password != null ? new String(password) : null), true);
		} catch (StorageException e) {
			Activator.logErrorMessage(
					"Failed to store Docker registry password", e);
		}
	}

	public void remove(final IRegistryAccount info) {
		final ISecurePreferences preferences = getDockerNode();
		final String key = getKeyFor(info);
		preferences.remove(key);
	}

	private ISecurePreferences getDockerNode() {
		final ISecurePreferences preferences = SecurePreferencesFactory
				.getDefault();
		final ISecurePreferences dockerNode = preferences
				.node("org.eclipse.linuxtools.docker.ui.accounts"); //$NON-NLS-1$
		return dockerNode;
	}

	private String getKeyFor(final IRegistryAccount info) {
		return info.getServerAddress() + "," + info.getUsername() + "," + info.getEmail(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
