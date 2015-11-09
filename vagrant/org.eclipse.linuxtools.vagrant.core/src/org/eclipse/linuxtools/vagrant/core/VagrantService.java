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
