/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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
package org.eclipse.linuxtools.vagrant.core;

import org.eclipse.linuxtools.internal.vagrant.core.VagrantConnection;

public class VagrantService {

	private static VagrantConnection client;

	/**
	 * Retrieve an instance of an IVagrantConnection for calling various
	 * 'vagrant' commands.
	 *
	 * @return an instance of an IVagrantConnection
	 */
	public static IVagrantConnection getInstance() {
		if (client == null) {
			client = new VagrantConnection();
		}
		return client;
	}
}
