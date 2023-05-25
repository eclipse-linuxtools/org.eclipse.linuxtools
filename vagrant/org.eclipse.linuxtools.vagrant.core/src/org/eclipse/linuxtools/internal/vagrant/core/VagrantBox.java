/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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
package org.eclipse.linuxtools.internal.vagrant.core;

import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.osgi.framework.Version;

public class VagrantBox implements IVagrantBox {

	private String name;
	private String provider;
	private Version version;

	public VagrantBox(String name, String provider, Version version) {
		this.name = name;
		this.provider = provider;
		this.version = version;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getProvider() {
		return provider;
	}

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return "Name: " + this.name //$NON-NLS-1$
				+ "Provider : " + this.provider //$NON-NLS-1$
				+ "Version : " + this.version; //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof VagrantBox other) {
			return name.equals(other.getName())
					&& provider.equals(other.getProvider())
					&& version.equals(other.getVersion());
		}
		return false;
	}
}
