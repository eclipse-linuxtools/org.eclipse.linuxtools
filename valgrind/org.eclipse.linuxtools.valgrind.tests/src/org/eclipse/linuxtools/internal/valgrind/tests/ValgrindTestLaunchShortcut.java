/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchShortcut;

public class ValgrindTestLaunchShortcut extends ValgrindLaunchShortcut {

	private ILaunchConfiguration config;

	@Override
	public void launch(IBinary bin, String mode) {
		config = findLaunchConfiguration(bin, mode);
	}

	public ILaunchConfiguration getConfig() {
		return config;
	}
}
