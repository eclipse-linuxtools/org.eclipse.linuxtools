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
package org.eclipse.linuxtools.internal.vagrant.core;

import java.io.File;
import java.util.Map;

import org.eclipse.linuxtools.vagrant.core.IVagrantVM;

public class VagrantVM implements IVagrantVM {

	private String id;
	private String name;
	private String provider;
	private String state;
	private String state_desc;
	private File directory;
	private String ip;
	private String user;
	private int port;
	private String identityFile;

	public VagrantVM(String id, String name, String provider, String state,
			String state_desc, File directory, String ip, String user,
			int port, String identityFile) {
		this.id = id;
		this.name = name;
		this.provider = provider;
		this.state = state;
		this.state_desc = state_desc;
		this.directory = directory;
		this.ip = ip;
		this.user = user;
		this.port = port;
		this.identityFile = identityFile;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String provider() {
		return provider;
	}

	@Override
	public String state() {
		return state;
	}

	@Override
	public String state_desc() {
		return state_desc;
	}

	@Override
	public File directory() {
		return directory;
	}

	@Override
	public String ip() {
		return ip;
	}

	@Override
	public String user() {
		return user;
	}

	@Override
	public int port() {
		return port;
	}

	@Override
	public String identityFile() {
		return identityFile;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof VagrantVM) {
			VagrantVM other = (VagrantVM) o;
			return id.equals(other.id())
					&& name.equals(other.name())
					&& provider.equals(other.provider())
					&& state.equals(other.state())
					&& state_desc.equals(other.state_desc())
					&& directory.equals(other.directory());
		}
		return false;
	}

	@Override
	public Map<String, String> getEnvironment() {
		return EnvironmentsManager.getSingleton().getEnvironment(directory);
	}

}
