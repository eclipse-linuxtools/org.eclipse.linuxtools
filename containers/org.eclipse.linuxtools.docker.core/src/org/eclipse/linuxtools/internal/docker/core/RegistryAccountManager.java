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

import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IRegistryAccount;

public class RegistryAccountManager {

	private static RegistryAccountManager instance;

	/** the storage manager. */
	private RegistryAccountStorageManager storageManager = new RegistryAccountStorageManager();

	/** local cache of accounts. */
	private List<IRegistryAccount> registryAccounts;

	/**
	 * Private constructor of the singleton
	 */
	private RegistryAccountManager() {

	}

	public static RegistryAccountManager getInstance() {
		if (instance == null) {
			instance = new RegistryAccountManager();
		}
		return instance;
	}

	/**
	 * Replaces the default storage manager implementation instance,
	 * {@link RegistryAccountStorageManager} by another one. This can be used
	 * during tests if a mock instance is used to avoid actually storing
	 * registry accounts.
	 * 
	 * @param storageManager
	 *            the {@link RegistryAccountStorageManager} instance to use
	 */
	public void setStorageManager(
			final RegistryAccountStorageManager storageManager) {
		this.storageManager = storageManager;
		// make sure the registry accounts are bound to the new storage manager
		this.registryAccounts = storageManager.getAccounts();
	}

	/**
	 * @return the underlying {@link RegistryAccountStorageManager}
	 */
	public RegistryAccountStorageManager getStorageManager() {
		return this.storageManager;
	}

	public List<IRegistryAccount> getAccounts() {
		if (this.registryAccounts == null) {
			this.registryAccounts = storageManager.getAccounts();
		}
		return Collections.unmodifiableList(this.registryAccounts);
	}

	public IRegistryAccount getAccount(final String serverAddress,
			final String username) {
		return getAccounts().stream()
				.filter(a -> a.getServerAddress().equals(serverAddress)
						&& a.getUsername().equals(username))
				.findFirst().orElse(null);
	}

	public void add(final IRegistryAccount info) {
		this.registryAccounts.add(info);
		storageManager.add(info);
	}

	public void remove(final IRegistryAccount info) {
		this.registryAccounts.remove(info);
		storageManager.remove(info);
	}


}
