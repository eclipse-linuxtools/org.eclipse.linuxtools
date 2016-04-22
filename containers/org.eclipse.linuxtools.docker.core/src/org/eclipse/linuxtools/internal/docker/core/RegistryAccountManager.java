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
import org.eclipse.linuxtools.docker.core.IRegistryAccount;

public class RegistryAccountManager {

	private static RegistryAccountManager instance;

	private RegistryAccountManager() {
	}

	public static RegistryAccountManager getInstance() {
		if (instance == null) {
			return new RegistryAccountManager();
		}
		return instance;
	}

	public List<IRegistryAccount> getAccounts() {
		List<IRegistryAccount> accounts = new ArrayList<>();
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		ISecurePreferences dockerNode = preferences.node("org.eclipse.linuxtools.docker.ui.accounts"); //$NON-NLS-1$
		for (String key : dockerNode.keys()) {
			String[] tokens = key.split("_"); //$NON-NLS-1$
			String serverAddress = tokens[0];
			String username = tokens[1];
			String email = ""; //$NON-NLS-1$
			if (tokens.length > 2) {
				email = tokens[2];
			}
			RegistryAccountInfo account = new RegistryAccountInfo(serverAddress, username, email, null);
			accounts.add(account);
		}
		return accounts;
	}

	public void add(IRegistryAccount info) {
		ISecurePreferences preferences = getDockerNode();
		char[] password = info.getPassword();
		String key = getKeyFor(info);
		try {
			preferences.put(key, new String(password), true);
		} catch (StorageException e) {
		}
	}

	public void remove(IRegistryAccount info) {
		ISecurePreferences preferences = getDockerNode();
		String key = getKeyFor(info);
		preferences.remove(key);
	}

	private ISecurePreferences getDockerNode() {
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		ISecurePreferences dockerNode = preferences
				.node("org.eclipse.linuxtools.docker.ui.accounts"); //$NON-NLS-1$
		return dockerNode;
	}

	private String getKeyFor(IRegistryAccount info) {
		return info.getServerAddress() + "_" + info.getUsername() + "_" + info.getEmail(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
