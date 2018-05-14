/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountStorageManager;
import org.mockito.Mockito;

/**
 * Utility class to get mocked instances of {@link RegistryAccountManager}
 */
public class MockRegistryAccountManagerFactory {

	public static MockRegistryAccountManagerBuilder registryAccount(IRegistryAccount registryAccount) {
		return new MockRegistryAccountManagerBuilder().registryAccount(registryAccount);
	}

	public static class MockRegistryAccountManagerBuilder {

		private final List<IRegistryAccount> registryAccounts = new ArrayList<>();

		public MockRegistryAccountManagerBuilder registryAccount(IRegistryAccount registryAccount) {
			registryAccounts.add(registryAccount);
			return this;
		}

		public RegistryAccountManager build() {
			final RegistryAccountStorageManager mockRegistryAccountStorageManager = Mockito
					.mock(RegistryAccountStorageManager.class);
			Mockito.when(mockRegistryAccountStorageManager.getAccounts()).thenReturn(registryAccounts);
			final RegistryAccountManager registryAccountManager = RegistryAccountManager.getInstance();
			registryAccountManager.setStorageManager(mockRegistryAccountStorageManager);
			return registryAccountManager;
		}

	}

}
